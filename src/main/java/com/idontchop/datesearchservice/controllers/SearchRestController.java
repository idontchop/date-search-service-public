package com.idontchop.datesearchservice.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.idontchop.datesearchservice.api.TestApis;
import com.idontchop.datesearchservice.dtos.RestMessage;

import reactor.core.publisher.Mono;

@Component
public class SearchRestController {

	@Autowired
	TestApis testApis;
	
	
	public Mono<ServerResponse> searchTest (ServerRequest r) {
		return ServerResponse
				.ok()
				.body(testApis.testLocationSearch(),String.class);
	}
}
