package com.idontchop.datesearchservice.dtos;

/**
 * Used for DTOs where choices obtained from a service.
 * 
 * Mainly trait selections from profile service.
 * 
 * @author nathan
 *
 */
public class GenericSelector {

	// should cover the necessary information needed
	private long id;
	private String name;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
}
