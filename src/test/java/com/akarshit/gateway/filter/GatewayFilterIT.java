package com.akarshit.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GatewayFilterIT {


    @Autowired
    WebTestClient webTestClient;

    @Test
    void test_cacheFilter(){
        webTestClient.get()
                .uri("/cache?key=testKey")
                .header(HttpHeaders.HOST, "localhost")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).consumeWith(response -> {
                    assertTrue((response.getResponseBody()).contains("testKey"));
                });
    }
}
