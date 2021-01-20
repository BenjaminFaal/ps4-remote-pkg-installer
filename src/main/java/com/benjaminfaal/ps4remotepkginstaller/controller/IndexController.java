package com.benjaminfaal.ps4remotepkginstaller.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/")
@RestController
public class IndexController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getUser(OAuth2AuthenticationToken token) {
        return ResponseEntity.ok(token.getPrincipal().getAttributes());
    }

}
