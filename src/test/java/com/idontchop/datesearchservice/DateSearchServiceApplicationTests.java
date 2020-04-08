package com.idontchop.datesearchservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.idontchop.datesearchservice.api.TestApis;

import reactor.core.publisher.Flux;

@SpringBootTest
class DateSearchServiceApplicationTests {

	@Autowired
	TestApis testApis;
	
	@Test
	void contextLoads() {
	}
	
	@Test
	public void testHelloWorlds() throws InterruptedException {
		
		Flux<String> apis = testApis.helloWorlds();
		
		apis.subscribe( e -> {
			System.out.println(e);
		}, e -> e.getMessage() );
		
		Thread.sleep(1000);
	}

}
