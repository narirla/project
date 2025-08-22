package com.KDT.mosi.initializer;

import com.KDT.mosi.domain.publicdatamanage.facility.svc.FacilityDataProcessorService;
import com.KDT.mosi.domain.publicdatamanage.restaurant.svc.FoodDataProcessorService;
import com.KDT.mosi.domain.product.svc.ProductSearchService; // ✨✨✨ ProductSearchService 클래스를 임포트합니다.
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 시작 시 공공데이터를 불러와서 Elasticsearch에 저장하는 역할을 하는 클래스입니다.
 * CommandLineRunner를 구현하여 스프링 부트 애플리케이션이 실행된 직후 run() 메서드가 실행됩니다.
 *
 * @Component 어노테이션은 이 클래스를 스프링 빈으로 등록하여 의존성 주입(DI)을 받을 수 있게 합니다.
 * @RequiredArgsConstructor 어노테이션은 final 필드에 대한 생성자를 자동으로 생성하여 의존성 주입을 처리합니다.
 * @Slf4j는 Lombok 라이브러리로, 로깅을 위한 Logger 객체를 자동으로 생성합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

  // @RequiredArgsConstructor에 의해 자동으로 의존성 주입됩니다.
  private final FoodDataProcessorService foodDataProcessorService;
  private final FacilityDataProcessorService facilityDataProcessorService;
  private final ProductSearchService productSearchService; // ✨✨✨ ProductSearchService 필드 추가

  /**
   * 애플리케이션 시작 시 실행되는 메서드입니다.
   * `throws Exception`을 선언하여 호출하는 메서드가 던지는 예외를 처리하도록 합니다.
   * 이렇게 하면 예외 발생 시 애플리케이션 시작이 중단되고, 스프링이 예외를 적절하게 기록합니다.
   *
   * @param args 커맨드 라인 인자들
   * @throws Exception 데이터 처리 중 발생할 수 있는 모든 예외
   */
  @Override
  public void run(String... args) throws Exception {
    log.info(">>>>>> 애플리케이션 시작과 함께 공공데이터를 초기화합니다.");

    // 맛집 데이터 처리
    try {
      log.info(">>>>>> 부산 맛집 공공데이터 로딩을 시작합니다.");
      foodDataProcessorService.fetchAndProcessAllFoodData();
      log.info(">>>>>> 부산 맛집 공공데이터 로딩 완료.");
    } catch (Exception e) {
      log.error(">>>>>> 부산 맛집 데이터 로딩 중 오류가 발생했습니다: {}", e.getMessage(), e);
      // 예외를 잡아서 로그만 남기고, 다른 초기화 작업이 계속되도록 할 수도 있습니다.
      // throw e; // 만약 데이터 로딩 실패 시 애플리케이션을 중단하고 싶다면 이 코드를 사용합니다.
    }

    // 시설 데이터 처리
    try {
      log.info(">>>>>> 부산 시설 공공데이터 로딩을 시작합니다.");
      facilityDataProcessorService.fetchAndProcessAllFacilityData(); // FacilityDataProcessorService에 이 메서드가 있다고 가정합니다.
      log.info(">>>>>> 부산 시설 공공데이터 로딩 완료.");
    } catch (Exception e) {
      log.error(">>>>>> 부산 시설 데이터 로딩 중 오류가 발생했습니다: {}", e.getMessage(), e);
      // throw e;
    }

    // ✨✨✨ 새로 추가된 코드
    try {
      log.info(">>>>>> Oracle DB의 상품 데이터를 Elasticsearch에 인덱싱을 시작합니다.");
      productSearchService.indexAllProductsFromDB();
      log.info(">>>>>> Oracle DB 상품 데이터 인덱싱 완료.");
    } catch (Exception e) {
      log.error(">>>>>> Oracle DB 상품 데이터 인덱싱 중 오류가 발생했습니다: {}", e.getMessage(), e);
    }

    log.info(">>>>>> 공공데이터 초기화 프로세스 종료.");
  }
}