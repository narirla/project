package com.KDT.mosi.web.config;

// src/main/java/com/mosi/chat/config/WebSocketConfig.java
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // 브라우저 → 서버 WebSocket 연결 entrypoint
    registry.addEndpoint("/ws")
        .setAllowedOriginPatterns("*")   // 개발 편의용. 운영에선 도메인 제한
        .withSockJS();                   // SockJS 폴백(방화벽/프록시 환경 대응)
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    // 서버가 내보내는(브로드캐스트) 대상 prefix
    registry.enableSimpleBroker("/topic");
    // 클라이언트가 서버로 보낼 때 prefix
    registry.setApplicationDestinationPrefixes("/app");
  }
}

