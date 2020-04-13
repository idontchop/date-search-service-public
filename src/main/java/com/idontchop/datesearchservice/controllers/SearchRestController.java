package com.idontchop.datesearchservice.controllers;


import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.idontchop.datesearchservice.api.TestApis;
import com.idontchop.datesearchservice.dtos.RestMessage;
import com.idontchop.datesearchservice.dtos.SearchDto;
import com.idontchop.datesearchservice.dtos.SearchRequest;
import com.idontchop.datesearchservice.api.SearchPotentialsApi;

import reactor.core.publisher.Mono;

@Component
public class SearchRestController {

	@Autowired
	TestApis testApis;
	@Autowired
	ApplicationContext applicationContext;
	
	
	public Mono<ServerResponse> searchTest (ServerRequest r) {
		return ServerResponse
				.ok()
				.body(testApis.testLocationSearch(),String.class);
	}
	
	public Mono<ServerResponse> reduceTest ( ServerRequest r ) {
		
		return r.bodyToMono(SearchRequest.class)
				.flatMap( sr -> 
		 			SearchPotentialsApi.from(sr, applicationContext)
		 				.run()
		 				.flatMap( searchDto -> {
		 					return ServerResponse.ok()
		 						.contentType(MediaType.APPLICATION_JSON)
		 						.bodyValue(searchDto);
		 			}));
	}
	
}
