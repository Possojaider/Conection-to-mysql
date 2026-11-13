package com.conectiontomysql.domain.ports.out;

import java.util.List;

import com.conectiontomysql.domain.model.Empleado;

public interface EmpleadoRepository extends CrudRepository<Empleado, Integer> {
    List<Empleado> findByOffice(String officeCode);
}
