package com.example.oms.ws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Sinks;

import java.util.Map;

@Configuration
public class OrderStream {

  public static final Sinks.Many<String> ORDER_UPDATES = Sinks.many().multicast().onBackpressureBuffer();

  @Bean
  HandlerMapping wsMapping() {
    Map<String, WebSocketHandler> map = Map.of("/ws/orders", session ->
      session.send(ORDER_UPDATES.asFlux().map(session::textMessage))
    );
    SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
    mapping.setOrder(10); mapping.setUrlMap(map);
    return mapping;
  }

  @Bean
  WebSocketHandlerAdapter wsAdapter(){ return new WebSocketHandlerAdapter(); }
}
