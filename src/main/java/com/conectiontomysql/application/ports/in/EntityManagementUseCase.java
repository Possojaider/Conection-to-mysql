package com.conectiontomysql.application.ports.in;

import java.util.List;

import com.conectiontomysql.application.model.EntityOption;
import com.conectiontomysql.application.model.EntityTable;
import com.conectiontomysql.application.model.EntityValue;

public interface EntityManagementUseCase {
    EntityTable load(String tableName);

    List<EntityOption> loadOptions(String tableName, String keyColumn);

    int insert(String tableName, List<EntityValue> values);

    int update(String tableName, List<EntityValue> values, List<EntityValue> keyValues);

    int delete(String tableName, List<EntityValue> keyValues);
}
