package com.idontchop.datesearchservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.idontchop.datesearchservice.controllers.SearchRestController;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class SearchEndpointConfiguration {
	
	@Bean
	RouterFunction<ServerResponse> routes (SearchRestController handler) {
		return route ( GET("/search/searchTest"), handler::searchTest);
	}

}
