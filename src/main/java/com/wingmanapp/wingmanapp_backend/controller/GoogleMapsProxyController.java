package com.wingmanapp.wingmanapp_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class GoogleMapsProxyController {

    private final String BASE_URL_NEARBY = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    private final String BASE_URL_PHOTO = "https://maps.googleapis.com/maps/api/place/photo";
    private final String BASE_URL_DETAIL = "https://maps.googleapis.com/maps/api/place/details";

    @GetMapping("/api/proxy/nearbysearch")
    public ResponseEntity<String> proxyNearbySearch(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam int radius,
            @RequestParam String apiKey,
            @RequestParam(required = false) String pageToken
    ) {
        String url = String.format("%s?location=%f,%f&radius=%d&key=%s", BASE_URL_NEARBY, latitude, longitude, radius, apiKey);
        if (pageToken != null && !pageToken.isEmpty()) {
            url += "&pagetoken=" + pageToken;
        }

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        return ResponseEntity.ok().body(response.getBody());
    }

    @GetMapping("/api/proxy/detail")
    public ResponseEntity<String> proxyDetail(
            @RequestParam String placeId,
            @RequestParam String apiKey
    ) {
        String url = String.format("%s/json?place_id=%s&key=%s", BASE_URL_DETAIL, placeId, apiKey);

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
}
