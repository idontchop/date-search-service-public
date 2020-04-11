package com.idontchop.datesearchservice;


import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.idontchop.datesearchservice.api.JsonExtraction;
import com.idontchop.datesearchservice.api.TestApis;
import com.idontchop.datesearchservice.controllers.TestController;


@SpringBootTest
@AutoConfigureWebTestClient
public class AbstractEndpoints {
	
	Logger logger = LoggerFactory.getLogger(AbstractEndpoints.class);
	
	@Autowired
	private WebTestClient client;
	
	@Autowired
	JsonExtraction jsonExtractor;
		
	@Test
	public void testLocationSearch () throws IOException {
		
		EntityExchangeResult<String> result = 
			this.client
			.get()
			.uri("/search/searchTest")
			.exchange()
			.expectStatus().isOk()
			.expectBody(String.class)
			.returnResult();
		
		String json = result.getResponseBody();
		
		logger.debug("Finished");
		logger.debug(json);
		
		List<String> userList = jsonExtractor.userListFromLocation(json);
		
		assertTrue (userList.size() > 0);
		
		userList.forEach( e -> logger.debug(e));
		
		
	}
	
	@Test
	public void contextLoads() {
	}

}
