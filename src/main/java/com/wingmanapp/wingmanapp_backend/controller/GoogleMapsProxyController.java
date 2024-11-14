package com.wingmanapp.wingmanapp_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
public class GoogleMapsProxyController {

    private final String BASE_URL_NEARBY = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    private final String BASE_URL_PHOTO = "https://maps.googleapis.com/maps/api/place/photo";
    private final String BASE_URL_DETAIL = "https://maps.googleapis.com/maps/api/place/details/json";

    private final String[] categoryGroups = {
            "restaurant|cafe|bar|bakery|meal_delivery|meal_takeaway",
            "museum|art_gallery|movie_theater|bowling_alley|zoo|amusement_park|aquarium|night_club|casino|spa",
            "park|shopping_mall|book_store|clothing_store|shoe_store|jewelry_store|store|florist|beauty_salon|library",
            "campground|city_hall|embassy|stadium|tourist_attraction|university"
    };

    @GetMapping("/api/proxy/nearbysearch")
    public ResponseEntity<Map<String, Object>> proxyNearbySearch(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam int radius,
            @RequestParam String apiKey,
            @RequestParam(required = false) String nextPageToken,
            @RequestParam(required = false, defaultValue = "0") int groupIndex
    ) {
        String keywords = categoryGroups[groupIndex];
        String url = String.format("%s?location=%f,%f&radius=%d&keyword=%s&key=%s",
                BASE_URL_NEARBY, latitude, longitude, radius, keywords, apiKey);

        if (nextPageToken != null && !nextPageToken.isEmpty()) {
            url += "&pagetoken=" + nextPageToken;
        }

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> responseBody = handleApiResponse(response, groupIndex);

        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/api/proxy/detail")
    public ResponseEntity<String> proxyDetail(
            @RequestParam String placeId,
            @RequestParam String apiKey
    ) {
        String url = String.format("%s?place_id=%s&key=%s", BASE_URL_DETAIL, placeId, apiKey);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @GetMapping("/api/proxy/photo")
    public ResponseEntity<byte[]> proxyPhoto(
            @RequestParam String photoReference,
            @RequestParam String apiKey,
            @RequestParam(required = false, defaultValue = "400") int maxwidth
    ) {
        String url = String.format("%s?maxwidth=%d&photoreference=%s&key=%s", BASE_URL_PHOTO, maxwidth, photoReference, apiKey);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);

        return ResponseEntity
                .status(response.getStatusCode())
                .body(response.getBody());
    }

    private Map<String, Object> handleApiResponse(ResponseEntity<Map> response, int groupIndex) {
        Map<String, Object> responseBody = new HashMap<>(response.getBody());
        if (responseBody.containsKey("next_page_token")) {
            responseBody.put("groupIndex", groupIndex);
        } else if (groupIndex < categoryGroups.length - 1) {
            responseBody.put("groupIndex", groupIndex + 1);
        } else {
            responseBody.put("groupIndex", null);
        }
        return responseBody;
    }
}
