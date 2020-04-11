package com.idontchop.datesearchservice.dtos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

/**
 * This is mainly a wrapper to List<String> potentials
 * 
 * It is passed through the api chain when forming a search for the user, storing
 * the potentials List as well as any messages from the apis in the chain.
 * 
 * For example, if an error occured on the Likes microservice, this isn't a reason
 * to break the whole search request.
 * 
 * @author nathan
 *
 */
public class SearchDto {
	
	private LocalDateTime created = LocalDateTime.now();
	
	private List<String> potentials = new ArrayList<>();
	
	private Map<String,ApiMessage> apiMessages = new HashMap<>();

	public SearchDto ( List<String> potentials ) {
		this.potentials = potentials;
	}
	
	public SearchDto () {}
	
	public static SearchDto build () {
		SearchDto newDto = new SearchDto();
		return newDto;
	}
	
	public static SearchDto build( List<String> potentials ) {
		SearchDto newDto = build();
		newDto.setPotentials(potentials);
		return newDto;
	}
	
	public SearchDto add (String name, ApiMessage newApiMessage ) {
		apiMessages.put(name, newApiMessage);
		return this;
	}
	
	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	public List<String> getPotentials() {
		return potentials;
	}

	public void setPotentials(List<String> potentials) {
		this.potentials = potentials;
	}

	@JsonAnyGetter
	public Map<String, ApiMessage> getApiMessages() {
		return apiMessages;
	}

	public void setApiMessages(Map<String, ApiMessage> apiMessages) {
		this.apiMessages = apiMessages;
	}
	
	

}
