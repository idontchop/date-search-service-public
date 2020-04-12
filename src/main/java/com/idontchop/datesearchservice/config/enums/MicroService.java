package com.idontchop.datesearchservice.config.enums;

import com.idontchop.datesearchservice.api.microservices.*;

public enum MicroService {
	
	MEDIA ("media-service", ""),
	GENDER ("gender-service", "GenderServiceApi"),
	MEDIADATA ("mediadata-service", ""),
	PROFILE ("profile-service", ""),
	LOCATION ("location-service", ""),
	AGE ("age-service", "AgeServiceApi"),
	BLOCK ("block-service", "BlockServiceApi"),
	LIKE ("like-service", "LikeServiceApi");
	
	private String name;
	
	private String className;
	
	MicroService ( String name, String className ) {
		this.name = name;
		this.className = className;
	}
	
	public String getName() {
		return name;
	}
	
	public String getClassName() {
		return className;
	}
	
	

}
