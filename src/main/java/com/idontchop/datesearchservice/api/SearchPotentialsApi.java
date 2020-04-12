package com.idontchop.datesearchservice.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.idontchop.datesearchservice.dtos.SearchRequest;

/**
 * This will call a sequence of microservices in order to build a SearchDto
 * 
 * The sequence is built usually according to a SearchRequest
 * 
 *  After the sequence is built, a zipped Mono is returned with the results.
 * 
 * @see SearchDto
 * @see SearchRequest
 *  
 * @author nathan
 *
 */
@Service
public class SearchPotentialsApi {
	
	public SearchPotentialsApi () {};
	
	// Called to get the intial list (Location)
	private MicroServiceApiAbstract baseApiCall;
	
	// Called to reduce the potentials obtained from baseApiCall
	// This class sets required reduce calls [blocks, hides] (can be overridden with noBlockCall)
	private List<MicroServiceApiAbstract> reduceCalls = new ArrayList<>();
	
	/*
	 * Build methods
	 */
	
	public static SearchPotentialsApi build() {
		SearchPotentialsApi spa = new SearchPotentialsApi();
		return spa;
	}
	
	public static SearchPotentialsApi from (SearchRequest searchRequest ) {
		SearchPotentialsApi spa = build();
		
		return spa;
	}
	

}
