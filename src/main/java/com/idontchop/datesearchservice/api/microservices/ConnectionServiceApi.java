package com.idontchop.datesearchservice.api.microservices;

import java.io.IOException;

import com.idontchop.datesearchservice.api.MicroServiceApiAbstract;
import com.idontchop.datesearchservice.config.enums.MicroService;

public class ConnectionServiceApi extends MicroServiceApiAbstract  {

	public ConnectionServiceApi() throws IOException, RuntimeException {
		super(MicroService.CONNECTION);
		
	}

}
