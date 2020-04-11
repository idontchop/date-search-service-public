package com.idontchop.datesearchservice.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.idontchop.datesearchservice.config.enums.MicroService;
import com.idontchop.datesearchservice.dtos.ReduceRequest;
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
	
	@Autowired
	private WebClient.Builder webClientBuilder;
	
	
	/**
	 * Returns proper info from eureka client for requested microservice.
	 * 
	 * @return
	 */
	public InstanceInfo getServiceInfo (MicroService microService) {
		return discoveryClient
			.getNextServerFromEureka(microService.getName(), false);
	}
	
	public Applications getServices() {
		return discoveryClient.getApplications();
	}
	
	public Flux<String> helloWorlds () {
		
		List<Mono<String>> monoList =
		getServices().getRegisteredApplications().stream().map( elem -> {
			String serviceName = elem.getName();
			String baseUrl = "http://" + discoveryClient.getNextServerFromEureka(serviceName, false).getAppName();
			WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();
			Mono<String> newMono = webClient.get().uri( uriBuilder -> uriBuilder.path("/helloWorld").build() )
					.exchange()
					.flatMap ( response -> {
						if ( response.statusCode().is2xxSuccessful() ) {
							return response.bodyToMono(String.class);
					} else {
						return Mono.just("404 \"" + baseUrl + "helloWorld\"");
					}});
					
			return newMono;
		}).collect ( Collectors.toList());
		
		return Flux.merge(monoList);
	}
	
	public Mono<String> testLocationSearch () {
		String testurl = "/api/search-location/LOC,HOME/34.001/114.001/500";
		
		WebClient webClient = WebClient.create(getServiceInfo(MicroService.LOCATION).getHomePageUrl());
		
		return webClient.get().uri(uriBuilder -> uriBuilder.path(testurl).build(0) )
			.retrieve().bodyToMono(String.class);
	}
	
	public Mono<String> testDirectCall () {
		
		String testurl = "http://MEDIA-SERVICE/helloWorld";
		
		WebClient webClient = webClientBuilder.build();
		
		return webClient.get().uri(testurl).retrieve().bodyToMono(String.class);
	}
	
	public Mono<List<String>> reduceGender (String username, List<String> userList) {
		String reduceGenderApi = "/api/reduce";
		
		ReduceRequest reduceRequest = new ReduceRequest(username, userList);
		
		WebClient webClient = WebClient.create(getServiceInfo(MicroService.GENDER).getHomePageUrl());
		
		return webClient.post().uri( uriBuilder -> uriBuilder.path(reduceGenderApi).build(0) )
				.bodyValue(reduceRequest)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<String>>() {} );
	}

}
