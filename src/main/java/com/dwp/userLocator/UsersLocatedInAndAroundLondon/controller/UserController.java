package com.dwp.userLocator.UsersLocatedInAndAroundLondon.controller;

import com.dwp.userLocator.UsersLocatedInAndAroundLondon.model.User;
import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("userlocator")
public class UserController {

    @Value("${url.for.people.living.in.london}")
    private String urlForPeopleLivingInLondon;

    @Value("${url.for.all.people}")
    private String urlForAllUsers;

    @Autowired
    RestTemplate restTemplate;

    @GetMapping(path ="/userList/{city}")
    public ResponseEntity<User[]> getUsersInLondonAndInA50Mileradius(@PathVariable(value = "city") String city) throws Exception {
        List<User> userThatLiveInAndWithin50MilesOfLondon = new ArrayList<>();
        double latitudeCoordinateForLondon = 51 + (30 / 60.0) + (26 / 60.0 / 60.0);
        double longitudeCoordinateForLondon =  0 - (7 / 60.0) - (39 / 60.0 / 60.0);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = null;
        ResponseEntity<User[]> response = null;
        if (city.equalsIgnoreCase("London")) {
            entity = new HttpEntity<String>(httpHeaders);
            response = restTemplate.exchange(urlForPeopleLivingInLondon, HttpMethod.GET, entity, User[].class);
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            for (User users : response.getBody()) {
                userThatLiveInAndWithin50MilesOfLondon.add(users);
            }
            System.out.println();
        } else {
            System.out.println("Error");
        }
        ResponseEntity<User[]> responses = restTemplate.exchange(urlForAllUsers, HttpMethod.GET, entity, User[].class);
        for (User user : responses.getBody()) {
            double distanceInMiles = calculate50milesRadiusFromLondon(latitudeCoordinateForLondon, longitudeCoordinateForLondon, user.getLatitude(), user.getLongitude(), user);
            if (distanceInMiles <= 50) {
                userThatLiveInAndWithin50MilesOfLondon.add(user);
            }
        }
            User[] objects = userThatLiveInAndWithin50MilesOfLondon.toArray(new User[0]);
            return new ResponseEntity<User[]>(objects, HttpStatus.OK);
        }

    private double calculate50milesRadiusFromLondon(double latitudeOfLondon, double longitudeOfLondon,double latitudeOfUser, double longitudeOfUser,User user){
        GeodesicData result = Geodesic.WGS84.Inverse(latitudeOfLondon, longitudeOfLondon, latitudeOfUser, longitudeOfUser);
        double distanceInMetres = result.s12;
        double milesConverter = 1609.34;
        double distanceInMiles = distanceInMetres / milesConverter;
        return distanceInMiles;
    }
}








