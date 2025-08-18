package com.KDT.mosi.domain.order.dao;

import com.KDT.mosi.domain.entity.order.Order;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderDAOImpl implements OrderDAO {

  private final EntityManager entityManager;

  @Override
  public Order insert(Order order) {
    entityManager.persist(order);
    entityManager.flush();
    return order;
  }

  @Override
  public Order update(Order order) {
    return entityManager.merge(order);
  }

  @Override
  public void delete(Long orderId) {
    Order order = entityManager.find(Order.class, orderId);
    if (order != null) {
      entityManager.remove(order);
    }
  }

  @Override
  public Optional<Order> findById(Long orderId) {
    Order order = entityManager.find(Order.class, orderId);
    return Optional.ofNullable(order);
  }

  @Override
  public Optional<Order> findByOrderCodeAndBuyerId(String orderCode, Long buyerId) {
    try {
      TypedQuery<Order> query = entityManager.createQuery(
          "SELECT o FROM Order o WHERE o.orderCode = :orderCode AND o.buyerId = :buyerId",
          Order.class);
      query.setParameter("orderCode", orderCode);
      query.setParameter("buyerId", buyerId);
      Order order = query.getSingleResult();
      return Optional.of(order);
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }

  @Override
  public List<Order> findByBuyerIdOrderByOrderDateDesc(Long buyerId) {
    TypedQuery<Order> query = entityManager.createQuery(
        "SELECT o FROM Order o WHERE o.buyerId = :buyerId ORDER BY o.orderDate DESC",
        Order.class);
    query.setParameter("buyerId", buyerId);
    return query.getResultList();
  }

  @Override
  public Optional<Order> findByOrderCode(String orderCode) {
    try {
      TypedQuery<Order> query = entityManager.createQuery(
          "SELECT o FROM Order o WHERE o.orderCode = :orderCode",
          Order.class);
      query.setParameter("orderCode", orderCode);
      Order order = query.getSingleResult();
      return Optional.of(order);
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }
}
