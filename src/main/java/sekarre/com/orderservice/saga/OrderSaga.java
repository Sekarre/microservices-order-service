package sekarre.com.orderservice.saga;

import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import sekarre.com.core.commands.ReserveProductCommand;
import sekarre.com.orderservice.core.events.OrderCreatedEvent;

@RequiredArgsConstructor
@Saga
public class OrderSaga {

    private final transient CommandGateway commandGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderCreatedEvent event) {
        ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
                .orderId(event.getOrderId())
                .productId(event.getProductId())
                .quantity(event.getQuantity())
                .userId(event.getUserId())
                .build();

        commandGateway.send(reserveProductCommand, (commandMessage, commandResultMessage) -> {
            if (commandResultMessage.isExceptional()) {
                //start transaction
            }
        });
    }
}
