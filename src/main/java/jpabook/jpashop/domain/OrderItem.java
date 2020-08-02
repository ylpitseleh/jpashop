package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private long id;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private int OrderPrice; // 주문 가격
    private int count; // 주문 수량

    //==생성 메서드==//
    // 주문 상품, 가격, 수량 정보를 사용해서 주문상품 엔티티를 생성한다.
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);
        // 주문한 수량만큼 상품의 재고룰 줄인다.
        item.removeStock(count);
        return orderItem;
    }

    //==비즈니스 로직==//
    // 주문 취소시에 취소된 주문에 담겼던 OrderItem들도 주문 수량만큼 취소해줘야 함.
    public void cancel() {
        // 재고 수량을 원상 복귀
        getItem().addStock(count);
    }
    // 전체 주문 가격 조회할 때 필요
    public int getTotalPrice() {
        return getOrderPrice() * getCount();
    }
}
