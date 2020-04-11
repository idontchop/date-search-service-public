package com.idontchop.datesearchservice.api;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.idontchop.datesearchservice.config.enums.MicroService;
import com.idontchop.datesearchservice.dtos.ReduceRequest;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;

import reactor.core.publisher.Mono;

/**
 * Superclass for setting up API calls to microservices.
 * 
 * Most services should conform to at least these methods:
 * 
 *  reduce - accepts a list of potentials
 *  
 * @author nathan
 *
 */
@Component
public abstract class MicroServiceApiAbstract {
	
	private MicroService microService;	// set in constructor of subclass
	
	protected String apiReduceExt = 	"/api/reduce"; 	// can be overridden by subclass if necessary
													// but should try to standardize this
	
	private EurekaClient discoveryClient;
	@Autowired public final void setEurekaClient(EurekaClient discoveryClient) {
		this.discoveryClient = discoveryClient;
	}
	
	@Autowired
	private WebClient.Builder webClientBuilder;
	
	public MicroServiceApiAbstract (MicroService microService) throws IOException, RuntimeException {
		
		// microService enum should be set by subclass constrcutor
		this.microService = microService;
		
		if ( microService == null ) {
			throw new IOException ("MicroServiceApiAbstract: microService not set in constructor");
		} 
	}
	
	/**
	 * Returns proper info from eureka client for requested microservice.
	 * 
	 * @return
	 */
	private InstanceInfo getServiceInfo () {
		return discoveryClient
			.getNextServerFromEureka(microService.getName(), false);
	}
	
	/**
	 * @see MicroServiceApiAbstract#reduce(ReduceRequest)
	 */
	public Mono<List<String>> reduce (String username, List<String> potentials) {
		
		// The DTO for communication with microservice
		ReduceRequest reduceRequest = new ReduceRequest(username, potentials);
		
		return reduce (reduceRequest);
	}
	
	/**
	 * Standard method for calling the MicroServices' reduce api.
	 * 
	 * The contract here is sending a class with a username and a list of "potentials"
	 * The Microservice will reduce the list of potentials based on its data.
	 * 
	 * For example, the gender-service will reduce the potentials list to users of the proper
	 * genders.
	 * 
	 * For simple reducerequests, username + potential can be sent, but for others
	 * where the service needs to know the variables, a reducerequest must be sent
	 * 
	 * @param username
	 * @param potentials
	 * @return
	 */
	public Mono<List<String>> reduce ( ReduceRequest reduceRequest ) {
		
		// Use enum to find the proper microservice from Eureka
		WebClient webClient = WebClient.create( getServiceInfo().getHomePageUrl() );
		
		// API call will return a Mono with a new List of potentials
		return webClient.post().uri( uriBuilder -> uriBuilder.path(apiReduceExt).build(0) )
				.bodyValue(reduceRequest)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<String>>() {} );
	}
	
}
