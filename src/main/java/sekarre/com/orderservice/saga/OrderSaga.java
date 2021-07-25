package sekarre.com.orderservice.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;
import sekarre.com.core.commands.ProcessPaymentCommand;
import sekarre.com.core.commands.ReserveProductCommand;
import sekarre.com.core.events.PaymentProcessedEvent;
import sekarre.com.core.events.ProductReservedEvent;
import sekarre.com.core.model.User;
import sekarre.com.core.query.FetchUserPaymentDetailsQuery;
import sekarre.com.orderservice.command.commands.ApprovedOrderCommand;
import sekarre.com.orderservice.core.events.OrderApprovedEvent;
import sekarre.com.orderservice.core.events.OrderCreatedEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Saga
public class OrderSaga {

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient QueryGateway queryGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderCreatedEvent event) {
        ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
                .orderId(event.getOrderId())
                .productId(event.getProductId())
                .quantity(event.getQuantity())
                .userId(event.getUserId())
                .build();

        log.info("order created event");

        commandGateway.send(reserveProductCommand, (commandMessage, commandResultMessage) -> {
            if (commandResultMessage.isExceptional()) {
                //start transaction
            }
        });
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservedEvent productReservedEvent) {

        FetchUserPaymentDetailsQuery fetchUserPaymentDetailsQuery =
                new FetchUserPaymentDetailsQuery(productReservedEvent.getUserId());

        User user = null;
        try {
            user = queryGateway.query(fetchUserPaymentDetailsQuery, ResponseTypes.instanceOf(User.class)).join();
        } catch (Exception ex) {
            //start compensating transaction
            return;
        }

        if (user == null) {
            //start compensating transaction
            return;
        }

        log.info("Succesfully fetched user with name: " + user.getFirstName());

        ProcessPaymentCommand processPaymentCommand = ProcessPaymentCommand.builder()
                .orderId(productReservedEvent.getOrderId())
                .paymentDetails(user.getPaymentDetails())
                .paymentId(UUID.randomUUID().toString())
                .build();

        String result;

        try {
            result = commandGateway.sendAndWait(processPaymentCommand, 10, TimeUnit.SECONDS);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            //start compensating transaction
            return;
        }

        if (result == null) {
            log.info("ProcessPaymentCommand result is null");
            //start compensating transaction
        }

    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentProcessedEvent paymentProcessedEvent) {
        ApprovedOrderCommand approvedOrderCommand = new ApprovedOrderCommand(paymentProcessedEvent.getOrderId());

        commandGateway.send(approvedOrderCommand);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderApprovedEvent orderApprovedEvent) {
        log.info("Saga is complete for order id:" + orderApprovedEvent.getOrderId());
    }
}
