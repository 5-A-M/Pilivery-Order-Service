package com.fiveam.orderservice.order.entity;

import com.fiveam.orderservice.audit.Auditable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Getter
@Setter
@Entity(name = "ITEM_ORDERS")
@NoArgsConstructor
@AllArgsConstructor
public class ItemOrder extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemOrderId;

    @Column(nullable = false)
    private Integer quantity;

    @Column
    @ColumnDefault("0")
    private Integer period;

    @Column(nullable = false)
    private boolean subscription;

    @Column(name = "NEXT_DELIVERY")
    private ZonedDateTime nextDelivery;

    @Column(name = "PAYMENT_DAY")
    private ZonedDateTime paymentDay;

    @Column
    private Long itemId;

    @ManyToOne
    @JoinColumn(name = "ORDER_ID")
    private Order order;

    public ItemOrder( ItemOrder origin){
        this.quantity = origin.getQuantity();
        this.period = origin.getPeriod();
        this.subscription = origin.isSubscription();
        this.nextDelivery = origin.getNextDelivery();
        this.paymentDay = origin.getPaymentDay();
        this.itemId = origin.getItemId();
        this.order = origin.getOrder();
    }

}
