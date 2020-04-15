package com.idontchop.datesearchservice.api;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.idontchop.datesearchservice.api.microservices.LocationServiceApi;
import com.idontchop.datesearchservice.config.enums.MicroService;
import com.idontchop.datesearchservice.dtos.ApiMessage;
import com.idontchop.datesearchservice.dtos.ReduceRequest;
import com.idontchop.datesearchservice.dtos.RestMessage;
import com.idontchop.datesearchservice.dtos.SearchDto;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Applications;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Testing calls to other microservices.
 * 
 * Plan to use fragments from here.
 * 
 * @author nathan
 *
 */
@Service
public class TestApis {
	
	Logger logger = LoggerFactory.getLogger(TestApis.class);
	
	@Autowired
	private EurekaClient discoveryClient;
	
	@Autowired
	private WebClient.Builder webClientBuilder;
	
	@Autowired
	private ApplicationContext context;
	
	
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
	
	/**
	 * Returns the HelloWorlds of the various microservices.
	 * 
	 * Largely used to test various patterns.
	 * 
	 * @return
	 */
	public Flux<SearchDto> helloWorlds () {
		
		List<Mono<SearchDto>> monoList =
		getServices().getRegisteredApplications().stream().map( elem -> {
			String serviceName = elem.getName();
			String baseUrl = "http://" + discoveryClient.getNextServerFromEureka(serviceName, false).getAppName();
			WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();
			Mono<SearchDto> newMono = webClient.get().uri( uriBuilder -> uriBuilder.path("/helloWorld").build() )
					.exchange()
					.flatMap ( response -> convertHelloWorldBody(response,serviceName) )
					.doOnError( ex -> logger.debug(ex.getMessage()))
					.onErrorResume( ex -> handleHelloWorldError(ex, serviceName));
			return newMono;
		}).collect ( Collectors.toList());
		
		return Flux.merge(monoList);
	}
	
	private Mono<SearchDto> handleHelloWorldError(Throwable ex, String serviceName) {
		
		return Mono.just(SearchDto.empty(serviceName,ex.getClass().getName(), ex.getMessage()));
	}

	private Mono<SearchDto> convertHelloWorldBody (ClientResponse response, String serviceName) {
		// https://github.com/codecentric/spring-boot-admin/blob/master/spring-boot-admin-server/src/main/java/de/codecentric/boot/admin/server/services/StatusUpdater.java#L64
		// create a statusinfo class to hold response info
		// Have searchdto store the statusinfos and lists 	
		boolean hasContent = response.headers()	// TODO: get content for error as well, maybe reasonphrase enough
					.contentType()
					.map( (mt) -> mt.isCompatibleWith(MediaType.APPLICATION_JSON))
					.orElse(false);
			
			// return the body as a RestMessage
		if ( hasContent && response.statusCode().is2xxSuccessful() ) {
				return response.bodyToMono(RestMessage.class).map( body -> {
					return SearchDto.build().fromRestMessage(body);
				});
		} else {
			return Mono.just(SearchDto.empty(serviceName,Integer.toString(response.statusCode().value()),
					response.statusCode().getReasonPhrase() ) );
			// return the error code and message
			//return Mono.just(RestMessage.build(serviceName)
			//		.add(Integer.toString(response.rawStatusCode()),
			//				response.statusCode().getReasonPhrase()));
		}
	}
	
	public Mono<String> testLocationSearch () {
		//String testurl = "/api/search-location/LOC,HOME/34.001/114.001/500";
		LocationServiceApi locationApi;

			locationApi = (LocationServiceApi) context
					.getBean(MicroService.LOCATION.getClassName());

		
		return locationApi.baseSearch("LOC,HOME","34.001","114.001","500");
		
	}
	
	public Mono<RestMessage> testDirectCall () {
		
		String testurl = "http://localhost:62698/helloWorld";
		
		WebClient webClient = WebClient.builder().build();
		
		return webClient.get().uri(testurl).retrieve().bodyToMono(RestMessage.class);
	}
	
	public Mono<String> testCall () {
		
		WebClient.Builder webClientBuilder = WebClient.builder();
		WebClient webClient;
		try {
			webClient = webClientBuilder
				.baseUrl("http://" + MicroService.LOCATION.getTestAddress()).build();
		} catch ( RuntimeException ex ) {
			return Mono.error(ex);
		}
		
		String finalUrlExt = "LOC";
		
		return webClient.get()
				.uri( uriBuilder -> uriBuilder.path(finalUrlExt).build(0) )
				.retrieve().bodyToMono(String.class);
	}
	
	public Mono<String> testOk() {
		
		LocationServiceApi locationApi;

		locationApi = (LocationServiceApi) context
				.getBean(MicroService.LOCATION.getClassName());

	
	return locationApi.baseSearch("loc");
	
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
