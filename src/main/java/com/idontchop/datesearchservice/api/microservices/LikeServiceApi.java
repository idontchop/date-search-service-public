package com.idontchop.datesearchservice.api.microservices;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Component;

import com.idontchop.datesearchservice.api.MicroServiceApiAbstract;
import com.idontchop.datesearchservice.config.enums.MicroService;

@Component("LikeServiceApi")
public class LikeServiceApi extends MicroServiceApiAbstract {

	
	public LikeServiceApi(List<String> toggles) throws IOException, RuntimeException {
		super(MicroService.LIKE);
		
		// TODO: toggles determines which reduce api will be used
	}

}
