package com.KDT.mosi.domain.entity;

import lombok.Data;

@Data
public class Role {
  private String roleId;     // R01, R02
  private String roleName;   // 구매자, 판매자
}
