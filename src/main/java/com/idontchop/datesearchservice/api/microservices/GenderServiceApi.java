package com.idontchop.datesearchservice.api.microservices;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.idontchop.datesearchservice.api.MicroServiceApiAbstract;
import com.idontchop.datesearchservice.config.enums.MicroService;

@Component ("GenderServiceApi")
public class GenderServiceApi extends MicroServiceApiAbstract {

	// Sets up Instance Info
	public GenderServiceApi () throws IOException {
		super (MicroService.GENDER);
	}
	
	

}
