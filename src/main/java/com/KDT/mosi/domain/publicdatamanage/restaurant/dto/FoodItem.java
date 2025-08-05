package com.KDT.mosi.domain.publicdatamanage.restaurant.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class FoodItem {
  @JacksonXmlProperty(localName = "UC_SEQ")
  private int ucSeq;

  @JacksonXmlProperty(localName = "GUGUN_NM")
  private String gugunNm;

  @JacksonXmlProperty(localName = "TITLE")
  private String title;

  @JacksonXmlProperty(localName = "SUBTITLE")
  private String subtitle;

  @JacksonXmlProperty(localName = "ADDR1")
  private String addr1;

  @JacksonXmlProperty(localName = "ADDR2")
  private String addr2;

  @JacksonXmlProperty(localName = "CNTCT_TEL")
  private String cntctTel;

  @JacksonXmlProperty(localName = "HOMEPAGE_URL")
  private String homepageUrl;

  @JacksonXmlProperty(localName = "USAGE_DAY_WEEK_AND_TIME")
  private String usageDayWeekAndTime;

  @JacksonXmlProperty(localName = "RPRSNTV_MENU")
  private String rprsntvMenu;

  @JacksonXmlProperty(localName = "MAIN_IMG_NORMAL")
  private String mainImgNormal;

  @JacksonXmlProperty(localName = "MAIN_IMG_THUMB")
  private String mainImgThumb;

  @JacksonXmlProperty(localName = "ITEMCNTNTS")
  private String itemcntnts;

  @JacksonXmlProperty(localName = "LAT")
  private double lat;

  @JacksonXmlProperty(localName = "LNG")
  private double lng;
}