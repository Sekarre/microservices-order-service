package sekarre.com.orderservice.core.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import sekarre.com.orderservice.core.model.OrderStatus;

@Value
public class OrderApprovedEvent {
    String orderId;
    OrderStatus orderStatus = OrderStatus.APPROVED;
}
