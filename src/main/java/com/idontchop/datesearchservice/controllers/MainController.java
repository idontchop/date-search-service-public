package com.idontchop.datesearchservice.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.idontchop.datesearchservice.dtos.RestMessage;

import reactor.core.publisher.Mono;

@RestController
public class MainController {
	
	@GetMapping ("/helloWorld")
	Mono<RestMessage> helloWorld() {
		
		RestMessage rm = RestMessage.build("Hello From Reactive Spring!");
		
		return Mono.just(rm);
		
	}

}
