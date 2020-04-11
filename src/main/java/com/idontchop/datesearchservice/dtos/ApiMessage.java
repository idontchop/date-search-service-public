package com.idontchop.datesearchservice.dtos;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	
	private String error;
	
	private String message;
	
	public ApiMessage () {}
	
	public ApiMessage (String level, String message) {
		this.level = level;
		this.message = message;
	}
	
	public static ApiMessage build () {
		ApiMessage a = new ApiMessage();
		return a;
	}

	@JsonIgnore
	public LocalDateTime getCreated() {
		return created;
	}
	
	public String getCreatedTime () {
		return created.toString();
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	public String getLevel() {
		return level;
	}

	public ApiMessage setLevel(String level) {
		this.level = level;
		return this;
	}

	public String getMessage() {
		return message;
	}

	public ApiMessage setMessage(String message) {
		this.message = message;
		return this;
	}
	
	public static ApiMessage from (String message) {
		ApiMessage a = new ApiMessage();
		a.setLevel("default");
		a.setMessage(message);
		return a;
	}

	public static ApiMessage error (String error ) {
		ApiMessage a = new ApiMessage();
		a.setLevel("ERROR");
		a.setError(error);
		return a;
	}
	
	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
	
	
}
