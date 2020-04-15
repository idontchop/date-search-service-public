package com.idontchop.datesearchservice.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.idontchop.datesearchservice.config.enums.MicroService;
import com.idontchop.datesearchservice.dtos.FullReduceRequest;
import com.idontchop.datesearchservice.dtos.SearchDto;
import com.idontchop.datesearchservice.dtos.SearchRequest;

import reactor.core.publisher.Mono;

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
@Component
public class SearchPotentialsApi implements ApplicationContextAware {
	
	Logger logger = LoggerFactory.getLogger(SearchPotentialsApi.class);
	
	@Autowired
	private ApplicationContext context;
	
	public SearchPotentialsApi () {};
	
	// Configures the reduce calls that will always be made
	// can be turned off after Api is built.
	// see reduceApisServices
	final private List<MicroService> alwaysCall = List.of(MicroService.BLOCK);
	
	// Called to get the intial list (Location)
	private MicroServiceApiAbstract baseApiCall;
	private String baseApiArgs[];		// passed to .baseSearch
	
	// Called to reduce the potentials obtained from baseApiCall
	// This class sets required reduce calls [blocks, hides] (can be overridden with noBlockCall)
	private List<MicroServiceApiAbstract> reduceApiCalls = new ArrayList<>();
	private FullReduceRequest reduceRequest;	// built with args before potentials are added.
	
	// Sets the matchs list in SearchDto
	private List<MicroServiceApiAbstract> matchApiCalls = new ArrayList<>();
	
	/*
	 * Build methods
	 */
	
	public static SearchPotentialsApi build() {
		SearchPotentialsApi spa = new SearchPotentialsApi();
		return spa;
	}
	
	public static SearchPotentialsApi from (SearchRequest searchRequest, ApplicationContext applicationContext ) {
		SearchPotentialsApi spa = build();
		
		spa.setApplicationContext(applicationContext);
		
		// build searchBase
		if ( searchRequest.getBaseSearch() == MicroService.LOCATION ) {
			spa.buildLocationBase(
					searchRequest.getLocationTypes(),
					String.valueOf(searchRequest.getLat()),
					String.valueOf(searchRequest.getLng()),
					String.valueOf(searchRequest.getRange()));
		}
		
		spa.reduceApisServices( searchRequest.getReduceSearch() );
		
		// populate our ReduceRequest with relavent args from searchRequest
		spa.reduceRequestFromSearchRequest(searchRequest);
		
		// should be good here to get a working api up.	
		// TODO: handle toggles ( does user want to hide connections? )
		
		
		return spa;
	}
	
	private SearchPotentialsApi reduceRequestFromSearchRequest(SearchRequest searchRequest) {
		this.reduceRequest = new FullReduceRequest(searchRequest.getUsername(), Set.of());
		this.reduceRequest.setMinAge(searchRequest.getMinAge());
		this.reduceRequest.setMaxAge(searchRequest.getMaxAge());
		this.reduceRequest.setSelections(searchRequest.getSelections());
		return this;
	}

	public SearchPotentialsApi baseApi(MicroServiceApiAbstract baseApiCall) {
		this.baseApiCall = baseApiCall;
		return this;
	}
	
	public SearchPotentialsApi reduceApis(List<MicroServiceApiAbstract> reduceApiCalls) {
		this.reduceApiCalls = reduceApiCalls;
		return this;
	}
	
	/**
	 * Builds simple list of reduce api calls. This works since the reduce arguments
	 * are passed with the actual api call
	 * 
	 * 
	 * 
	 * @param services
	 * @return
	 */
	public SearchPotentialsApi reduceApisServices ( List<MicroService> services ) {
		this.reduceApiCalls = services.stream().map( e -> {
			MicroServiceApiAbstract serviceApi = (MicroServiceApiAbstract)
					context.getBean(e.getClassName());
			// specific service code goes here
			return serviceApi;
		}).collect(Collectors.toList());
		
		// Add always calls
		this.reduceApiCalls.addAll(
			alwaysCall.stream().map 
				( e -> (MicroServiceApiAbstract)
						context.getBean(e.getClassName()))
			.collect(Collectors.toList()));
		
		return this;
	}
	
	/**
	 * Attaches a LocacationServiceApi to the base call and provides the Path arguments.
	 * 
	 * @param args
	 * @return
	 */
	public SearchPotentialsApi buildLocationBase ( String... args ) {
		this.baseApi( (MicroServiceApiAbstract) this.context.getBean
				(MicroService.LOCATION.getClassName()));
		this.baseApiArgs = args;
		return this;
	}
	
	/**
	 * Runs an asynchronous sequence of api calls to build the results
	 * for the Potentials Search.
	 * 
	 * @return
	 */
	public Mono<SearchDto> run () {
		
		return doBaseApiCall()
				.flatMap ( potentials -> {
					
					reduceRequest.setPotentials(potentials);					
					return doReduceApiCalls();
				})
				.doOnError(ex -> Mono.error(ex));
		
	}
	
	/**
	 * Second phase in search potentials api call
	 * 
	 * reduces the list of potentials from the base call by calling the reduce
	 * endpoint from other services.
	 * 
	 * @return
	 */
	private Mono<SearchDto> doReduceApiCalls ()  {
		
		return Mono.zip (
				reduceApiCalls.stream().map( e -> e.reduce(reduceRequest)).collect(Collectors.toList()),
				resultMonos -> {  // blocked for completion of all api calls
					
					for ( int i = 1; i < resultMonos.length; i++) {
						// Combines all SearchDtos based on intersect method.
						// intersect requires potentials to exist after every api call
						((SearchDto) resultMonos[0]).intersect((SearchDto) resultMonos[i]);
					}
					return (SearchDto) resultMonos[0];
				});
	}
	
	/**
	 * Returns the intial set of Potentials from the base Api call.
	 * 
	 * @return
	 */
	private Mono<Set<String>> doBaseApiCall () {
		
		JsonExtraction jsonExtraction = context.getBean(JsonExtraction.class);
		
		return baseApiCall.baseSearch(baseApiArgs).flatMap(s -> {
			try {
				return Mono.just(jsonExtraction.userListFromLocation(s));
			} catch (IOException e) {
				return Mono.error(e);
			}
		});
	}

	public MicroServiceApiAbstract getBaseApiCall() {
		return baseApiCall;
	}

	public void setBaseApiCall(MicroServiceApiAbstract baseApiCall) {
		this.baseApiCall = baseApiCall;
	}

	public String[] getBaseApiArgs() {
		return baseApiArgs;
	}

	public void setBaseApiArgs(String[] baseApiArgs) {
		this.baseApiArgs = baseApiArgs;
	}

	public List<MicroServiceApiAbstract> getReduceApiCalls() {
		return reduceApiCalls;
	}

	public void setReduceApiCalls(List<MicroServiceApiAbstract> reduceApiCalls) {
		this.reduceApiCalls = reduceApiCalls;
	}

	public FullReduceRequest getReduceRequest() {
		return reduceRequest;
	}

	public void setReduceRequest(FullReduceRequest reduceRequest) {
		this.reduceRequest = reduceRequest;
	}

	public List<MicroServiceApiAbstract> getMatchApiCalls() {
		return matchApiCalls;
	}

	public void setMatchApiCalls(List<MicroServiceApiAbstract> matchApiCalls) {
		this.matchApiCalls = matchApiCalls;
	}

	public List<MicroService> getAlwaysCall() {
		return alwaysCall;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;		
	}
	

	
}
