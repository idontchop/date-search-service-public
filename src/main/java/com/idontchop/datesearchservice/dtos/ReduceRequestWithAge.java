package com.idontchop.datesearchservice.dtos;

import java.util.List;
import java.util.Set;

public class ReduceRequestWithAge extends ReduceRequest {
	
	private int minAge;
	private int maxAge;
	
	public ReduceRequestWithAge ( String username, Set<String> potentials) {
		super (username, potentials);
	}
	
	public int getMinAge() {
		return minAge;
	}
	public void setMinAge(int minAge) {
		this.minAge = minAge;
	}
	public int getMaxAge() {
		return maxAge;
	}
	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}
	public void setAge(int minAge, int maxAge) {
		this.minAge = minAge;
		this.maxAge = maxAge;
	}
	
	
}
