package de.js.backend.controller;

import de.js.backend.data.VideoContent;
import de.js.backend.services.VideoContentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api")
@Slf4j
public class ContentController {

	@Resource
	VideoContentService videoContentService;

	@PostMapping(value="/content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Flux<VideoContent> consumeVideoContent(@RequestPart("files") Flux<FilePart> file, ServerWebExchange webExchange){
		return file.flatMap(filePart -> videoContentService.create(filePart, webExchange.getResponse()))
				.switchIfEmpty(a -> emptyResponse(webExchange));
	}

	private Mono<?> emptyResponse(ServerWebExchange exchange){
		exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
		return Mono.empty();
	}

	@GetMapping(value="/content")
	public Flux<VideoContent> getVideoContent(){
		return videoContentService.getAll();
	}

	@GetMapping(value="/content/own")
	public Flux<VideoContent> getOwnVideoContent(){
		return videoContentService.getAll();
	}

	@GetMapping(value="/content/{id}")
	public Mono<VideoContent> getVideoContent(@PathVariable String id){
		return videoContentService.getOne(id);
	}

	@GetMapping("/content/{id}/stream")
	public Flux<Void> read(@PathVariable String id, ServerWebExchange exchange) {
		return videoContentService.stream(id,exchange);
	}

	@GetMapping(value="/content/{id}/prepare")
	public Mono<VideoContent> prepareVideo(@PathVariable String id, ServerHttpResponse response){
		return videoContentService.prepareContent(id, response);
	}
}
