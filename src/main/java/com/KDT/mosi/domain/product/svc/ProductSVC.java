package com.KDT.mosi.domain.product.svc;

import com.KDT.mosi.domain.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductSVC {

  Product save(Product product);

  List<Product> findAll(int pageNo, int numOfRows);

  Optional<Product> findById(Long id);

  Optional<Product> findByIdAfterPay(Long id);

  Product updateById(Long productId, Product product);

  void deleteById(Long id);

  void deleteByIds(List<Long> ids);

  long getTotalCount();
}