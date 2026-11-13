package com.conectiontomysql.domain;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.conectiontomysql.domain.model.DetallePedido;
import com.conectiontomysql.domain.model.Producto;

class DomainValidationTest {
    @Test
    void rejectsNegativeProductStock() {
        assertThrows(IllegalArgumentException.class, () -> new Producto(
                "P-1", "Planta", "Ornamentales", null, null, null,
                -1, BigDecimal.TEN, null));
    }

    @Test
    void rejectsNonPositiveOrderLineQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new DetallePedido(
                1, "P-1", 0, BigDecimal.ONE, 1));
    }

}
