package de.js.backend.services;

import de.js.backend.data.VideoContent;
import de.js.backend.repositories.VideoContentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
@Slf4j
public class VideoContentService {
	@Value("${baseUrl}")
	String baseUrl;

	@Resource
	VideoContentRepository videoContentRepository;
	@Resource
	PreparationService preparationService;

	@Resource
	ReactiveGridFsTemplate gridFsTemplate;

	public Mono<VideoContent> create(FilePart filePart, ServerHttpResponse response) {
		return this.gridFsTemplate.store(filePart.content(), filePart.filename()).flatMap((id) -> {
			VideoContent entity = new VideoContent();
			entity.setContentId(id.toHexString());
			return videoContentRepository.save(entity);
		}).flatMap(videoContent -> {
			return prepareContent(videoContent.getId(), response);
		});
	}

	public Flux<VideoContent> getAll() {
		return videoContentRepository.findAll();
	}

	public Mono<VideoContent> getOne(String id) {
		return videoContentRepository.findById(id);
	}

	public Flux<Void> stream(String id, ServerWebExchange webExchange) {
		return getContent(id)
				.flatMapMany(r -> webExchange.getResponse().writeWith(r.getDownloadStream()));
	}

	public Mono<ReactiveGridFsResource> getContent(String id) {
		return this.gridFsTemplate.findOne(query(where("_id").is(id)))
				.log()
				.flatMap(gridFsTemplate::getResource);
	}

	/**
	 * prepare content with thumbnail and preview
	 *
	 * @param id
	 * @return
	 */
	public Mono<VideoContent> prepareContent(String id, ServerHttpResponse response) {
		log.info("preparing content " + id);
		return videoContentRepository.findById(id)
				.map(videoContent -> {
					Flux<DataBuffer> buffer = getContent(videoContent.getContentId())
							.flatMapMany(r -> r.getDownloadStream());
					return prepareContent(buffer, videoContent, response);

				}).flatMap(a -> a)
				.flatMap(videoContent -> videoContentRepository.save(videoContent));
	}

	private Mono<? extends VideoContent> emptyResponse(ServerHttpResponse response) {
		log.info("returning empty response 204");
		response.setStatusCode(HttpStatus.NO_CONTENT);
		return Mono.empty();
	}

	private Mono<VideoContent> prepareContent(Flux<DataBuffer> content, VideoContent videoContent, ServerHttpResponse response) {
		if (videoContent == null) {
			throw new IllegalStateException("video not found");
		}
		return preparationService.prepare(content, videoContent, response);
	}
}
