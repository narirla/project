package com.KDT.mosi.domain.product.svc;

import com.KDT.mosi.domain.entity.Product;
import com.KDT.mosi.domain.product.dao.ProductDAO;
import com.KDT.mosi.domain.product.svc.ProductSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ProductSVCImpl implements ProductSVC {

  private final ProductDAO productDAO;

  @Override
  public Product save(Product product) {
    log.info("Saving product: {}", product);
    return productDAO.save(product);
  }

  @Override
  public List<Product> findAll(int pageNo, int numOfRows) {
    log.info("Fetching product list: pageNo={}, numOfRows={}", pageNo, numOfRows);
    return productDAO.findAll(pageNo, numOfRows);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Product> findById(Long id) {
    log.info("Fetching product by ID (before pay): {}", id);
    return productDAO.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Product> findByIdAfterPay(Long id) {
    log.info("Fetching product by ID (after pay): {}", id);
    return productDAO.findByIdAfterPay(id);
  }

  @Override
  public Product updateById(Long productId, Product product) {
    log.info("Updating product with ID {}: {}", productId, product);
    return productDAO.updateById(productId, product);
  }

  @Override
  public void deleteById(Long id) {
    log.info("Deleting product by id: {}", id);
    productDAO.deleteById(id);
  }

  @Override
  public void deleteByIds(List<Long> ids) {
    log.info("Deleting products by ids: {}", ids);
    productDAO.deleteByIds(ids);
  }

  @Override
  @Transactional(readOnly = true)
  public long getTotalCount() {
    log.info("Getting total count of products.");
    return productDAO.getTotalCount();
  }
}