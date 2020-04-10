package com.idontchop.datesearchservice.dtos;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

public class ReduceRequest {
	
	public ReduceRequest () {
		
	}
	
	public ReduceRequest ( String name, List<String> potentials) {
		this.name = name;
		this.potentials = potentials;
	}

	@NotBlank
	String name;
	
	// This list is some potential options the api has considered.
	// Using the name, the service will check the potentials against the interestedins
	@NotEmpty(message = "Need at least one potential")
	List<String> potentials;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getPotentials() {
		return potentials;
	}

	public void setPotentials(List<String> potentials) {
		this.potentials = potentials;
	}
	
	
}
