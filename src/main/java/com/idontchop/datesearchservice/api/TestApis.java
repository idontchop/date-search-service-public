package com.idontchop.datesearchservice.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Applications;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Testing calls to other microservices.
 * 
 * @author nathan
 *
 */
@Service
public class TestApis {
	
	@Autowired
	private EurekaClient discoveryClient;
	
	
	
	public InstanceInfo getLocationInfo () {
		InstanceInfo locationService = discoveryClient.getNextServerFromEureka("location-service", false);
		return locationService;
	}
	
	public Applications getServices() {
		return discoveryClient.getApplications();
	}
	
	public Flux<String> helloWorlds () {
		
		List<Mono<String>> monoList =
		getServices().getRegisteredApplications().stream().map( elem -> {
			String serviceName = elem.getName();
			String baseUrl = discoveryClient.getNextServerFromEureka(elem.getName(), false).getHomePageUrl();
			WebClient webClient = WebClient.create(baseUrl);
			Mono<String> newMono = webClient.get().uri( uriBuilder -> uriBuilder.path("/helloWorld").build() )
					.retrieve().bodyToMono(String.class);
			return newMono;
		}).collect ( Collectors.toList());
		
		
	}
	
	public Mono<String> testLocationSearch () {
		String testurl = "/api/search-location/LOC,HOME/34.001/114.001/500";
		
		WebClient webClient = WebClient.create(getLocationInfo().getHomePageUrl());
		
		return webClient.get().uri(uriBuilder -> uriBuilder.path(testurl).build(0) )
			.retrieve().bodyToMono(String.class);
	}

}
