package com.fiveam.orderservice.order.reposiroty;

import com.fiveam.orderservice.order.entity.Order;
import com.fiveam.orderservice.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findAllByUserIdAndSubscriptionAndOrderStatusNot(
            Pageable pageable, Long userId, boolean subscription, OrderStatus orderStatus1);

    Page<Order> findAllByUserIdAndSubscriptionAndOrderStatusNotAndOrderStatusNot(
            Pageable pageable, Long userId, boolean subscription, OrderStatus orderStatus1, OrderStatus orderStatus2
    );

    @Query("Select distinct o from ORDERS o join ITEM_ORDERS io on o.orderId = io.order.orderId " +
            "where io.itemId = :itemId and o.userId = :userId and o.orderStatus not in :status")
    List<Order> findByItemAndUser(@Param("itemId") long itemId, @Param("userId") long userId, @Param("status") OrderStatus status);

    Page<Order> findAllByUserIdAndOrderStatus(Pageable pageable, Long userid, OrderStatus orderStatus);
}
