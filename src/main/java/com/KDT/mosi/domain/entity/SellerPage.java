package com.KDT.mosi.domain.entity;

import java.sql.Timestamp;

public class SellerPage {

  private Long pageId;           // 마이페이지 ID
  private Long memberId;         // 회원 ID (FK)
  private byte[] image;          // 프로필 이미지
  private String intro;          // 자기소개글
  private int salesCount;        // 누적 판매 건수
  private double reviewAvg;      // 평균 평점
  private Timestamp createDate;  // 생성일시
  private Timestamp updateDate;  // 수정일시

  // 기본 생성자
  public SellerPage() {
  }

  // 모든 필드를 포함한 생성자
  public SellerPage(Long pageId, Long memberId, byte[] image, String intro,
                    int salesCount, double reviewAvg, Timestamp createDate, Timestamp updateDate) {
    this.pageId = pageId;
    this.memberId = memberId;
    this.image = image;
    this.intro = intro;
    this.salesCount = salesCount;
    this.reviewAvg = reviewAvg;
    this.createDate = createDate;
    this.updateDate = updateDate;
  }

  // Getter / Setter
  public Long getPageId() {
    return pageId;
  }

  public void setPageId(Long pageId) {
    this.pageId = pageId;
  }

  public Long getMemberId() {
    return memberId;
  }

  public void setMemberId(Long memberId) {
    this.memberId = memberId;
  }

  public byte[] getImage() {
    return image;
  }

  public void setImage(byte[] image) {
    this.image = image;
  }

  public String getIntro() {
    return intro;
  }

  public void setIntro(String intro) {
    this.intro = intro;
  }

  public int getSalesCount() {
    return salesCount;
  }

  public void setSalesCount(int salesCount) {
    this.salesCount = salesCount;
  }

  public double getReviewAvg() {
    return reviewAvg;
  }

  public void setReviewAvg(double reviewAvg) {
    this.reviewAvg = reviewAvg;
  }

  public Timestamp getCreateDate() {
    return createDate;
  }

  public void setCreateDate(Timestamp createDate) {
    this.createDate = createDate;
  }

  public Timestamp getUpdateDate() {
    return updateDate;
  }

  public void setUpdateDate(Timestamp updateDate) {
    this.updateDate = updateDate;
  }

  // toString (디버깅용)
  @Override
  public String toString() {
    return "SellerPage{" +
        "pageId=" + pageId +
        ", memberId=" + memberId +
        ", intro='" + intro + '\'' +
        ", salesCount=" + salesCount +
        ", reviewAvg=" + reviewAvg +
        ", createDate=" + createDate +
        ", updateDate=" + updateDate +
        '}';
  }
}
