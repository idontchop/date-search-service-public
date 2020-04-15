package com.idontchop.datesearchservice.api.microservices;

import java.io.IOException;

import com.idontchop.datesearchservice.api.MicroServiceApiAbstract;
import com.idontchop.datesearchservice.config.enums.MicroService;

public class MediaServiceApi extends MicroServiceApiAbstract {

	public MediaServiceApi(MicroService microService) throws IOException, RuntimeException {
		super(MicroService.MEDIA);
	}

}
