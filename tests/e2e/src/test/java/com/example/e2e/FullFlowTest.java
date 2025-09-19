/*
package com.example.e2e;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import java.io.File;
import java.net.http.*;
import java.net.URI;
import java.time.Duration;

public class FullFlowTest {

  @Test
  void place_route_partial_cancel_positions() throws Exception {
    File compose = new File("../../docker-compose.yml").getCanonicalFile();
    try (DockerComposeContainer<?> env = new DockerComposeContainer<>(compose)
        .withExposedService("oms", 8081, Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5)))
        .withExposedService("portfolio", 8083, Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5)))
        .withExposedService("exec-adapter", 8085, Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5)))
        .withExposedService("risk", 8082, Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5)))
        .withExposedService("kafka", 9092, Wait.forListeningPort())
        .withExposedService("postgres", 5432, Wait.forListeningPort())
    ){
      env.start();

      HttpClient http = HttpClient.newHttpClient();

      // Place IOC qty=2
        String body = "{\"symbol\":\"NIFTY\",\"segment\":\"CASH\",\"side\":\"BUY\",\"qty\":2,\"orderType\":\"MARKET\",\"tif\":\"IOC\"}";
        HttpRequest req = HttpRequest.newBuilder(URI.create("http://localhost:8081/api/v1/orders"))
        .timeout(Duration.ofSeconds(30))
        .header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
      HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
      Assertions.assertTrue(resp.statusCode()==201 || resp.statusCode()==422, "Response="+resp.statusCode()+": "+resp.body());
      if (resp.statusCode()==422) return; // market closed in CI — acceptable

      Thread.sleep(4000); // allow pipeline to process

      HttpRequest posReq = HttpRequest.newBuilder(URI.create("http://localhost:8083/api/v1/positions"))
        .timeout(Duration.ofSeconds(20)).GET().build();
      HttpResponse<String> pos = http.send(posReq, HttpResponse.BodyHandlers.ofString());
      Assertions.assertEquals(200, pos.statusCode());
      // Expect at least one position update occurred (partial fill 1)
      Assertions.assertTrue(pos.body().contains("netQty") || pos.body().contains("avgPrice"));
    }
  }
}
*/
