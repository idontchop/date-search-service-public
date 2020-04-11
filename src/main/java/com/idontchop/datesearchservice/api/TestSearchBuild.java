package com.idontchop.datesearchservice.api;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class TestSearchBuild {
	
	@Autowired
	TestApis testApis;
	
	@Autowired
	JsonExtraction jsonExtraction;
	
	/**
	 * Testing functionality of a service that will build a list of matches
	 * by calling apis in the microservice architecture.
	 * 
	 * 1) 	Blocking call to Location to get a list of possibles.
	 * 2) 	Non-blocking simultaneous calls to other microservices to
	 * 		filter the list returned from location.
	 * 3)	Contract profile information of matches with a 3rd api call
	 * 4)	Return full info.
	 * 
	 * @return
	 */
	public Set<String> testSearchLocation () throws IOException {
		
		return jsonExtraction.userListFromLocation(
				testApis.testLocationSearch().block());
	}
	
	public Mono<List<String>> reduce (List<String> potentials) {
		
		String testUser = "username";
		
		return null;
		//return Mono.zip
			//testApis.reduceGender(testUser, potentials),
			//Mono.just(List.of("username"));
			
	}

}
