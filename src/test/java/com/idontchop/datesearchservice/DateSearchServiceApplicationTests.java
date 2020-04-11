package com.idontchop.datesearchservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.idontchop.datesearchservice.api.JsonExtraction;
import com.idontchop.datesearchservice.api.MicroServiceApiAbstract;
import com.idontchop.datesearchservice.api.TestApis;
import com.idontchop.datesearchservice.api.microservices.GenderServiceApi;
import com.idontchop.datesearchservice.config.enums.MicroService;
import com.idontchop.datesearchservice.dtos.ReduceRequest;
import com.idontchop.datesearchservice.dtos.ReduceRequestWithAge;
import com.idontchop.datesearchservice.dtos.RestMessage;
import com.idontchop.datesearchservice.dtos.SearchDto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootTest
class DateSearchServiceApplicationTests {
	
	Logger logger = LoggerFactory.getLogger(DateSearchServiceApplicationTests.class);

	@Autowired
	TestApis testApis;
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private JsonExtraction jsonExtraction;
	
	@Test
	void contextLoads() {
	}
	
	@Test
	public void testDirectCall () {
		RestMessage test = testApis.testDirectCall().block();
		
		assertTrue ( test.getMessages().get("message") != null);
	}
	
	@Test
	public void testMono () {
		
		// ok, get two lists to intersect
		Set<String> fromList = Set.of("1","2","username","4","5");
		List<String> aList = List.of("1","2","username","ohhellno");
		List<String> bList = List.of("4","5","username");
		
		Mono<Set<String>> test = Mono.zip(
			List.of(Mono.just(aList), Mono.just(bList)),
			result -> {
			Set<String> set = fromList;
			for ( Object r : result ) {
				if ( r instanceof List<?>) {
					
					set = set.stream()
							.filter(((List<String>) r)::contains)
							.collect(Collectors.toSet());
				}
			}
			return set;
			});
		
		
		Set<String> holyshit = test.block();
		assertEquals ( 1, holyshit.size());
		
		holyshit.forEach(System.out::println);
		assertTrue(holyshit.stream().allMatch( s -> List.of("username").contains(s)));
		
	}
	
	@Test
	public void testAbstractApis () throws IOException {
		
		String username = "username";
		Set<String> potentials = 
				jsonExtraction.userListFromLocation(testApis.testLocationSearch().block());
		
		assertTrue ( potentials.size() > 0);
		assertTrue ( potentials.contains("22"));
		
		MicroServiceApiAbstract genderApi = (MicroServiceApiAbstract)
				context.getBean(MicroService.GENDER.getClassName()); 
		MicroServiceApiAbstract ageApi = (MicroServiceApiAbstract)
				context.getBean(MicroService.AGE.getClassName());
		MicroServiceApiAbstract blockApi = (MicroServiceApiAbstract)
				context.getBean(MicroService.BLOCK.getClassName());
		
		// gender
		SearchDto newPotentials = 
				genderApi.reduce(username, potentials).block();
		
		assertEquals(5, newPotentials.getPotentials().size());
		
		newPotentials.getPotentials().forEach( e -> {
			logger.debug("newPotentials: " + e);
		});
		
		// Age
		ReduceRequestWithAge reduceRequestWithAge = new ReduceRequestWithAge(username, newPotentials.getPotentials());		
		
		reduceRequestWithAge.setMaxAge(80);
		reduceRequestWithAge.setMinAge(5);
		
		newPotentials =
				ageApi.reduce(reduceRequestWithAge).block();
		
		assertEquals(2, newPotentials.getPotentials().size());
		
		newPotentials = blockApi.reduce(username, newPotentials.getPotentials()).block();
		
		assertEquals(1, newPotentials.getPotentials().size());
		assertTrue(newPotentials.getPotentials().contains("30"));
		
		
	}
	
	@Test
	public void testZip () throws IOException {
		
		String username = "username";
		Set<String> potentials = 
				jsonExtraction.userListFromLocation(testApis.testLocationSearch().block());
		
		assertTrue ( potentials.size() > 0);
		assertTrue ( potentials.contains("22"));
		
		MicroServiceApiAbstract genderApi = (MicroServiceApiAbstract)
				context.getBean(MicroService.GENDER.getClassName()); 
		MicroServiceApiAbstract ageApi = (MicroServiceApiAbstract)
				context.getBean(MicroService.AGE.getClassName());
		MicroServiceApiAbstract blockApi = (MicroServiceApiAbstract)
				context.getBean(MicroService.BLOCK.getClassName());
		
		List<Mono<SearchDto>> newPotentials = new ArrayList<>();
		// gender
		newPotentials.add(
				genderApi.reduce(username, potentials));		
		// Age
		ReduceRequestWithAge reduceRequestWithAge = new ReduceRequestWithAge(username, potentials);
		reduceRequestWithAge.setAge(5,80);
		newPotentials.add(
				ageApi.reduce(reduceRequestWithAge));
		// block
		newPotentials.add(
				blockApi.reduce(username,potentials));
		
		SearchDto result = Mono.zip(newPotentials, resultMonos -> {
			if ( resultMonos.length == 1 ) return (SearchDto) resultMonos[0];
			else {
				for ( int i = 1; i < resultMonos.length; i++) {
					((SearchDto) resultMonos[0]).intersect((SearchDto) resultMonos[i]);
				}
				return (SearchDto) resultMonos[0];
			}
		}).block();
		
		assertTrue( result.getPotentials().contains("30"));
				
		/*
		 * 		Mono<Set<String>> test = Mono.zip(
			List.of(Mono.just(aList), Mono.just(bList)),
			result -> {
			Set<String> set = fromList;
			for ( Object r : result ) {
				if ( r instanceof List<?>) {
					
					set = set.stream()
							.filter(((List<String>) r)::contains)
							.collect(Collectors.toSet());
				}
			}
			return set;
			});
		 */
	}
	
	@Test
	public void testHelloWorlds() throws InterruptedException {
		
		Flux<SearchDto> apis = testApis.helloWorlds();
		
		
		apis.subscribe( e -> {
			logger.debug(e.toString());
		}, e -> e.getMessage() );
		
		Thread.sleep(1000);
	}

}
