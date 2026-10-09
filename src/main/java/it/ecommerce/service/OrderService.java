package it.ecommerce.service;

import it.ecommerce.command.CreateOrderCommand;
import it.ecommerce.model.Order;

import java.util.List;

public interface OrderService {

    Order create(CreateOrderCommand command);

    List<Order> findAll();
}
