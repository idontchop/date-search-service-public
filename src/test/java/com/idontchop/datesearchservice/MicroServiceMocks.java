package com.idontchop.datesearchservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idontchop.datesearchservice.api.JsonExtraction;
import com.idontchop.datesearchservice.api.MicroServiceApiAbstract;
import com.idontchop.datesearchservice.api.TestApis;
import com.idontchop.datesearchservice.api.microservices.LocationServiceApi;
import com.idontchop.datesearchservice.config.enums.MicroService;
import com.idontchop.datesearchservice.dtos.RestMessage;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * This test class will mock all MicroServices as necessary.
 * 
 * @author micro
 *
 */
@ActiveProfiles("test")
@SpringBootTest
public class MicroServiceMocks {
	
	Logger logger = LoggerFactory.getLogger(MicroServiceMocks.class);
	
	@Autowired
	Environment env;
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	JsonExtraction jsonExtraction;
	
	public static MockWebServer mockServices;
	
	public static Map<MicroService, MockWebServer> micros;
	
	@Autowired
	TestApis testApis;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@BeforeAll
	static void setup() throws IOException {
		mockServices = new MockWebServer();
		mockServices.start(62698);
		
		micros = new HashMap<>();
		for (MicroService m : MicroService.values()) {
			
			micros.put(m, new MockWebServer());
			micros.get(m).start(Integer.parseInt(m.getTestAddress().replaceAll("\\D+","")));
		}
	}
	
	@AfterAll
	static void shutdown() throws IOException {
		mockServices.shutdown();
		
		micros.forEach( (k,v) -> {
			try {
				v.shutdown();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
	
	@Test
	void contextLoads() {
		MicroServiceApiAbstract mapi = (MicroServiceApiAbstract) context.getBean("AgeServiceApi");
		assertEquals("62906",MicroService.AGE.getTestAddress().replaceAll("\\D+",""));
		assertEquals("localhost:62906/test-age", mapi.getServiceAddress());
		
		assertEquals("test", env.getProperty("spring.profiles.active"));
		assertEquals("active", env.getProperty("date.test"));
		assertNotNull(env.getProperty("eureka.instance.hostname")); 
		assertNotNull(env.getProperty("eureka.client.registerWithEureka"));
		assertNotNull(mockServices.getPort());
	}
	
	@Test
	void testLocationSearch () throws JsonProcessingException, InterruptedException {
		
		String testApiString = "test api";
		
		RestMessage restMessage = RestMessage.build("Test From test helloWorld");
		RestMessage restMessage2 = RestMessage.build("Test From test helloWorld");
		RestMessage badRestMessage = RestMessage.build("Not Good");
		
		assertTrue( restMessage.equals(restMessage2));
		assertFalse( restMessage.equals(badRestMessage));
		
		mockServices.enqueue( new MockResponse()
				.setBody("test api") );
		
		WebClient webClient = WebClient.create("http://localhost:62698/test-location/api/search-location/LOC,HOME/34.001/114.001/500");
		Mono<String> test = webClient.get().retrieve().bodyToMono(String.class);
		
		assertEquals(62698, mockServices.getPort());
		
		
		assertEquals(testApiString, test.block());
		
		RecordedRequest rr = mockServices.takeRequest();
		assertEquals("GET", rr.getMethod());
		assertEquals("/test-location/api/search-location/LOC,HOME/34.001/114.001/500", rr.getPath());
		
		mockServices.enqueue( new MockResponse()
				.setBody(objectMapper.writeValueAsString(restMessage))
				.addHeader("Content-type", "application/json"));

		Mono<RestMessage> rm = testApis.testDirectCall();
		
		//assertEquals (restMessage, rm.block());
		StepVerifier.create(rm)
			.expectNext(restMessage)
			.verifyComplete();
	
	}
	
	@Test
	void testReduce() {
		
		MockWebServer baseCall = micros.get(MicroService.LOCATION);
		
		
		
	}
	


}
