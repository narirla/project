# 🌍 MO:SI - 개인 간 여행 상품 거래 플랫폼

## 1. 프로젝트 개요

- **프로젝트명**: MO:SI 
- **기간**: 2025.05.29 ~ 2025.07.25 
- **인원**: 5명 
- **주제**: 개인 간 여행 코스를 공유하고 거래할 수 있는 플랫폼 개발  

---

## 2. 기획 배경

- 여행 트렌드 변화  
  - 유명 관광지 → **개인 맞춤 여행 증가**  
- 혼행, 반려동물 동반 여행 증가  
- 고령층 및 취약계층 여행 수요 확대  

👉 기존 플랫폼의 한계  
- 획일적인 관광 정보  
- 개인 맞춤형 콘텐츠 부족  

👉 해결 방향  
- 사용자 중심 여행 콘텐츠 공유 플랫폼 구축
---

## 3. 핵심 기능

### 👤 회원 관리
- 회원가입 / 로그인 / 로그아웃  
- 구매자 ↔ 판매자 역할 전환  
- 마이페이지 (정보 수정)

---

### 🛍 상품 관리 (핵심 기능)
- 여행 코스 상품 등록 / 수정 / 삭제  
- 상품 상세 조회  
- 상품 정렬 및 검색  

---

### 💬 커뮤니티
- 게시글 등록 / 수정 / 삭제  
- 댓글 기능  
- 좋아요 / 신고 기능  

---

### 🗺 관광 정보
- 부산 맛집 조회  
- 교통약자 편의시설 조회  

---

## 4. 기술 스택

### 🖥 Backend
![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=springboot)
![Spring MVC](https://img.shields.io/badge/SpringMVC-6DB33F?style=for-the-badge)

### 🎨 Frontend
![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript)

### 🗄 Database
![Oracle](https://img.shields.io/badge/Oracle-F80000?style=for-the-badge&logo=oracle)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=elasticsearch)

### ⚙️ 기타
![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle)
![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman)

---

## 5. 시스템 구조

### 아키텍처

- Controller → Service → DAO 구조  
- Spring MVC 기반 계층형 구조  
- REST API + AJAX 통신  

👉 SSR + 일부 CSR 혼합 구조 

---

### 주요 특징

- 역할 기반 시스템 (구매자 / 판매자 전환)
- 상품 중심 구조 설계
- 게시판 + 댓글 + 신고 기능 포함
- Elasticsearch 기반 검색 확장 가능 구조

---

## 6. 데이터베이스 구조

- 주요 테이블  
  - MEMBER / ROLE / MEMBER_ROLE  
  - PRODUCT / PRODUCT_IMAGE / COURSE  
  - BBS / COMMENT  
  - BUYER_PAGE / SELLER_PAGE  

👉 ERD 기반 설계 후 구현

---

## 7. 내가 수행한 역할

- 회원 기능 (로그인, 회원가입, 역할 전환) 백엔드/프론트 개발  
- 마이페이지 기능 구현 (구매자/판매자 분리 구조)  
- 회원 정보 수정 및 세션 관리  
- 전체 기능 개발 지원 및 통합  

👉 서비스 전체 흐름 이해 기반 개발 수행 
---

## 8. 주요 성과

- 구매자 ↔ 판매자 **동적 역할 전환 시스템 구현**
- 상품 / 커뮤니티 / 회원 기능 통합 플랫폼 구축
- Git 기반 협업 및 브랜치 관리 경험
- 풀스택 개발 경험 확보
---

## 9. 한계 및 개선 방향

### 한계
- 협업 프로세스 미흡  
- DB 설계 초기 완성도 부족  
- 부가 기능 (유효성 검사 등) 부족  

---

### 개선 방향

- Git Flow 기반 협업 체계 구축  
- 코드 리뷰 및 문서화 강화  
- 유효성 검사 및 사용자 UX 개선  
- 지도 기반 기능 강화  

---

## 10. 인사이트

- 설계 단계가 개발보다 더 중요함을 체감  
- 단순 기능 구현이 아닌  
👉 **전체 시스템 구조 이해가 핵심**

- 실제 서비스 개발은  
👉 **협업 + 구조 + 설계의 종합 결과물**
