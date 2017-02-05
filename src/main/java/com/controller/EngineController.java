package com.controller;


import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.opengts.util.EmailSender;
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
import com.model.db.db.db.users_location.UsersLocation;
import com.model.db.db.db.users_location.UsersLocationImpl;
import com.model.db.db.db.users_location.UsersLocationManager;
import com.model.entities.CollectCoinEntity;
import com.model.entities.LocationEntity;
import com.model.entities.UserEntity;
import com.pusher.rest.Pusher;
import com.speedment.plugins.json.JsonBundle;
import com.speedment.plugins.json.JsonCollector;
import com.speedment.plugins.json.JsonComponent;

import spark.Request;
import spark.Response;
import spark.Route;

public class EngineController {

	static double range = 0.02; 
	
	static DbApplication application = new DbApplicationBuilder()
            .withUsername("root")
            .withBundle(JsonBundle.class)
            .withPassword("KOYnjFz6zPZT")
            .build();
	
//	static DbApplication application = new DbApplicationBuilder()
//            .withUsername("root")
//            .withBundle(JsonBundle.class)
//            .withPassword("root")
//            .build();
	
	static UserManager  users = application.getOrThrow(UserManager.class);
	static CoinManager  coins = application.getOrThrow(CoinManager.class);
	static LocationManager  locations = application.getOrThrow(LocationManager.class);
	static StoreManager  stores = application.getOrThrow(StoreManager.class);
	static UsersLocationManager  usersLocation = application.getOrThrow(UsersLocationManager.class);
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
		System.out.println("register with: " + creation.toString());
		//check if user already exists
		Optional<User> user = users.stream()
	        .filter(User.EMAIL.contains(creation.email))
	        .findAny();
		
		if (user.isPresent())	
			return json.allOf(users).apply(user.get());
		
