package com.KDT.mosi.web.config;// ElasticSearchConfig.java 수정 (아마도 이 부분이 문제일 것입니다)

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

@Configuration
// ...
public class ElasticSearchConfig extends ElasticsearchConfiguration {

  @Value("${spring.elasticsearch.rest.uris}")
  private String elasticsearchUris;

  @Override
  public ClientConfiguration clientConfiguration() {
    // http://localhost:9200 이라는 전체 문자열을 connectedTo에 바로 전달하면 오류가 발생합니다.
    // connectedTo는 "host:port" 포맷을 기대합니다.
    String host = "localhost";
    int port = 9200;

    // 따라서 uris를 파싱해야 하지만, 간단하게 yml을 다시 수정하는 것이 낫습니다.
    return ClientConfiguration.builder()
        .connectedTo(host + ":" + port) // 이렇게 host와 port를 분리해서 넘겨줘야 합니다.
        .build();
  }
}