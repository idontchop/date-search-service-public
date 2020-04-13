package com.idontchop.datesearchservice.api.microservices;

import java.io.IOException;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.idontchop.datesearchservice.api.MicroServiceApiAbstract;
import com.idontchop.datesearchservice.config.enums.MicroService;
import com.idontchop.datesearchservice.dtos.ReduceRequest;
import com.idontchop.datesearchservice.dtos.SearchDto;

import reactor.core.publisher.Mono;

@Component ("LocationServiceApi")
public class LocationServiceApi extends MicroServiceApiAbstract {

	public LocationServiceApi( ) throws IOException, RuntimeException {
		super(MicroService.LOCATION);
		apiBaseExt = "/api/search-location";
	}

	/**
	 * Location is a base search only MicroService
	 */
	@Override
	public Mono<SearchDto> reduce(String username, Set<String> potentials) {
		return Mono.error(new IOException("Location has no reduce function"));
	}

	@Override
	public Mono<SearchDto> reduce(ReduceRequest reduceRequest) {
		return Mono.error(new IOException("Location has no reduce function"));
	}
	
}
