/*
package com.example.e2e;

import org.junit.jupiter.api.*;
import org.springframework.boot.SpringApplication;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.net.http.*;
import java.net.URI;
import java.time.Duration;

public class FullFlowSpringTest {

  static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));
  static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"));

  static Thread omsT, riskT, mdT, execT, pfT, outboxT;

  @BeforeAll
  static void up() throws Exception {
    kafka.start();
    pg.start();

    String kafkaUrl = kafka.getBootstrapServers();
    String pgHost = pg.getHost();
    Integer pgPort = pg.getFirstMappedPort();

    Runnable runOms = () -> SpringApplication.run(com.example.oms.OrderManagementApplication.class,
      "--spring.kafka.bootstrap-servers="+kafkaUrl,
      "--spring.datasource.url=jdbc:postgresql://"+pgHost+":"+pgPort+"/oms_db",
      "--spring.datasource.username="+pg.getUsername(),
      "--spring.datasource.password="+pg.getPassword()
    );
    Runnable runRisk = () -> SpringApplication.run(com.example.risk.OrderManagementApplication.class,
      "--spring.kafka.bootstrap-servers="+kafkaUrl
    );
    Runnable runMd = () -> SpringApplication.run(com.example.marketdata.OrderManagementApplication.class);
    Runnable runExec = () -> SpringApplication.run(com.example.exec.adapter.OrderManagementApplication.class,
      "--spring.kafka.bootstrap-servers="+kafkaUrl,
      "--spring.datasource.url=jdbc:postgresql://"+pgHost+":"+pgPort+"/exec_adapter_db",
      "--spring.datasource.username="+pg.getUsername(),
      "--spring.datasource.password="+pg.getPassword()
    );
    Runnable runPf = () -> SpringApplication.run(com.example.portfolio.OrderManagementApplication.class,
      "--spring.kafka.bootstrap-servers="+kafkaUrl,
      "--spring.datasource.url=jdbc:postgresql://"+pgHost+":"+pgPort+"/portfolio_db",
      "--spring.datasource.username="+pg.getUsername(),
      "--spring.datasource.password="+pg.getPassword()
    );
    Runnable runOutbox = () -> SpringApplication.run(com.example.outboxdispatcher.OrderManagementApplication.class,
      "--spring.kafka.bootstrap-servers="+kafkaUrl,
      "--spring.datasource.url=jdbc:postgresql://"+pgHost+":"+pgPort+"/oms_db",
      "--spring.datasource.username="+pg.getUsername(),
      "--spring.datasource.password="+pg.getPassword()
    );

    omsT = new Thread(runOms); omsT.start();
    riskT = new Thread(runRisk); riskT.start();
    mdT = new Thread(runMd); mdT.start();
    execT = new Thread(runExec); execT.start();
    pfT = new Thread(runPf); pfT.start();
    outboxT = new Thread(runOutbox); outboxT.start();

    Thread.sleep(8000);
  }

  @AfterAll
  static void down(){
    try { pg.stop(); } catch (Exception ignored){}
    try { kafka.stop(); } catch (Exception ignored){}
  }

  @Test
  void place_ioc_flow() throws Exception {
    HttpClient http = HttpClient.newHttpClient();
      String body = "{\"symbol\":\"NIFTY\",\"segment\":\"CASH\",\"side\":\"BUY\",\"qty\":2,\"orderType\":\"MARKET\",\"tif\":\"IOC\"}";
      HttpRequest req = HttpRequest.newBuilder(URI.create("http://localhost:8081/api/v1/orders"))
        .timeout(Duration.ofSeconds(20))
        .header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
    HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
    Assertions.assertTrue(resp.statusCode()==201 || resp.statusCode()==422);
  }
}
*/
