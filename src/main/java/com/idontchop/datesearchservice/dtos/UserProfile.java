package com.idontchop.datesearchservice.dtos;

/**
 * Used for updating User Profile. Information taken from this form will be propagated to various microservices.
 * 
 * @author micro
 *
 */
public class UserProfile {

	private String username;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	
}
