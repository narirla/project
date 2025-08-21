package com.KDT.mosi.web.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

  @Bean
  public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();

    // XML 응답을 처리하기 위한 컨버터 추가
    restTemplate.getMessageConverters().add(new MappingJackson2XmlHttpMessageConverter(new XmlMapper()));

    // JSON 응답을 처리하기 위한 컨버터 추가 (기존에 사용하던 컨버터)
    restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

    return restTemplate;
  }
}