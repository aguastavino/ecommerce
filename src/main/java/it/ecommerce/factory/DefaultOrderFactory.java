package it.ecommerce.factory;

import it.ecommerce.model.Customer;
import it.ecommerce.model.Order;
import it.ecommerce.model.OrderItem;
import it.ecommerce.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DefaultOrderFactory implements OrderFactory {

    private final Clock clock;

    @Override
    public Order create(Customer customer, Map<Product, Integer> productQuantities) {
        Order order = Order.builder()
                .customer(customer)
                .createdAt(LocalDateTime.now(clock))
                .build();
        productQuantities.forEach((product, quantity) ->
                order.addItem(OrderItem.builder().product(product).quantity(quantity).build()));
        return order;
    }
}
