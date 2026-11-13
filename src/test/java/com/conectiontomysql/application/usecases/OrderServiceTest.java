package com.conectiontomysql.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.conectiontomysql.domain.model.DetallePedido;
import com.conectiontomysql.domain.model.Pedido;
import com.conectiontomysql.domain.ports.out.OrderTransactionPort;

class OrderServiceTest {
    @Test
    void delegatesValidOrderToTransactionalPort() {
        RecordingOrderPort port = new RecordingOrderPort();
        OrderService service = new OrderService(port);
        Pedido order = order();

        service.create(order, List.of(detail(10, "P-1"), detail(10, "P-2")));

        assertEquals(order, port.order);
        assertEquals(2, port.details.size());
    }

    @Test
    void rejectsDuplicateProductsBeforePersistence() {
        RecordingOrderPort port = new RecordingOrderPort();
        OrderService service = new OrderService(port);

        assertThrows(IllegalArgumentException.class,
                () -> service.create(order(), List.of(detail(10, "P-1"), detail(10, "P-1"))));
        assertNull(port.order);
    }

    @Test
    void rejectsInvalidOrderDatesBeforePersistence() {
        RecordingOrderPort port = new RecordingOrderPort();
        OrderService service = new OrderService(port);
        Pedido invalidOrder = new Pedido(10, LocalDate.now(), LocalDate.now().minusDays(1),
                null, "Pendiente", null, 2);

        assertThrows(IllegalArgumentException.class,
                () -> service.create(invalidOrder, List.of(detail(10, "P-1"))));
        assertNull(port.order);
    }

    private Pedido order() {
        return new Pedido(10, LocalDate.now(), LocalDate.now().plusDays(7),
                null, "Pendiente", null, 2);
    }

    private DetallePedido detail(int orderId, String productId) {
        return new DetallePedido(orderId, productId, 1, BigDecimal.TEN, 1);
    }

    private static final class RecordingOrderPort implements OrderTransactionPort {
        private Pedido order;
        private List<DetallePedido> details = new ArrayList<>();

        @Override
        public void createAtomically(Pedido order, List<DetallePedido> details) {
            this.order = order;
            this.details = List.copyOf(details);
        }
    }
}
