package com.idontchop.datesearchservice.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class JsonExtraction {
	
	Logger logger = LoggerFactory.getLogger(JsonExtraction.class);
		
	
	@Autowired
	ObjectMapper mapper;
	
	public List<String> userListFromLocation (String locationJson) throws IOException {
		
		JsonNode userList = mapper.readTree(locationJson).get("results").get("content");
		
		if ( userList == null ) {
			logger.debug(locationJson);
		}
		if ( !userList.isArray() ) {
			throw new IOException("Malformed Json from Location Service: " + locationJson);
		}
		
		List<String> users = new ArrayList<>();
		
		userList.forEach( user -> {
			String username = user.get("content").get("username").toString();
			if ( username != null ) {
				users.add(username);
			}
		});
		
		return users;
		
	}
}
