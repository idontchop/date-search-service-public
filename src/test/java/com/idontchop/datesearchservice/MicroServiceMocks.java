package com.idontchop.datesearchservice;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.idontchop.datesearchservice.api.SearchPotentialsApi;
import com.idontchop.datesearchservice.api.TestApis;
import com.idontchop.datesearchservice.api.microservices.LocationServiceApi;
import com.idontchop.datesearchservice.config.enums.MicroService;
import com.idontchop.datesearchservice.dtos.ReduceRequest;
import com.idontchop.datesearchservice.dtos.RestMessage;
import com.idontchop.datesearchservice.dtos.SearchDto;
import com.idontchop.datesearchservice.dtos.SearchRequest;

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
	void testLocationSearch () throws InterruptedException, IOException {
		
		String testApiString = "test api";
		
		MockWebServer baseCall = micros.get(MicroService.LOCATION);
		
		baseCall.enqueue( new MockResponse ()
				.setBody(locJson())
				.addHeader("Content-type", "application/json"));

		Mono<String> result = testApis.testLocationSearch();
		
		Set<String> potentials = jsonExtraction.userListFromLocation(result.block());
		logger.debug("---");
		logger.debug( "testlocationSearch" + potentials.toString());
		logger.debug("---");
	}
	
	@Test
	void testAddMatch () throws InterruptedException {
		List<String> likeList = List.of("30", "22");
		
		// test searchdto absorb
		SearchDto sdto = SearchDto.build(likeList);
		SearchDto adto = SearchDto.build().absorbMatch(sdto, "like");
		assertEquals(1, adto.getMatches().size());
		
		
		try {
			setMock(MicroService.LIKE, likeList);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MicroServiceApiAbstract api = (MicroServiceApiAbstract) context.getBean(MicroService.LIKE.getClassName());
		
		ReduceRequest reduceRequest = new ReduceRequest();
		reduceRequest.setName("username"); reduceRequest.setPotentials(List.of("none"));
		SearchDto dto = api.addMatch(reduceRequest).block();
		
		RecordedRequest rr = micros.get(MicroService.LIKE).takeRequest();
		
		assertEquals("POST", rr.getMethod());
		assertNotNull(dto);
		assertEquals(1, dto.getPotentials().size());
		assertEquals(0, dto.getApiMessages().size());
		assertEquals(1, dto.getMatches().size());
		assertEquals(likeList.stream().collect(Collectors.toSet()), dto.getMatches().get(MicroService.LIKE.getName()));
		assertEquals(Set.of("none"), dto.getPotentials());
		
	}
	
	@Test
	void testReduce()  {
		
		
		
		// location: base call
		MockWebServer baseCall = micros.get(MicroService.LOCATION);
		baseCall.enqueue( new MockResponse ()
				.setBody(locJson())
				.addHeader("Content-type", "application/json"));
		
		// reduce calls
		try {
			setMock(MicroService.BLOCK, List.of("30"));
			setMock(MicroService.AGE, List.of("30", "22", "20"));
			setMock(MicroService.LIKE, List.of("30"));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		SearchRequest searchRequest = getSearchRequest();
		// Search Potentials
		SearchPotentialsApi sApi = SearchPotentialsApi.from(searchRequest, context);
		
		
		// Run reduce and produce SearchDto
		Mono<SearchDto> m = sApi.run();
		SearchDto sdto = null;
		try {
			sdto = m.block();
		} catch ( Exception e ) {

			assertNull(e.getMessage());
		}
		

		// Check requests
		RecordedRequest request = null, likeRequest = null;
		RecordedRequest ageRequest = null;
		try {
			ageRequest = micros.get(MicroService.AGE).takeRequest();
			request = micros.get(MicroService.BLOCK).takeRequest();
			likeRequest = micros.get(MicroService.LIKE).takeRequest();
		} catch (Exception e) {

			assertNull(e.getMessage());
		}
		
		assertEquals("POST", ageRequest.getMethod());
		assertEquals("POST",request.getMethod());
		assertEquals("POST", likeRequest.getMethod());
		

		assertEquals(1,sdto.getMatches().size());
		assertTrue(sdto.getMatches().containsKey(MicroService.LIKE.getName()));
		assertEquals(1, sdto.getMatches().get(MicroService.LIKE.getName()).size());
		assertTrue(sdto.getMatches().get(MicroService.LIKE.getName()).contains("30"));
		assertEquals(1,sdto.getPotentials().size());
		assertTrue(sdto.getPotentials().contains("30"));
				

	}
	
	private void setMock (MicroService m, List<String> users) throws JsonProcessingException {
		
		MockWebServer mws = micros.get(m);
		mws.enqueue( new MockResponse()
				.setBody(objectMapper.writeValueAsString(users))
				.addHeader("Content-type", "application/json") );
	}
	
	private static String locJson () {
		
		byte[] encoded;
		try {
			encoded = Files.readAllBytes(Paths.get("./src/test/java/com/idontchop/datesearchservice/location-result.json"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			encoded = null;
		}
		return new String ( encoded, StandardCharsets.US_ASCII);
	}
	
	private static SearchRequest getSearchRequest () {
		
		SearchRequest searchRequest = new SearchRequest();
		List<MicroService> reduceSearches =
				List.of(MicroService.AGE);
		List<MicroService> matchSearches =
				List.of(MicroService.LIKE);
		
		int maxAge = 80, minAge = 5, range = 500;
		double lat = 34.001, lng = 114.001;
		String locationTypes = "LOC,HOME";
		String username  = "username";
		
		searchRequest.setUsername(username);
		searchRequest.setBaseSearch(MicroService.LOCATION);
		searchRequest.setReduceSearch(reduceSearches);
		searchRequest.setMatchesSearch(matchSearches);
		searchRequest.setLocationTypes(locationTypes);
		searchRequest.setMaxAge(maxAge);
		searchRequest.setMinAge(minAge);
		searchRequest.setLat(lat); searchRequest.setLng(lng);
		searchRequest.setRange(range);
		searchRequest.setSelections(List.of());
		
		return searchRequest;
	}
	
	


}
