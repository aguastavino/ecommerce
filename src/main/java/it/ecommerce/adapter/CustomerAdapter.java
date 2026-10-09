package it.ecommerce.adapter;

import it.ecommerce.dto.CustomerRequest;
import it.ecommerce.dto.CustomerResponse;
import it.ecommerce.model.Customer;
import org.springframework.stereotype.Component;

/**
 * Adapts the REST representation of a customer to the domain model and back.
 */
@Component
public class CustomerAdapter {

    public Customer toEntity(CustomerRequest request) {
        return Customer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .birthDate(request.birthDate())
                .taxCode(request.taxCode().toUpperCase())
                .email(request.email())
                .build();
    }

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getFirstName(), customer.getLastName(),
                customer.getBirthDate(), customer.getTaxCode(), customer.getEmail());
    }
}
