package de.js.backend.data;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
	@Id
	String id;
	String name;
	String avatar;
}
