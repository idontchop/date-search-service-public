package com.idontchop.datesearchservice.dtos;

import java.time.LocalDateTime;

/**
 * Used by an API to give a message.
 * 
 * @see SearchDto
 * 
 * @author nathan
 *
 */
public class ApiMessage {

	private LocalDateTime created = LocalDateTime.now();
	
	private String level;		// level of error, "fatal, info" etc
	
	private String message;
	
	public ApiMessage () {}
	
	public ApiMessage (String level, String message) {
		this.level = level;
		this.message = message;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
