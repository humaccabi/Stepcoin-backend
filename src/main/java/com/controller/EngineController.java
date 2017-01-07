package com.controller;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.opengts.util.GeoPoint;
import org.opengts.util.GeoPolygon;
import org.opengts.util.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.model.db.DbApplication;
import com.model.db.DbApplicationBuilder;
import com.model.db.db.db.coin.Coin;
import com.model.db.db.db.coin.CoinImpl;
import com.model.db.db.db.coin.CoinManager;
import com.model.db.db.db.location.Location;
import com.model.db.db.db.location.LocationImpl;
import com.model.db.db.db.location.LocationManager;
import com.model.db.db.db.store.Store;
import com.model.db.db.db.store.StoreManager;
import com.model.db.db.db.user.User;
import com.model.db.db.db.user.UserImpl;
import com.model.db.db.db.user.UserManager;
import com.model.entities.CollectCoinEntity;
import com.model.entities.LocationEntity;
import com.model.entities.UserEntity;
import com.speedment.plugins.json.JsonBundle;
import com.speedment.plugins.json.JsonCollector;
import com.speedment.plugins.json.JsonComponent;

import spark.Request;
import spark.Response;
import spark.Route;

public class EngineController {

	static double range = 0.01; 
	
	static DbApplication application = new DbApplicationBuilder()
            .withUsername("root")
            .withBundle(JsonBundle.class)
            .withPassword("KOYnjFz6zPZT")
            .build();
	static UserManager  users = application.getOrThrow(UserManager.class);
	static CoinManager  coins = application.getOrThrow(CoinManager.class);
	static LocationManager  locations = application.getOrThrow(LocationManager.class);
	static StoreManager  stores = application.getOrThrow(StoreManager.class);
	static JsonComponent json = application.getOrThrow(JsonComponent.class);
	
	public static final Route getUserDetails = (Request request, Response response) -> {
        String result = users.stream()
        		.filter(User.ID.equal(Integer.valueOf(request.params("id"))))
        	    .collect(JsonCollector.toJson(
        	        json.allOf(users)
        	            .putStreamer(
        	                "coins",                            
        	                coins.finderBackwardsBy(Coin.USER_ID),
        	                json.allOf(coins)
							.put(Coin.LOCATION_ID, json.allOf(locations))
        	            )
        	    ));  
        
		return result;
	};
	
	public static final Route register = (Request request, Response response) -> {

		UserEntity creation = new ObjectMapper().readValue(request.body(), UserEntity.class);
		System.out.println(creation.toString());
		//check if user already exists
		Optional<User> user = users.stream()
	        .filter(User.EMAIL.contains(creation.email))
	        .findAny();
		
		if (user.isPresent())	
			return json.allOf(users).apply(user.get());
		
		User newUser = new UserImpl().setEmail(creation.email).setPassword(Utils.sha256(creation.password));
		users.persist(newUser);
               
		return json.allOf(users).apply(newUser);
	};
	public static final Route getCoins = null;
	public static final Route getFreeCoins = (Request request, Response response) -> {
		System.out.println(request.queryString().toString());
		
		double longitude, lat;
		double eastLong, northLat, westLong, southLat;
		Set<Integer> filteredLocations;
		if (request.queryParams().size() == 4)
		{
			eastLong = Double.valueOf(request.queryParams("ne.longitude"));
			northLat = Double.valueOf(request.queryParams("ne.latitude"));
			westLong = Double.valueOf(request.queryParams("sw.longitude"));
			southLat = Double.valueOf(request.queryParams("sw.latitude"));
			filteredLocations = getLocationsInRectangle(eastLong, northLat, westLong, southLat);
		}
		else
		{
			longitude = Double.valueOf(request.queryParams("longitude"));
			lat = Double.valueOf(request.queryParams("latitude"));
			filteredLocations = getLocationsFixedInRange(longitude, lat);
		}
		
		System.out.println(filteredLocations);

		String result = coins.stream()
		.filter(Coin.TAKEN.notEqual("true"))
		.filter(c -> filteredLocations.contains(c.getLocationId()))
		.collect(JsonCollector.toJson(json.allOf(coins)
				.put(Coin.LOCATION_ID, json.allOf(locations)))
				);

		return result;
	};

	public static final Route getStoreDetails = (Request request, Response response) -> {
		
		String result = stores.stream()
				.filter(Store.ID.equal(Integer.valueOf(request.params("id"))))
        	    .collect(JsonCollector.toJson(json.allOf(stores)));
		return result;
	};
	
