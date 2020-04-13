package com.idontchop.datesearchservice.dtos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FullReduceRequest extends ReduceRequestWithAge {

	// For profile traits
	private List<GenericSelector> selections = new ArrayList<>();
	
	public FullReduceRequest(String username, Set<String> potentials) {
		super(username, potentials);
	}

	public List<GenericSelector> getSelections() {
		return selections;
	}

	public void setSelections(List<GenericSelector> selections) {
		this.selections = selections;
	}

}
