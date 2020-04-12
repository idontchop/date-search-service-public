package com.idontchop.datesearchservice.dtos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.idontchop.datesearchservice.config.enums.BaseSearch;
import com.idontchop.datesearchservice.config.enums.MicroService;

/**
 * DTO for submission from frontend. Holds all search parameters.
 * 
 * @author nathan
 *
 */
public class SearchRequest {
	
	public SearchRequest () {}

	// Set by front end
	private MicroService baseSearch = MicroService.LOCATION;
	
	// Set by front end and added to by backend (blocks)
	private List<MicroService> reduceSearch = new ArrayList<>();
	
	private double lat;
	private double lng;
	
	private int minAge;
	private int maxAge;
	
	private GenericSelector gender;
	private GenericSelector interestedIn;
	
	private List<String> toggles = new ArrayList<>();		// lists toggles such as "showConnections" if only mutual likes searched for
	
	private List<GenericSelector> selections = new ArrayList<>();

	public MicroService getBaseSearch() {
		return baseSearch;
	}

	public void setBaseSearch(MicroService baseSearch) {
		this.baseSearch = baseSearch;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
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

	public List<GenericSelector> getSelections() {
		return selections;
	}

	public void setSelections(List<GenericSelector> selections) {
		this.selections = selections;
	}

	public List<MicroService> getReduceSearch() {
		return reduceSearch;
	}

	public void setReduceSearch(List<MicroService> reduceSearch) {
		this.reduceSearch = reduceSearch;
	}

	public GenericSelector getGender() {
		return gender;
	}

	public void setGender(GenericSelector gender) {
		this.gender = gender;
	}

	public GenericSelector getInterestedIn() {
		return interestedIn;
	}

	public void setInterestedIn(GenericSelector interestedIn) {
		this.interestedIn = interestedIn;
	}

	public List<String> getToggles() {
		return toggles;
	}

	public void setToggles(List<String> toggles) {
		this.toggles = toggles;
	}
	
	
	
}
