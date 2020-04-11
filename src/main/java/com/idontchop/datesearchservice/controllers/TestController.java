package com.idontchop.datesearchservice.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.idontchop.datesearchservice.api.TestApis;
import com.idontchop.datesearchservice.config.enums.MicroService;
import com.idontchop.datesearchservice.dtos.RestMessage;
import com.idontchop.datesearchservice.dtos.SearchDto;
import com.netflix.appinfo.InstanceInfo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Tests for proper production mechanics.
 * 
 * @author nathan
 *
 */
@RestController
public class TestController {

	@Autowired
	TestApis testApi;
	

	
	@GetMapping ("/info")
	public Map<String,Object> getLocation () {
		Map<String,Object> infoMap = new HashMap<>();
		
		infoMap.put("locationInstanceInfo", testApi.getServiceInfo(MicroService.LOCATION));
		infoMap.put("allServices", testApi.getServices());
		return infoMap;
	}
	
	/*
	 * Change this to routes
	 */
	
	
	@GetMapping ("/testLocation")
	public Mono<String> testLocation () {
		return testApi.testLocationSearch();
	}
	
	@GetMapping ("/helloWorlds")
	public Flux<SearchDto> getHelloWorlds() {
		return testApi.helloWorlds();
	}
	
	@GetMapping ("/testDirectCall")
	public Mono<RestMessage> testDirectCall () {
		return testApi.testDirectCall();
	}
}
