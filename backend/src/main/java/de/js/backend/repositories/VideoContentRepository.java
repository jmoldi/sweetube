package de.js.backend.repositories;

import de.js.backend.data.VideoContent;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface VideoContentRepository extends ReactiveMongoRepository<VideoContent,String> {

}
