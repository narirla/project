// com.KDT.mosi.web.config.ElasticSearchConfig.java

package com.KDT.mosi.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = {
    "com.KDT.mosi.domain.publicdatamanage.facility.repository",
    "com.KDT.mosi.domain.publicdatamanage.restaurant.document",
    "com.KDT.mosi.domain.product.document",
    "com.KDT.mosi.domain.product.search.document"
})
public class ElasticSearchConfig extends ElasticsearchConfiguration {

  @Value("${elasticsearch.host}")
  private String elasticsearchHost;

  @Value("${elasticsearch.port}")
  private int elasticsearchPort;

  @Override
  public ClientConfiguration clientConfiguration() {
    return ClientConfiguration.builder()
        .connectedTo(elasticsearchHost + ":" + elasticsearchPort)
        .build();
  }
}