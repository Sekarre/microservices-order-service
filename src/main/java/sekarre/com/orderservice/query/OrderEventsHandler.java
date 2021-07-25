
package sekarre.com.orderservice.query;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import sekarre.com.orderservice.core.data.OrderEntity;
import sekarre.com.orderservice.core.data.OrdersRepository;
import sekarre.com.orderservice.core.events.OrderApprovedEvent;
import sekarre.com.orderservice.core.events.OrderCreatedEvent;

@Component
@ProcessingGroup("order-group")
public class OrderEventsHandler {
    
    private final OrdersRepository ordersRepository;
    
    public OrderEventsHandler(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    @EventHandler
    public void on(OrderCreatedEvent event) throws Exception {
        OrderEntity orderEntity = new OrderEntity();
        BeanUtils.copyProperties(event, orderEntity);
 
        this.ordersRepository.save(orderEntity);
    }

    @EventHandler
    public void on(OrderApprovedEvent event) {
        var order = ordersRepository.findByOrderId(event.getOrderId());

        if (order == null) {
            //todo
            return;
        }

        order.setOrderStatus(event.getOrderStatus());
        ordersRepository.save(order);
    }
    
}
