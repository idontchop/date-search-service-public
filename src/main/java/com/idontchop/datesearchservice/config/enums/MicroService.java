package com.idontchop.datesearchservice.config.enums;

import com.idontchop.datesearchservice.api.microservices.*;

public enum MicroService {
	
	MEDIA ("media-service", "MediaServiceApi", "localhost:62901/test-media"),
	GENDER ("gender-service", "GenderServiceApi", "localhost:62902/test-gender"),
	MEDIADATA ("mediadata-service", "MediadataServiceApi", "localhost:62903/test-mediadata"),
	PROFILE ("profile-service", "ProfileServiceApi", "localhost:62904/test-profile"),
	LOCATION ("location-service", "LocationServiceApi", "localhost:62905/test-location"),
	AGE ("age-service", "AgeServiceApi", "localhost:62906/test-age"),
	BLOCK ("block-service", "BlockServiceApi", "localhost:62907/test-block"),
	HIDE ("hide-service", "HideServiceApi", "localhost:62908/test-hide"),
	LIKE ("like-service", "LikeServiceApi", "localhost:62909/test-like");
	
	private String name;
	
	private String className;
	
	// used in mocks for integration tests
	private String testAddress;
	
	MicroService ( String name, String className, String testAddress ) {
		this.name = name;
		this.className = className;
		this.testAddress = testAddress;
	}
	
	public String getName() {
		return name;
	}
	
	public String getClassName() {
		return className;
	}

	public String getTestAddress() {
		return testAddress;
	}
	
	

}
