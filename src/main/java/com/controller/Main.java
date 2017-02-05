package com.controller;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.port;
import static spark.Spark.ipAddress;
import static spark.Spark.secure;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Collections;


import com.controller.EngineController;


public class Main {
    public static void main(String[] args) {
    	port(9999); 
    	//ipAddress("192.168.1.3");
    	
    	//secure("keystore.jks", "Eliandoriel1!", null, null);

    	get("/users",          		EngineController.getUsers);
    	get("/coins",          		EngineController.getFreeCoins);  //long and lat (radius))
    	post("/coins",          	EngineController.addNewCoin);
    	get("/users/:id",           EngineController.getUserDetails);
    	get("/stores/:id",          EngineController.getStoreDetails);
    	post("/registerUser",       EngineController.register);
    	post("/users/login",        EngineController.loginUser);
    	post("/users/:id",   		EngineController.AddUserLocation);
    	post("/coins/collect",      EngineController.collectCoin);  
    	get("/users/:id/config",    EngineController.getUserConfig);
    }

}
