package com.idontchop.datesearchservice.api.microservices;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.idontchop.datesearchservice.api.MicroServiceApiAbstract;
import com.idontchop.datesearchservice.config.enums.MicroService;

@Component ("BlockServiceApi")
public class BlockServiceApi extends MicroServiceApiAbstract {
	
	public BlockServiceApi () throws IOException {
		super (MicroService.BLOCK);
		apiReduceExt = "/block/difference";
	}

}
