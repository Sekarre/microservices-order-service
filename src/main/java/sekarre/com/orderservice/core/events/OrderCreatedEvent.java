package sekarre.com.orderservice.core.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sekarre.com.orderservice.core.model.OrderStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private String orderId;
    private String productId;
    private String userId;
    private int quantity;
    private String addressId; 
    private OrderStatus orderStatus;
}
