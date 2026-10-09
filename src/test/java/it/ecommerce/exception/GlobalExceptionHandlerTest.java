package it.ecommerce.exception;

import it.ecommerce.model.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsNotFoundTo404() {
        ProblemDetail problem = handler.handleNotFound(new ResourceNotFoundException("Customer", 1L));

        assertEquals(404, problem.getStatus());
        assertEquals("Customer with id 1 not found", problem.getDetail());
    }

    @Test
    void mapsConflictsTo409() {
        Product product = Product.builder().code("P1").stock(1).build();

        assertEquals(409, handler.handleConflict(new DuplicateResourceException("dup")).getStatus());
        ProblemDetail stock = handler.handleConflict(new InsufficientStockException(product, 5));
        assertEquals(409, stock.getStatus());
        Assertions.assertNotNull(stock.getDetail());
        Assertions.assertTrue(stock.getDetail().contains("requested 5, available 1"));
    }

    @Test
    void mapsValidationErrorsTo400() throws Exception {
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "request");
        result.addError(new FieldError("request", "email", "must not be blank"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(
                        GlobalExceptionHandlerTest.class.getDeclaredMethod("dummy", String.class), 0), result);

        ProblemDetail problem = handler.handleValidation(ex);

        assertEquals(400, problem.getStatus());
        assertEquals("email: must not be blank", problem.getDetail());
    }

    @Test
    void mapsUnexpectedErrorsTo500() {
        assertEquals(500, handler.handleUnexpected(new IllegalStateException("boom")).getStatus());
    }

    @SuppressWarnings("unused")
    private void dummy(String ignored) {
    }
}
