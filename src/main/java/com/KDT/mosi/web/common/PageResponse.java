// src/main/java/com/KDT/mosi/web/common/PageResponse.java
package com.KDT.mosi.web.common;


import java.util.List;

public record PageResponse<T>(
    List<T> content, int page, int size, long totalElements, int totalPages, boolean first, boolean last
) {
  public static <T> PageResponse<T> of(List<T> content, int page, int size, long total){
    int totalPages = (int)Math.ceil((double)total / size);
    return new PageResponse<>(content, page, size, total, totalPages, page==0, page>=totalPages-1);
  }
}
