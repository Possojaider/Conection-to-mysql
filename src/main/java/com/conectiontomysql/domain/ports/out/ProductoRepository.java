package com.conectiontomysql.domain.ports.out;

import java.util.List;

import com.conectiontomysql.domain.model.Producto;

public interface ProductoRepository extends CrudRepository<Producto, String> {
    List<Producto> findLowStock(int threshold);
}
