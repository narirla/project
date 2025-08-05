package com.KDT.mosi.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ImageProxyController {

  private final RestTemplate restTemplate;

  /**
   * 외부 이미지 URL을 프록시하여 반환하는 엔드포인트입니다.
   * @param url 프록시할 외부 이미지의 원본 URL
   * @return 이미지 데이터가 포함된 ResponseEntity
   */
  @GetMapping("/image-proxy")
  public ResponseEntity<byte[]> proxyImage(@RequestParam(name = "url") String url) {
    log.info("Image proxy request for URL: {}", url);

    // URL 유효성 검사 및 파싱
    URI uri;
    try {
      uri = new URI(url);
    } catch (URISyntaxException e) {
      log.error("Invalid URL syntax: {}", url, e);
      // 잘못된 URL인 경우 400 Bad Request 응답 반환
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    try {
      // RestTemplate을 사용하여 외부 이미지 데이터를 byte[]로 가져옵니다.
      ResponseEntity<byte[]> response = restTemplate.getForEntity(uri, byte[].class);

      // 외부 API로부터 받은 HTTP 상태 코드가 성공(2xx)인 경우
      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        // 응답 헤더를 설정합니다. (Content-Type을 원래 이미지의 타입으로 설정)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(response.getHeaders().getContentType());
        headers.setContentLength(response.getHeaders().getContentLength());
        log.info("Successfully proxied image. Content-Type: {}, Content-Length: {}",
            headers.getContentType(), headers.getContentLength());

        // 이미지 데이터와 헤더를 함께 클라이언트에게 반환합니다.
        return new ResponseEntity<>(response.getBody(), headers, HttpStatus.OK);
      }
      log.warn("Failed to get image from URL with success status. Status: {}", response.getStatusCode());
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);

    } catch (HttpClientErrorException | HttpServerErrorException e) {
      // 외부 서버에서 오류가 발생한 경우 (예: 404 Not Found, 500 Internal Server Error 등)
      log.error("HTTP error while fetching image from {}: Status {} - {}", url, e.getStatusCode(), e.getMessage());
      return new ResponseEntity<>(e.getStatusCode());
    } catch (Exception e) {
      // 기타 예상치 못한 오류가 발생한 경우
      log.error("Unexpected error while fetching image from {}: {}", url, e.getMessage());
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}