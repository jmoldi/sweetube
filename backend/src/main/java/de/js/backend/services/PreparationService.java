package de.js.backend.services;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import de.js.backend.data.VideoContent;
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

	private static final String IMAGEMAT = "png";
	//private static final String ROTATE = "rotate";

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
					.doOnNext(s -> log.info("buffer video page"))
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

	private Mono<VideoContent> consume(byte[] array, VideoContent videoContent, ServerHttpResponse response) {
		log.info("bytearray databuffer progress " + array.length);
		byte[] thumbnail = getThumbnail(array);
		DataBufferFactory dataBufferFactory = response.bufferFactory();

		Flux<DataBuffer> thumbnailBuffer = DataBufferUtils
				.readInputStream(() -> new ByteArrayInputStream(thumbnail), dataBufferFactory, defaultBufferSize);
		return this.gridFsTemplate.store(thumbnailBuffer, "thumbnail.png").map((objectId) -> {
			videoContent.setThumbnailId(objectId.toHexString());
			return videoContent;
		}).flatMap(content -> {
			byte[] preview = getPreview(array);
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
	public static byte[] getThumbnail(byte[] bytes) {
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

	public static byte[] getPreview(byte[] bytes) {

		try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		     FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(stream);
		     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();) {
			grabber.start();
			double framerate = grabber.getFrameRate();
			int frames = grabber.getLengthInFrames();

			// NOTE duration in ms instead of frame count
			//long duration = grabber.getLengthInTime();

			Map<Integer, Integer> slices = getSlices(frames);

			Java2DFrameConverter converter = new Java2DFrameConverter();
			AnimatedGifEncoder en = new AnimatedGifEncoder();
			en.setFrameRate(Double.valueOf(framerate).floatValue());
			en.start(outputStream);

			for (Map.Entry<Integer,Integer> entry : slices.entrySet()){
				int startFrame = entry.getKey().intValue();
				grabber.setFrameNumber(startFrame);
				int frameCount = entry.getValue().intValue();

				for (int i = 0; i < frameCount; i++) {
					en.addFrame(converter.convert(grabber.grab()));
					grabber.setFrameNumber(grabber.getFrameNumber() + 1);
				}
			}

			en.finish();

			return outputStream.toByteArray();
		} catch (java.lang.Exception ex) {
			log.error("preview", ex);
		}

		return bytes;
	}

	private static Map<Integer, Integer> getSlices(int frames) {
		Map<Integer, Integer> slices = new HashMap<>();
		int maxSlices = 4;
		int minimumFrame = 3;
		int sliceDuration = 1500; // 1.5 sec
		int sliceOffset = 500;
		int totalSliceDuration = sliceDuration + sliceOffset;
		int halfOffset = sliceOffset / 2;

		if (frames + minimumFrame < sliceDuration) {
			slices.put(minimumFrame, Long.valueOf(frames).intValue());
			return slices;
		}

		long fits = frames / totalSliceDuration;
		int framesPerSlice = frames / maxSlices;

		if (fits * 3 > maxSlices) {
			//high space

			Random random = new Random();
			for (int i = 0; i < maxSlices; i++) {
				int startFrame = framesPerSlice * i + halfOffset;
				int frame = pickFrame(random, startFrame, startFrame + framesPerSlice - sliceDuration);
				slices.put(Integer.valueOf(frame), sliceDuration);
			}
		} else {
			//low space

			int postAddition = 0;
			for (int i = 0; i < maxSlices; i++) {
				int frame = framesPerSlice * i + halfOffset + postAddition;
				slices.put(Integer.valueOf(frame), sliceDuration);
				postAddition = halfOffset;
			}
		}

		return slices;
	}

	private static int pickFrame(Random random, int min, int max) {
		return random.nextInt((max - min) + 1) + min;
	}


	/**
	 * Randomly generate random number sets based on video length
	 *
	 * @param baseNum: the base number, here is the video length
	 * @param length:  random number set length
	 * @return: a collection of random numbers
	 */
	public static List<Integer> random(int baseNum, int length) {
		List<Integer> list = new ArrayList<Integer>(length);
		while (list.size() < length) {
			Integer next = (int) (Math.random() * baseNum);
			if (list.contains(next)) {
				continue;
			}
			list.add(next);
		}
		Collections.sort(list);
		return list;
	}
}