		User newUser = new UserImpl()
				.setEmail(creation.email)
				.setPassword(Utils.sha256(creation.password))
				.setNotificationTime(new Timestamp(System.currentTimeMillis()))
				.setCreateTime(new Timestamp(System.currentTimeMillis()));
		users.persist(newUser);
		EmailSender.send("New user just registered",creation.email);
		return json.allOf(users).apply(newUser);
	};

	public static final Route getFreeCoins = (Request request, Response response) -> {
		System.out.println("getFreeCoins: " + request.queryString().toString());
		
		Optional<Integer> userId = tryParseInteger(request.queryParams("userId"));
		double longitude, lat, eastLong, northLat, westLong, southLat;
		Set<Integer> filteredLocations;
		if (request.queryParams().size() > 3)
		{
			eastLong = Double.valueOf(request.queryParams("ne.longitude"));
			northLat = Double.valueOf(request.queryParams("ne.latitude"));
			westLong = Double.valueOf(request.queryParams("sw.longitude"));
			southLat = Double.valueOf(request.queryParams("sw.latitude"));
			System.out.println("before getLocationsInRectangle: " + new Timestamp(System.currentTimeMillis()));
			filteredLocations = getLocationsInRectangle(eastLong, northLat, westLong, southLat);
			System.out.println("after getLocationsInRectangle: " + new Timestamp(System.currentTimeMillis()));
		}
		else
		{
			longitude = Double.valueOf(request.queryParams("longitude"));
			lat = Double.valueOf(request.queryParams("latitude"));
			System.out.println("before getLocationsFixedInRange: " + new Timestamp(System.currentTimeMillis()));
			filteredLocations = getLocationsFixedInRange(longitude, lat);
			System.out.println("after getLocationsFixedInRange: " + new Timestamp(System.currentTimeMillis()));
		}
		
		System.out.println("before coins.stream: " + new Timestamp(System.currentTimeMillis()));
		String result = coins.stream()
		.filter(Coin.TAKEN.notEqual("true"))
		.filter(Coin.USER_ID.equal(userId.orElse(-1)).or(Coin.USER_ID.isNull()))
		.filter(c -> filteredLocations.contains(c.getLocationId()))
		.collect(JsonCollector.toJson(json.allOf(coins)
				.put(Coin.LOCATION_ID, json.allOf(locations)))
				);

		System.out.println("after coins.stream: " + new Timestamp(System.currentTimeMillis()));
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
		        .filter(User.PASSWORD.equal(Utils.sha256(creation.password)))
		        .map(User.NOTIFICATION_ID.setTo(creation.notificationId))
		        .findAny();
			
		if (user.isPresent()){
			users.update(user.get());
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
				.setCreateTime(new Timestamp(System.currentTimeMillis()))
				.setLocationId(locationId)
				.setUserId(creation.userId);
		
		coins.persist(newCoin);
		//EmailSender.send("new coin was added: " + newCoin.getId(), "With vault: " + newCoin.getValue());
		return json.allOf(coins).apply(newCoin);
	};

	public static final Route AddUserLocation = (Request request, Response response) -> {
		LocationEntity location = new ObjectMapper().readValue(request.body(), LocationEntity.class);
		System.out.println("update User Location : " + request.body().toString());
		
		UsersLocation ul = new UsersLocationImpl()
				.setUserId(Integer.valueOf(request.params("id")))
				.setLatitude(location.latitude)
				.setLongitude(location.longitude)
				.setTimestamp(String.valueOf(new Timestamp(System.currentTimeMillis())));
		usersLocation.persist(ul);
		checkPushNotification(ul);
		return json.allOf(usersLocation).apply(ul);
	};
	
		
	public static final Route collectCoin = (Request request, Response response) -> {
		System.out.println(request.body().toString());
		CollectCoinEntity creation = new ObjectMapper().readValue(request.body(), CollectCoinEntity.class);
		//EmailSender.send("Coin "+ creation.coinId +" was collected by user: " + creation.userId,"");
		Timestamp t = new Timestamp(System.currentTimeMillis());
		coins.stream()
		    .filter(Coin.ID.equal(Integer.valueOf(creation.coinId)))
		    .map(Coin.USER_ID.setTo(Integer.valueOf(creation.userId)))
		    .map(Coin.TAKEN.setTo("true"))
		    .map(Coin.TAKEN_DATE.setTo(t.toString()))
		    .forEach(coins.updater());
		
		
		Optional<Coin> res = coins.stream()
		    .filter(Coin.ID.equal(Integer.valueOf(creation.coinId)))
		    .findAny();
		Optional<User> coinUser = users.stream()
				.filter(User.ID.equal(Integer.valueOf(creation.userId)))
				.findAny();
		
		if (res.isPresent() && coinUser.isPresent()){	
			coinUser.map(u -> u.setCredits(u.getCredits().orElse(0) + Integer.valueOf(res.get().getValue().get())));
			users.persist(coinUser.get());

			checkIfCredisOverFive(coinUser.get());
			
			return json.allOf(coins).apply(res.get());
		}
			
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

	public static final Route getUserConfig = (Request request, Response response) -> {
		String result = "{"
                +   "\"collect_coins_visability_distance\": \"50\","
                +   "\"desired_accuracy\": \"hundreds\""
                + "}";
		return result;
	};

	///			 ///
	/// Privates ///
	///			 ///
	private static Set<Integer> getLocationsFixedInRange(double longitude, double lat) {
		GeoPolygon fence = new GeoPolygon(
	            new GeoPoint(lat - range, longitude - range),
	            new GeoPoint(lat + range, longitude - range),
	            new GeoPoint(lat + range, longitude + range),
	            new GeoPoint(lat - range, longitude + range)
	            );

		return filterLocationsByFence(fence);
	}
	
	private static void checkIfCredisOverFive(User user) {
		if (user.getCredits().orElse(0) >= 5){
			EmailSender.send("User " +user.getId() + " has reached 5 dollars", user.getEmail().orElse("missing email"));
		}	
	}

	private static void checkPushNotification(UsersLocation ul) {
		Set<Integer> filteredLocations;
		Double longitude = Double.valueOf(ul.getLongitude());
		Double lat = Double.valueOf(ul.getLatitude());
		Optional<User> user = users.stream()
		        .filter(User.ID.equal(ul.getUserId()))
		        .findAny();
		
		filteredLocations = getLocationsFixedInRange(longitude,lat);
		if (filteredLocations.isEmpty()) return;
		long result = coins.stream()
				.filter(Coin.TAKEN.notEqual("true"))
				.filter(c -> filteredLocations.contains(c.getLocationId()))
				.count();
		if (result > 0) {
			if (canSendNotification(user.get())) {
				sendOneSignalNotification(user.get().getNotificationId().get());
				updateNotificationTime(user.get());
			}
		}
	}

	private static void updateNotificationTime(User user) {
		users.update(user.setNotificationTime(new Timestamp(System.currentTimeMillis())));
	}

	private static boolean canSendNotification(User user) {
		Timestamp ts = user.getNotificationTime().get();
		return LocalDateTime.now().isAfter(ts.toLocalDateTime().plusHours(6));
	}

	private static Set<Integer> getLocationsInRectangle(double eastLong, double northLat, double westLong, double southLat) {
		GeoPolygon fence = new GeoPolygon(
	            new GeoPoint(southLat, eastLong),
	            new GeoPoint(northLat, eastLong),
	            new GeoPoint(northLat, westLong),
	            new GeoPoint(southLat, westLong)
	            );

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

	private static int addNewLocation(LocationEntity creation) throws Exception {
		int locationId;
		GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyB-iartdbLOzap5Jfaw5KucWyNSkeOT0mM");
		GeocodingResult[] results =  GeocodingApi.reverseGeocode(context,
						new LatLng(Double.valueOf(creation.latitude),
								   Double.valueOf(creation.longitude)))
				.await();
		System.out.println("addNewLocation:" + results[0].formattedAddress);

		Location newLocation = new LocationImpl()
				.setLatitude(creation.latitude)
				.setLongitude(creation.longitude)
				.setAddress(results[0].formattedAddress);
		locations.persist(newLocation);
		locationId = newLocation.getId();
		return locationId;
	}
	
	private static void sendOneSignalNotification(String notificationId) {
		try {
			   String jsonResponse;
			   
			   URL url = new URL("https://onesignal.com/api/v1/notifications");
			   HttpURLConnection con = (HttpURLConnection)url.openConnection();
			   con.setUseCaches(false);
			   con.setDoOutput(true);
			   con.setDoInput(true);

			   con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			   con.setRequestProperty("Authorization", "Basic NzJlMDViZDktOGU3Yy00Y2VkLWI4NWMtOTI0YzFhOTMwODI5");
			   con.setRequestMethod("POST");

			   String strJsonBody = "{"
			                      +   "\"app_id\": \"841da559-305e-46bc-ab04-e12456f8f764\","
			                      +   "\"include_player_ids\": [\"" + notificationId + "\"],"
			                      +   "\"data\": {\"Coins\": \"notification\"},"
			                      +   "\"contents\": {\"en\": \"New Coins are available near you, be first to collect!\"}"
			                      + "}";
			         
			   
			   System.out.println("strJsonBody:\n" + strJsonBody);

			   byte[] sendBytes = strJsonBody.getBytes("UTF-8");
			   con.setFixedLengthStreamingMode(sendBytes.length);

			   OutputStream outputStream = con.getOutputStream();
			   outputStream.write(sendBytes);

			   int httpResponse = con.getResponseCode();
			   System.out.println("httpResponse: " + httpResponse);

			   if (  httpResponse >= HttpURLConnection.HTTP_OK
			      && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
			      Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
			      jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
			      scanner.close();
			   }
			   else {
			      Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
			      jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
			      scanner.close();
			   }
			   System.out.println("jsonResponse:\n" + jsonResponse);
			   
			} catch(Throwable t) {
			   t.printStackTrace();
			}
	}
	
	private static Optional<Integer> tryParseInteger(String string) {
	    try {
	        return Optional.of(Integer.valueOf(string));
	    } catch (NumberFormatException e) {
	        return Optional.empty();
	    }
	}
	
}
