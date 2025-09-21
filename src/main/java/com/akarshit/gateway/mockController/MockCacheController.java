package com.akarshit.gateway.mockController;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class MockCacheController {

    @GetMapping("/cache")
    public Mono<String> mockCacheResponse(@RequestParam String key) {
        return Mono.just("mock response for key " + key);
    }
}
