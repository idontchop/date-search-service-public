package com.idontchop.datesearchservice.api.microservices;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.idontchop.datesearchservice.api.MicroServiceApiAbstract;
import com.idontchop.datesearchservice.config.enums.MicroService;

@Component ("HideServiceApi")
public class HideServiceApi extends MicroServiceApiAbstract {

	public HideServiceApi() throws IOException, RuntimeException {
		super(MicroService.HIDE);
		apiReduceExt = "/hide/difference";
	}

}
