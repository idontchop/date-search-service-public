package com.idontchop.datesearchservice.dtos;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
	
	private Set<String> potentials = new HashSet<>();
	
	// Used for tertiary requests. (Likes)
	private Map<String, Set<String>> matches = new HashMap<>();
	
	private Map<String,ApiMessage> apiMessages = new HashMap<>();

	public SearchDto ( Set<String> potentials ) {
		this.potentials = potentials;
	}
	
	public SearchDto () {}
	
	public static SearchDto build () {
		SearchDto newDto = new SearchDto();
		return newDto;
	}
	
	public static SearchDto empty () {
		return build();
	}
	
	public static SearchDto build( Set<String> potentials ) {
		SearchDto newDto = build();
		newDto.setPotentials(potentials);
		return newDto;
	}
	
	public static SearchDto empty ( String service, String level, String message ) {
		SearchDto newDto = build();
		ApiMessage apiM = new ApiMessage();
		apiM.setLevel(level); apiM.setMessage(message);
		newDto.add(service, apiM);
		return newDto;
	}
	
	public static SearchDto error ( String service, String error, String message ) {
		SearchDto newDto = build();		
		newDto.add(service, ApiMessage.error(error).setMessage(message) );
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

	public Set<String> getPotentials() {
		return potentials;
	}

	public void setPotentials(Set<String> potentials) {
		this.potentials = potentials;
	}

	@JsonAnyGetter
	public Map<String, ApiMessage> getApiMessages() {
		return apiMessages;
	}

	public void setApiMessages(Map<String, ApiMessage> apiMessages) {
		this.apiMessages = apiMessages;
	}
	
	public SearchDto fromRestMessage ( RestMessage restMessage ) {
		restMessage.getMessages().forEach( (k,v) -> {
			this.apiMessages.put(k,ApiMessage.from(v));
		});
		return this;
	}
	
	public Map<String, Set<String>> getMatches() {
		return matches;
	}

	public void setMatches(Map<String, Set<String>> matches) {
		this.matches = matches;
	}

	public boolean isError () {
		for ( ApiMessage message : apiMessages.values() ) {
			if ( message.getLevel().equals("ERROR")) return true;
		}
		return false;		
	}
	
	public boolean isEmpty () {
		return potentials.size() == 0;
	}
	
	/**
	 * Combines the search dtos together using intersection.
	 * 
	 * Potentials must exist in both searchDtos.
	 * 
	 * ApiMessages are added together and saved. Created date is updated.
	 * 
	 * @param searchDto
	 * @return
	 */
	public SearchDto intersect ( SearchDto searchDto ) {
		
		created = LocalDateTime.now();
		
		// find potentials intersection
		potentials = potentials.stream()
				.filter(((Set<String>) searchDto.getPotentials())::contains)
				.collect(Collectors.toSet());
		
		// combine ApiMessages, duplicates should never happen
		// and would likely indicate an error in itself
		
		searchDto.getApiMessages().forEach( (k,v) -> {
			if ( this.apiMessages.containsKey(k)) {
				this.apiMessages.put(k + created.toString(), v); // duplicate is not common
			} else {
				this.apiMessages.put(k,v);
			}
		});
		
		return this;
	}

}
