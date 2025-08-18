package com.KDT.mosi.domain.order.dao;

import com.KDT.mosi.domain.entity.order.OrderItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderItemDAOImpl implements OrderItemDAO {

  private final EntityManager entityManager;

  @Override
  public OrderItem insert(OrderItem orderItem) {
    entityManager.persist(orderItem);
    entityManager.flush();
    return orderItem;
  }

  @Override
  public OrderItem update(OrderItem orderItem) {
    return entityManager.merge(orderItem);
  }

  @Override
  public void delete(Long orderItemId) {
    OrderItem orderItem = entityManager.find(OrderItem.class, orderItemId);
    if (orderItem != null) {
      entityManager.remove(orderItem);
    }
  }

  @Override
  public Optional<OrderItem> findById(Long orderItemId) {
    OrderItem orderItem = entityManager.find(OrderItem.class, orderItemId);
    return Optional.ofNullable(orderItem);
  }

  @Override
  public List<OrderItem> findByOrderId(Long orderId) {
    TypedQuery<OrderItem> query = entityManager.createQuery(
        "SELECT oi FROM OrderItem oi WHERE oi.order.orderId = :orderId",
        OrderItem.class);
    query.setParameter("orderId", orderId);
    return query.getResultList();
  }

  @Override
  public Optional<OrderItem> findByOrderIdAndProductId(Long orderId, Long productId) {
    try {
      TypedQuery<OrderItem> query = entityManager.createQuery(
          "SELECT oi FROM OrderItem oi WHERE oi.order.orderId = :orderId " +
              "AND oi.productId = :productId",
          OrderItem.class);
      query.setParameter("orderId", orderId);
      query.setParameter("productId", productId);
      OrderItem orderItem = query.getSingleResult();
      return Optional.of(orderItem);
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }
}