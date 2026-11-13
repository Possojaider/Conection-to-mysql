package com.conectiontomysql.domain.ports.out;

import java.util.List;
import java.util.Optional;

import com.conectiontomysql.domain.model.Cliente;

public interface ClienteRepository extends CrudRepository<Cliente, Integer> {
}
