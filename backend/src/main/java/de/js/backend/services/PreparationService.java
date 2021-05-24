package de.js.backend.services;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import de.js.backend.data.FileSize;
import de.js.backend.data.VideoContent;
import de.js.backend.transformations.Compressor;
import io.github.techgnious.dto.VideoFormats;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class PreparationService {
	@Value("${baseUrl}")
	String baseUrl;
	@Resource
	ReactiveGridFsTemplate gridFsTemplate;
	public static final int defaultBufferSize = 1 << 12;
	@Value("${thumbnail.maxsize:400}")
	Integer maxThumbnailSize;

	private static final String IMAGEMAT = "png";
	//private static final String ROTATE = "rotate";

	public byte[] scaleVideo(byte[] videoData) {
		String videoFormatName = null;
		int width = 0;
		int height = 0;
		try (ByteArrayInputStream stream = new ByteArrayInputStream(videoData);
		     FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(stream)) {

			grabber.start();
			String rawFormat = grabber.getFormat();
			videoFormatName = rawFormat.contains("mp4") ? "mp4" : rawFormat.split("\\,")[0];
			width = grabber.getImageWidth();
			height = grabber.getImageHeight();

		} catch (java.lang.Exception ex) {
			log.error("video retrieve details", ex);
		}
		if (videoFormatName == null || width == 0 || height == 0) {
			log.error("video details could not be retrieved");
			return null;
		}

		Compressor compress = new Compressor();
		VideoFormats fileFormat = VideoFormats.valueOf(videoFormatName.toUpperCase());
		if (fileFormat == null) {
			throw new IllegalArgumentException("video format " + videoFormatName + " not supported");
		}
		FileSize resolution = pickResolution(width, height);
		try {
			byte[] VideoOutput = compress.resize(videoData, fileFormat, resolution.getWidth(), resolution.getHeight());
			return VideoOutput;
		} catch (java.lang.Exception ex) {
			log.error("video scaling", ex);
		}
		return null;
	}

	/**
	 * The middle frame of the default captured video is the cover
	 */
	public static final int MOD = 2;

	public Mono<VideoContent> prepare(Flux<DataBuffer> buffer, VideoContent videoContent, ServerHttpResponse response) {

		log.info("thumbnail creation");
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

			Mono<VideoContent> thumbnailProcess = DataBufferUtils
					.write(buffer, byteArrayOutputStream)
					.map(DataBufferUtils::release)
					.collectList()
					.flatMap(empty -> consume(byteArrayOutputStream.toByteArray(), videoContent, response));
			return thumbnailProcess;

		} catch (Exception e) {
			log.error("thumnail creation error", e);
		} catch (IOException e) {
			log.error("thumnail creation error on bytearraystream", e);
		}

		return Mono.empty();
	}

	private Mono<VideoContent> consume(byte[] originalVideoSource, VideoContent videoContent, ServerHttpResponse response) {
		byte[] scaledVideoSource = scaleVideo(originalVideoSource);
		byte[] thumbnail = getThumbnail(scaledVideoSource);
		DataBufferFactory dataBufferFactory = response.bufferFactory();

		Flux<DataBuffer> thumbnailBuffer = DataBufferUtils
				.readInputStream(() -> new ByteArrayInputStream(thumbnail), dataBufferFactory, defaultBufferSize);
		return this.gridFsTemplate.store(thumbnailBuffer, "thumbnail.png").map((objectId) -> {
			videoContent.setThumbnailId(objectId.toHexString());
			return videoContent;
		}).flatMap(content -> {
			byte[] preview = getPreview(scaledVideoSource);
			Flux<DataBuffer> previewBuffer = DataBufferUtils
					.readInputStream(() -> new ByteArrayInputStream(preview), dataBufferFactory, defaultBufferSize);
			return this.gridFsTemplate.store(previewBuffer, "preview.gif").map((objectId) -> {

				videoContent.setPreviewId(objectId.toHexString());
				return videoContent;
			});
		});
	}

	/**
	 * Get video thumbnails
	 *
	 * @param bytes: video bytes
	 * @throws Exception
	 */
	public byte[] getThumbnail(byte[] bytes) {
		if (bytes.length == 0) {
			throw new IllegalArgumentException("video bytes are empty");
		}
		try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		     FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(stream)) {

			grabber.start();
			int ffLength = grabber.getLengthInFrames();
			Frame frame;

			int targetFrameNumber = ffLength / MOD;
			grabber.setFrameNumber(targetFrameNumber);
			frame = grabber.grabImage();

			if (null == frame || null == frame.image) {
				log.info("image not given, abort");
				return null;
			}
			Java2DFrameConverter converter = new Java2DFrameConverter();
			BufferedImage bufferedImage = converter.getBufferedImage(frame);

			try (ByteArrayOutputStream baos = new ByteArrayOutputStream(defaultBufferSize);
			     ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
				ImageIO.write(bufferedImage, IMAGEMAT, ios);
				return baos.toByteArray();
			} catch (IOException e) {
				log.error("write thumbnail", e);
			}

		} catch (Exception ex) {
			log.error("prepation", ex);
		} catch (IOException ex) {
			log.error("io prepation", ex);
		}
		return null;
	}

	public byte[] getPreview(byte[] bytes) {

		try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		     FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(stream);
		     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();) {
			grabber.start();
			double framerate = grabber.getFrameRate();
			int frames = grabber.getLengthInFrames();

			// NOTE duration in ms (duration / 1000 = ms) instead of frame count
			long duration = grabber.getLengthInTime();

			Map<Integer, Integer> slices = getSlices(frames, framerate);

			Java2DFrameConverter converter = new Java2DFrameConverter();
			AnimatedGifEncoder en = new AnimatedGifEncoder();
			en.setFrameRate(Double.valueOf(framerate).floatValue());
			en.start(outputStream);

			for (Map.Entry<Integer, Integer> entry : slices.entrySet()) {
				int startFrame = entry.getKey().intValue();
				grabber.setFrameNumber(startFrame);
				int frameCount = entry.getValue().intValue();

				for (int i = 0; i < frameCount; i++) {
					//log.info("frame for gif " + grabber.getFrameNumber());
					en.addFrame(converter.convert(grabber.grabImage()));
				}
			}

			en.finish();

			return outputStream.toByteArray();
		} catch (java.lang.Exception ex) {
			log.error("preview", ex);
		}

		return bytes;
	}

	private static Map<Integer, Integer> getSlices(int frames, double framerate) {
		TreeMap<Integer, Integer> slices = new TreeMap<>();
		int maxSlices = 4;
		int minimumFrame = 3;
		double sliceDurationInS = 1.5;
		double sliceOffsetInS = 0.5;

		double sliceDuration = sliceDurationInS * framerate;
		double sliceOffset = sliceOffsetInS * framerate;
		double totalSliceDuration = sliceDuration + sliceOffset;
		double halfOffset = sliceOffset / 2;

		if (frames + minimumFrame < sliceDuration) {
			slices.put(minimumFrame, Long.valueOf(frames).intValue());
			return slices;
		}

		double fits = frames / totalSliceDuration;
		int framesPerSlice = frames / maxSlices;

		if (fits * 3 > maxSlices) {
			//high space

			Random random = new Random();
			for (int i = 0; i < maxSlices; i++) {
				int startFrame = Double.valueOf(framesPerSlice * i + halfOffset).intValue();
				int frame = pickFrame(random, startFrame, startFrame + framesPerSlice - Double.valueOf(sliceDuration).intValue());
				slices.put(Integer.valueOf(frame), Double.valueOf(sliceDuration).intValue());
			}
		} else {
			//low space

			double postAddition = 0;
			for (int i = 0; i < maxSlices; i++) {
				int frame = Double.valueOf(framesPerSlice * i + halfOffset + postAddition).intValue();
				slices.put(Integer.valueOf(frame), Double.valueOf(sliceDuration).intValue());
				postAddition = halfOffset;
			}
		}

		return slices;
	}

	private static int pickFrame(Random random, int min, int max) {
		return random.nextInt((max - min) + 1) + min;
	}

	private FileSize pickResolution(int width, int height) {
		double aspectRatio;
		FileSize fileSize;
		if (width >= height) {
			aspectRatio = (double) width / (double) height;

			fileSize= FileSize.builder()
					.width(maxThumbnailSize)
					.height(Double.valueOf(maxThumbnailSize / aspectRatio).intValue())
					.build();
		}else{
			aspectRatio = (double) height / (double) width;
			fileSize = FileSize.builder()
					.width(Double.valueOf(maxThumbnailSize / aspectRatio).intValue())
					.height(maxThumbnailSize)
					.build();
		}
		if(fileSize.getHeight() % 2 != 0){
			fileSize.setHeight(fileSize.getHeight() - 1);
		}
		if(fileSize.getWidth() % 2 != 0){
			fileSize.setWidth(fileSize.getWidth() - 1);
		}
		return fileSize;
	}

}
