package com.idontchop.datesearchservice.api;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.idontchop.datesearchservice.config.enums.MicroService;
import com.idontchop.datesearchservice.dtos.ReduceRequest;
import com.idontchop.datesearchservice.dtos.RestMessage;
import com.idontchop.datesearchservice.dtos.SearchDto;
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
	
	protected Logger logger = LoggerFactory.getLogger(MicroServiceApiAbstract.class);
	
	protected MicroService microService;	// set in constructor of subclass
	
	protected String apiMatchExt =		"/api/match";	// can be overridden by subclass if necessary
	protected String apiReduceExt = 	"/api/reduce"; 	// but should try to standardize this													
	
	protected String apiBaseExt  =		"/api/baseSearch";	// The api call to get a fresh list
	
	// Discovery bean
	private EurekaClient discoveryClient;
	@Autowired public final void setEurekaClient(EurekaClient discoveryClient) {
		this.discoveryClient = discoveryClient;
	}
	
	@Value("${spring.profiles.active}")
	private String activeProfile;

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
	private InstanceInfo getServiceInstanceInfo () throws RuntimeException {
		return discoveryClient
			.getNextServerFromEureka(microService.getName(), false);
	}
	
	/**
	 * Provides a switch on tope of InstanceInfo to return a test url in the case of:
	 * 
	 * 1) Eureka Client is down
	 * 2) Active Profile = test
	 * 
	 * @return
	 * @throws RunTimeException
	 */
	public String getServiceAddress () throws RuntimeException {
		
		try {
			return getServiceInstanceInfo().getAppName();
		} catch ( RuntimeException ex ) {
			if ( activeProfile.equals("test") ) {
				return microService.getTestAddress();
			} else throw new RuntimeException( ex.getMessage() + " " + activeProfile);
		}
	}
	
	/**
	 * Returns proper web client. Mainly returns load balanced in production.
	 * 
	 * Regular webclient when in test.
	 * 
	 * @param url
	 * @return
	 * @throws RuntimeException
	 */
	private WebClient getWebClient (  ) throws RuntimeException {
		
		if ( activeProfile.equals("test") ) {
			return WebClient.builder().baseUrl("http://" + getServiceAddress()).build();
		} else {
			// production web client
			return webClientBuilder.baseUrl("http://" + getServiceAddress()).build();
		}
	}
	
	/**
	 * @see MicroServiceApiAbstract#reduce(ReduceRequest)
	 */
	public Mono<SearchDto> reduce (String username, Set<String> potentials) {
		
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
	public Mono<SearchDto> reduce ( ReduceRequest reduceRequest ) {
		
		// Use enum to find the proper microservice from Eureka
		WebClient webClient;
		try {
			webClient = getWebClient();
		} catch (RuntimeException ex ) {
			// reason: Service not registered
			return handleResponseError(ex);
		}
		
		// API call will return a Mono with a new List of potentials
		return webClient.post().uri( uriBuilder -> uriBuilder.path(apiReduceExt).build(0) )
				.bodyValue(reduceRequest)
				.exchange()
				.flatMap( response -> convertResponseBody(response) )
				.doOnError( ex -> logger.debug(ex.getMessage()))
				.onErrorResume( ex -> handleResponseError(ex) );
	}
	
	/**
	 * Calls a microservice and sets matches in a reduce request. This call
	 * doesn't manipulate the list of potentials, and instead creates a new entry
	 * in the SearchDto's matches.
	 * 
	 * @param reduceRequest
	 * @return
	 */
	public Mono<SearchDto> addMatch ( ReduceRequest reduceRequest ) {
		
		// Use enum to find the proper microservice from Eureka
		WebClient webClient;
		try {
			webClient = getWebClient();
		} catch (RuntimeException ex ) {
			// reason: Service not registered
			return handleResponseError(ex);
		}
		
		// API call will return a new SearchDto which will be mapped back to searchdto with
		// full potentials
		return webClient.post().uri( uriBuilder -> uriBuilder.path(apiMatchExt).build(0) )
				.bodyValue(reduceRequest)
				.exchange()
				.flatMap( response -> convertResponseBody(response) )
				.flatMap( searchDto -> {		// here we convert this dto so it can be easily zipped with other reduces
					SearchDto newDto = SearchDto.build(reduceRequest.getPotentials());	// build with original potentails
					newDto.absorbMatch(searchDto, microService.getName());				// absorb the match
					return Mono.just(newDto);		
				})
				.doOnError( ex -> logger.debug(ex.getMessage()))
				.onErrorResume( ex -> handleResponseError(ex) );
				
	}
	
	/**
	 * Handles converting the responsebody in a webclient request to the microservice.
	 * 
	 * Will return the potentials as well as any messages received (not implemented).
	 * 
	 * In case of an error, potentials will be returned empty and an apimessage will be
	 * set.
	 * 
	 * @param response
	 * @return
	 */
	public Mono<SearchDto> convertResponseBody ( ClientResponse response ) {
		
		boolean hasContent = response.headers()	// TODO: get content for error as well, maybe reasonphrase enough
				.contentType()
				.map( (mt) -> mt.isCompatibleWith(MediaType.APPLICATION_JSON))
				.orElse(false);
		
			// return the body as a RestMessage
		if ( hasContent && response.statusCode().is2xxSuccessful() ) {
				return response.bodyToMono(new ParameterizedTypeReference<Set<String>>() {})
						.map( body -> {
							return SearchDto.build(body);
						});
		} else {
			return Mono.just(	// sets empty potentials and ERROR ApiMessage
						SearchDto.error( microService.getName(),
						Integer.toString(response.statusCode().value()),
						response.statusCode().getReasonPhrase() )
						);
		}
	}
	
	/**
	 * Handles WebClient error. Will set a Search DTO with empty potentials and ERROR Api message.
	 * 
	 * @param ex
	 * @return
	 */
	public Mono<SearchDto> handleResponseError ( Throwable ex ) {
		return Mono.just(
				SearchDto.error(microService.getName(), ex.getClass().getName(), ex.getMessage())
				);
	}
	
	/**
	 * Unlike the reduce call, an error in this call will not allow the search process
	 * to continue.
	 * 
	 * @param args
	 * @return
	 */
	public Mono<String> baseSearch ( String... args ) {
		
		// Append args in order on the baseSearch ext and return a string response
		// which the caller can decode.
		
		String callUrlExt = apiBaseExt;
		for ( String s : args ) {
			callUrlExt += ("/" + s);
		}
		
		final String finalUrlExt = callUrlExt;		// for lambda call
		
		// Use enum to find the proper microservice from Eureka
		WebClient webClient;
		try {
			
			// call - error if eureka issue
			webClient = getWebClient();
			
			// API call will return a Mono with a new List of potentials
			return webClient.get().uri( uriBuilder -> uriBuilder.path(finalUrlExt).build(0) )
					.retrieve()
					.bodyToMono(String.class);
			
		} catch ( RuntimeException ex ) {
			return Mono.error(ex);
		}		
	}

	
}
