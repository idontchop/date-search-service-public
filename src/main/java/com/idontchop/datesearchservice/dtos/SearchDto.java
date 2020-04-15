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
	
	public static SearchDto build ( Iterable<String> potentials ) {
		SearchDto newDto = build();
		potentials.forEach(newDto.getPotentials()::add);
		return newDto;
	}
	
	public static SearchDto build ( Set<String> potentials, Set<String> matches, String matchName ) {
		SearchDto newDto = build();
		newDto.setPotentials(potentials);
		newDto.setNewMatch(matchName, matches);
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
		
	/**
	 * Adds an ApiMessage. If the name already exists, will be added with timestamp.
	 * 
	 * @param name the key
	 * @param newApiMessage
	 * @return
	 */
	public SearchDto add (String name, ApiMessage newApiMessage ) {
		if ( apiMessages.containsKey(name)) {
			apiMessages.put( name + "@" + LocalDateTime.now().toString(), newApiMessage);
		} else {
			apiMessages.put(name, newApiMessage);
		}
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
	
	public void setNewMatch ( String name, Set<String> matches ) {
		this.matches.put(name, matches);
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
	
	/**
	 * Combines the dtos by absorbing all potentials from the parameter
	 * into a new match entry.
	 * 
	 * Use primarily to add a search dto from a matches request.
	 * 
	 * @param searchDto
	 * @return
	 */
	public SearchDto absorbMatch ( SearchDto searchDto, String name ) {
		
		created = LocalDateTime.now();
		
		// if already contains the key, list the error
		if ( this.getMatches().containsKey(name)) {
			this.add(name, ApiMessage.error( name + " match exists, writing with timestamp."));
			this.getMatches().put(name + "@" + LocalDateTime.now(), searchDto.getPotentials());
		} else {
			this.getMatches().put(name, searchDto.getPotentials());
		}
		// catch error, create message, but add anyway
		searchDto.getMatches().forEach( (k,v) -> {
			if (this.matches.containsKey(k)) {
				this.add(name, ApiMessage.error(k + " match exists. Writing as " + k + created.toString()));
				this.matches.put(name, searchDto.getPotentials());
			} else {
				this.matches.put(name, searchDto.getPotentials());
			}			
		});
		
		// combine ApiMessages, duplicates should never happen
		// and would likely indicate an error in itself
		
		searchDto.getApiMessages().forEach( (k,v) -> {
			this.add(k,v);			
		});
		
		return this;
		
	}

}
