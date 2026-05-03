package com.cube.order;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cube.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
