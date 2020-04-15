package com.idontchop.datesearchservice.api.microservices;

import java.io.IOException;

import com.idontchop.datesearchservice.api.MicroServiceApiAbstract;
import com.idontchop.datesearchservice.config.enums.MicroService;

public class MediadataServiceApi extends MicroServiceApiAbstract {

	public MediadataServiceApi(MicroService microService) throws IOException, RuntimeException {
		super(MicroService.MEDIADATA);
	}

}