	public static final Route loginUser = (Request request, Response response) -> {
		UserEntity creation = new ObjectMapper().readValue(request.body(), UserEntity.class);
		Optional<User> user = users.stream()
		        .filter(User.EMAIL.equal(creation.email))
		        .filter(User.PASSWORD.equal(creation.password))
		        .findAny();
			
			if (user.isPresent())
			{
				return json.noneOf(users).putInt(User.ID).apply(user.get());
			}
		
		return "[]";
	};
	
	public static final Route addNewCoin = (Request request, Response response) -> {
		LocationEntity creation = new ObjectMapper().readValue(request.body(), LocationEntity.class);
		System.out.println("addNewCoin: " + request.body().toString());
		
		Optional<Location> location = locations.stream()
		        .filter(Location.LATITUDE.equal(creation.latitude))
		        .filter(Location.LONGITUDE.equal(creation.longitude))
		        .findAny();
		
		int locationId;
		if (location.isPresent()){
			locationId = location.get().getId();
		}
		else {  
			locationId = addNewLocation(creation);
		}
		
		float randomNum = ThreadLocalRandom.current().nextInt(1, 11);
    	randomNum = randomNum /10;
		//randomize coin value and stores
		Coin newCoin = new CoinImpl()
				.setValue(String.valueOf(randomNum))
				.setTaken("false")
				.setStoreId(4)
				.setLocationId(locationId);
		coins.persist(newCoin);
		
		return json.allOf(coins).apply(newCoin);
	};

	private static int addNewLocation(LocationEntity creation) throws Exception {
		int locationId;
		GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyB-iartdbLOzap5Jfaw5KucWyNSkeOT0mM");
		GeocodingResult[] results =  GeocodingApi
				.reverseGeocode(context,
						new LatLng(Double.valueOf(creation.latitude),
								   Double.valueOf(creation.longitude)))
				.await();
		System.out.println(results[0].formattedAddress);

		Location newLocation = new LocationImpl()
				.setLatitude(creation.latitude)
				.setLongitude(creation.longitude)
				.setAddress(results[0].formattedAddress);
		locations.persist(newLocation);
		locationId = newLocation.getId();
		return locationId;
	}
	
	public static final Route collectCoin = (Request request, Response response) -> {
		System.out.println(request.body().toString());
		CollectCoinEntity creation = new ObjectMapper().readValue(request.body(), CollectCoinEntity.class);
		
		coins.stream()
	    .filter(Coin.ID.equal(Integer.valueOf(creation.coinId)))
	    .map(Coin.USER_ID.setTo(Integer.valueOf(creation.userId)))
	    .map(Coin.TAKEN.setTo("true"))
	    .forEach(coins.updater());
		
		Optional<Coin> res = coins.stream()
	    .filter(Coin.ID.equal(Integer.valueOf(creation.coinId)))
		        .findAny();
		
		if (res.isPresent())
			return json.allOf(coins).apply(res.get());
		else
			return "[]";
	};
	
	public static final Route getUsers = (Request request, Response response) -> {
	        String result = users.stream()
	        	    .collect(JsonCollector.toJson(
	        	        json.allOf(users)
	        	            .putStreamer(
	        	                "coins",                            
	        	                coins.finderBackwardsBy(Coin.USER_ID), 
	        	                json.allOf(coins)                  
	        	            )
	        	    ));  
	        
			return result;
		};

	private static Set<Integer> getLocationsFixedInRange(double longitude, double lat) {
		GeoPolygon fence = new GeoPolygon(
	            new GeoPoint(lat - range, longitude - range),
	            new GeoPoint(lat + range, longitude - range),
	            new GeoPoint(lat + range, longitude + range),
	            new GeoPoint(lat - range, longitude + range)
	            );

		return filterLocationsByFence(fence);
	}
	
	private static Set<Integer> getLocationsInRectangle(double eastLong, double northLat, double westLong, double southLat) {
		GeoPolygon fence = new GeoPolygon(
	            new GeoPoint(southLat, eastLong),
	            new GeoPoint(northLat, eastLong),
	            new GeoPoint(northLat, westLong),
	            new GeoPoint(southLat, westLong)
	            );
		System.out.println("getLocationsInRectangle is valid: " + fence.isClockwise());
		return filterLocationsByFence(fence);
	}

	private static Set<Integer> filterLocationsByFence(GeoPolygon fence) {
		Set<Integer> filteredLocations = locations.stream()
				.filter(c-> fence.isPointInside(new GeoPoint(
						Double.valueOf(c.getLatitude()),
						Double.valueOf((c.getLongitude())))))
				.map(Location::getId)
				.collect(Collectors.toSet());
		return filteredLocations;
	}

}
