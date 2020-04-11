package com.idontchop.datesearchservice.api.microservices;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.idontchop.datesearchservice.api.MicroServiceApiAbstract;
import com.idontchop.datesearchservice.config.enums.MicroService;

@Component ("AgeServiceApi")
public class AgeServiceApi extends MicroServiceApiAbstract {

	public AgeServiceApi() throws IOException, RuntimeException {
		super(MicroService.AGE);
		apiReduceExt = "/age/reduce";
	}

}
