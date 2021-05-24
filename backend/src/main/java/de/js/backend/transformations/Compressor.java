package de.js.backend.transformations;

import io.github.techgnious.IVCompressor;
import io.github.techgnious.dto.ResizeResolution;
import io.github.techgnious.dto.VideoFormats;
import io.github.techgnious.exception.VideoException;
import org.apache.commons.io.FileUtils;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.encode.enums.X264_PROFILE;
import ws.schild.jave.info.VideoSize;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public class Compressor {
	private EncodingAttributes encodingAttributes;

	public Compressor(){

		VideoAttributes videoAttributes = new VideoAttributes();
		videoAttributes.setCodec("h264");
		videoAttributes.setX264Profile(X264_PROFILE.BASELINE);
		videoAttributes.setBitRate(160000);
		videoAttributes.setFrameRate(15);
		videoAttributes.setSize(new VideoSize(ResizeResolution.VIDEO_DEFAULT.getWidth(), ResizeResolution.VIDEO_DEFAULT.getHeight()));
		AudioAttributes audioAttributes = new AudioAttributes();
		audioAttributes.setCodec("aac");
		audioAttributes.setBitRate(64000);
		audioAttributes.setChannels(2);
		audioAttributes.setSamplingRate(44100);
		this.encodingAttributes = new EncodingAttributes();
		encodingAttributes.setVideoAttributes(videoAttributes);
		encodingAttributes.setAudioAttributes(audioAttributes);
	}

	public byte[] resize(byte[] data, VideoFormats fileFormat, int width, int height) throws VideoException {
		String fileType = fileFormat.getType();
		this.encodingAttributes.setInputFormat(fileType);
		this.encodingAttributes.setOutputFormat(fileType);

		if (width != 0 && height != 0) {
			Optional<VideoAttributes> videoAttr = this.encodingAttributes.getVideoAttributes();
			if (videoAttr.isPresent()) {
				videoAttr.get().setSize(new VideoSize(width, height));
			}
		}

		return encode(data, fileType);
	}

	protected byte[] encode(byte[] data, String fileFormat) throws VideoException {
		File target = null;
		File file = null;

		byte[] var6;
		try {
			target = File.createTempFile("target", fileFormat);
			file = File.createTempFile("source", fileFormat);
			FileUtils.writeByteArrayToFile(file, data);
			MultimediaObject source = new MultimediaObject(file);
			new Encoder().encode(source, target, this.encodingAttributes);
			var6 = FileUtils.readFileToByteArray(target);
		} catch (Exception var15) {
			throw new VideoException("Error Occurred while resizing the video", var15);
		} finally {
			try {
				if (file != null) {
					Files.deleteIfExists(file.toPath());
				}

				if (target != null) {
					Files.deleteIfExists(target.toPath());
				}
			} catch (IOException var14) {
			}

		}

		return var6;
	}
}
