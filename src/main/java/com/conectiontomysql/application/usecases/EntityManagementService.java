package com.conectiontomysql.application.usecases;

import java.util.List;
import java.util.Objects;

import com.conectiontomysql.application.model.EntityOption;
import com.conectiontomysql.application.model.EntityTable;
import com.conectiontomysql.application.model.EntityValue;
import com.conectiontomysql.application.ports.in.EntityManagementUseCase;
import com.conectiontomysql.application.ports.out.EntityManagementPort;

public final class EntityManagementService implements EntityManagementUseCase {
    private final EntityManagementPort entityManagementPort;

    public EntityManagementService(EntityManagementPort entityManagementPort) {
        this.entityManagementPort = Objects.requireNonNull(entityManagementPort);
    }

    @Override
    public EntityTable load(String tableName) {
        return entityManagementPort.load(requireTable(tableName));
    }

    @Override
    public List<EntityOption> loadOptions(String tableName, String keyColumn) {
        return entityManagementPort.loadOptions(requireTable(tableName), Objects.requireNonNull(keyColumn));
    }

    @Override
    public int insert(String tableName, List<EntityValue> values) {
        return entityManagementPort.insert(requireTable(tableName), List.copyOf(values));
    }

    @Override
    public int update(String tableName, List<EntityValue> values, List<EntityValue> keyValues) {
        return entityManagementPort.update(requireTable(tableName), List.copyOf(values), List.copyOf(keyValues));
    }

    @Override
    public int delete(String tableName, List<EntityValue> keyValues) {
        return entityManagementPort.delete(requireTable(tableName), List.copyOf(keyValues));
    }

    private String requireTable(String tableName) {
        if (!List.of("oficina", "empleado", "cliente", "gama_producto",
                "producto", "pedido", "detalle_pedido", "pago").contains(tableName)) {
            throw new IllegalArgumentException("Entidad no permitida: " + tableName);
        }
        return tableName;
    }
}
