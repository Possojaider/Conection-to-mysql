package com.conectiontomysql.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.conectiontomysql.application.model.EntityOption;
import com.conectiontomysql.application.model.EntityTable;
import com.conectiontomysql.application.model.EntityValue;
import com.conectiontomysql.application.ports.out.EntityManagementPort;

class EntityManagementServiceTest {
    @Test
    void delegatesAllowedEntityOperations() {
        RecordingEntityPort port = new RecordingEntityPort();
        EntityManagementService service = new EntityManagementService(port);

        service.delete("cliente", List.of(new EntityValue("codigo_cliente", java.sql.Types.INTEGER, 12)));

        assertEquals("cliente", port.tableName);
        assertEquals(1, port.keyValues.size());
    }

    @Test
    void rejectsTablesOutsideTheSupportedModules() {
        RecordingEntityPort port = new RecordingEntityPort();
        EntityManagementService service = new EntityManagementService(port);

        assertThrows(IllegalArgumentException.class, () -> service.load("users"));
        assertNull(port.tableName);
    }

    private static final class RecordingEntityPort implements EntityManagementPort {
        private String tableName;
        private List<EntityValue> keyValues = List.of();

        @Override
        public EntityTable load(String tableName) {
            this.tableName = tableName;
            return null;
        }

        @Override
        public List<EntityOption> loadOptions(String tableName, String keyColumn) {
            this.tableName = tableName;
            return List.of();
        }

        @Override
        public int insert(String tableName, List<EntityValue> values) {
            this.tableName = tableName;
            return 0;
        }

        @Override
        public int update(String tableName, List<EntityValue> values, List<EntityValue> keyValues) {
            this.tableName = tableName;
            this.keyValues = keyValues;
            return 0;
        }

        @Override
        public int delete(String tableName, List<EntityValue> keyValues) {
            this.tableName = tableName;
            this.keyValues = keyValues;
            return 1;
        }
    }
}
