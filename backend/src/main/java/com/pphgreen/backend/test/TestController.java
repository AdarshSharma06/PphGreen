package com.pphgreen.backend.test;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/protected")
    public ResponseEntity<String> protectedEndpoint(
            Authentication authentication) {

        return ResponseEntity.ok(
                "Authenticated successfully as: "
                        + authentication.getName()
        );
    }
}