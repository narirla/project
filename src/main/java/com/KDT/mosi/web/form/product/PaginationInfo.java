package com.KDT.mosi.web.form.product;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaginationInfo {
  private int currentPage;
  private int totalPages;
  private int startPage;
  private int endPage;
  private long totalCount;
  private String selectedStatus;
}