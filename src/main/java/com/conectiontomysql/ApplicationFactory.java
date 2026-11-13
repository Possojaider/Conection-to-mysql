package com.conectiontomysql;

import com.conectiontomysql.application.ports.in.CreateOrderUseCase;
import com.conectiontomysql.application.ports.in.EntityManagementUseCase;
import com.conectiontomysql.application.usecases.EntityManagementService;
import com.conectiontomysql.application.usecases.OrderService;
import com.conectiontomysql.infrastructure.adapters.out.persistence.JdbcEntityManagementAdapter;
import com.conectiontomysql.infrastructure.adapters.out.persistence.JdbcOrderTransactionAdapter;

public final class ApplicationFactory {
    private ApplicationFactory() {
    }

    public static CreateOrderUseCase createOrderUseCase() {
        return new OrderService(new JdbcOrderTransactionAdapter(DatabaseConfig.getInstance()));
    }

    public static EntityManagementUseCase createEntityManagementUseCase() {
        return new EntityManagementService(new JdbcEntityManagementAdapter(DatabaseConfig.getInstance()));
    }
}
