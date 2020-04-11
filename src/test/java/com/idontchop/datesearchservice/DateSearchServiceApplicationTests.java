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
		String test = testApis.testDirectCall().block();
		
		assertTrue ( test.length() > 0);
	}
	
	@Test
	public void testMono () {
		
		// ok, get two lists to intersect
		Set<String> fromList = Set.of("1","2","username","4","5");
		List<String> aList = List.of("1","2","username","ohhellno");
		List<String> bList = List.of("4","5","username");
		
		Mono<Set<String>> test = Mono.zip(result -> {
			Set<String> set = fromList;
			for ( Object r : result ) {
				if ( r instanceof List<?>) {
					
					set = set.stream()
							.filter(((List<String>) r)::contains)
							.collect(Collectors.toSet());
				}
			}
			return set;
			},
		Mono.just(aList), Mono.just(bList));
		
		Set<String> holyshit = test.block();
		assertEquals ( 1, holyshit.size());
		
		holyshit.forEach(System.out::println);
		assertTrue(holyshit.stream().allMatch( s -> List.of("username").contains(s)));
		
	}
	
	@Test
	public void testAbstractApis () throws IOException {
		
		String username = "username";
		List<String> potentials = 
				jsonExtraction.userListFromLocation(testApis.testLocationSearch().block());
		
		assertTrue ( potentials.size() > 0);
		assertTrue ( potentials.contains("22"));
		
		MicroServiceApiAbstract genderApi = (MicroServiceApiAbstract)
				context.getBean(MicroService.GENDER.getClassName()); 
		
		
		List<String> newPotentials = 
				genderApi.reduce(username, potentials).block();
		
		assertEquals(5, newPotentials.size());
		
		newPotentials.forEach( e -> {
			logger.debug("newPotentials: " + e);
		});
		
	}
	
	@Test
	public void testHelloWorlds() throws InterruptedException {
		
		Flux<String> apis = testApis.helloWorlds();
		
		
		apis.subscribe( e -> {
			logger.debug(e);
		}, e -> e.getMessage() );
		
		Thread.sleep(1000);
	}

}
