package de.js.backend.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class Content {
	@Id
	String id;
	String creatorId;
	String name;
	String contentId;
	String thumbnailId;
	String previewId;
	Long views = 0l;
	Date createdAt = new Date();

	@Transient
	@JsonProperty("creator")
	public User getTempCreator(){
		return User.builder()
				.id("some")
				.name("Elon")
				.avatar("https://image.flaticon.com/icons/png/512/194/194938.png")
				.build();
	}

	@JsonProperty("hasContent")
	public boolean hasContent() {
		return contentId != null;
	}
}