package de.js.backend.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bson.types.Binary;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContentFile {

	@JsonIgnore
	Binary content;
	String mimeType;
	String url;
}
