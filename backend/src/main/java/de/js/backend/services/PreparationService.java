package de.js.backend.services;

import de.js.backend.data.ContentFile;
import de.js.backend.data.VideoContent;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegLogCallback;
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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

			Mono<VideoContent> thumnailProcess = DataBufferUtils
					.write(buffer, byteArrayOutputStream)
					.map(DataBufferUtils::release)
					.then()
					.flatMap(empty -> consume(byteArrayOutputStream.toByteArray(), videoContent, response));
			return thumnailProcess;

		} catch (Exception e) {
			log.error("thumnail creation error", e);
		} catch (IOException e) {
			log.error("thumnail creation error on bytearraystream", e);
		}

		return Mono.empty();
	}

	private Mono<VideoContent> consume(byte[] array, VideoContent videoContent, ServerHttpResponse response) {
		log.info("bytearray databuffer progress " + array.length);
		var thumbnails = randomGrabberFFmpegImage(array, MOD);
		DataBufferFactory dataBufferFactory = response.bufferFactory();

		Flux<DataBuffer> result = DataBufferUtils
				.readInputStream(() -> new ByteArrayInputStream(thumbnails.get(0)), dataBufferFactory, defaultBufferSize);
		return this.gridFsTemplate.store(result, "thumbnail.png").map((objectId) -> {
			videoContent.setThumbnailId(objectId.toHexString());
			return videoContent;
		});
	}

	/**
	 * Get video thumbnails
	 *
	 * @param bytes: video bytes
	 * @param mod:   video length / mod gets the few frames
	 * @throws Exception
	 */
	public static List<byte[]> randomGrabberFFmpegImage(byte[] bytes, int mod) {
		if (bytes.length == 0) {
			throw new IllegalArgumentException("video bytes are empty");
		}
		List<byte[]> list = new ArrayList<>();
		try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {
			FFmpegFrameGrabber ff = new FFmpegFrameGrabber(stream); //.createDefault(filePath);
			//tried to set this once bytes was empty
			// FFmpegLogCallback.set();
			ff.start();
			int ffLength = ff.getLengthInFrames();
			Frame f;
			int i = 0;
			int index = ffLength / mod;
			while (i < ffLength) {
				f = ff.grabImage();
				if (i == index) {

					if (null == f || null == f.image) {
						log.info("image not given, abort");
						break;
					}
					Java2DFrameConverter converter = new Java2DFrameConverter();
					BufferedImage bi = converter.getBufferedImage(f);

					try (ByteArrayOutputStream baos = new ByteArrayOutputStream(defaultBufferSize);
					     ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
						ImageIO.write(bi, IMAGEMAT, ios);
						list.add(baos.toByteArray());
					} catch (IOException e) {
						log.error("write thumbnail", e);
					}
					break;
				}
				i++;
			}
			ff.stop();
			return list;
		} catch (Exception ex) {
			log.error("prepation", ex);
		} catch (IOException ex) {
			log.error("io prepation", ex);
		}
		return list;
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
