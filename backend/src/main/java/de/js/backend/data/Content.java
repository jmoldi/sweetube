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

import java.util.Date;

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

	Long views;
	@CreatedDate
	Date createdAt;

	@Transient
	User creator;

	@JsonProperty("hasContent")
	public boolean hasContent() {
		return contentId != null;
	}
}