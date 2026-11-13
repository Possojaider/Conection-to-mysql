CREATE DATABASE IF NOT EXISTS jardineria
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE jardineria;

CREATE TABLE IF NOT EXISTS oficina (
  codigo_oficina VARCHAR(10) NOT NULL,
  ciudad VARCHAR(30) NOT NULL,
  pais VARCHAR(50) NOT NULL,
  region VARCHAR(50) DEFAULT NULL,
  codigo_postal VARCHAR(10) NOT NULL,
  telefono VARCHAR(20) NOT NULL,
  linea_direccion1 VARCHAR(50) NOT NULL,
  linea_direccion2 VARCHAR(50) DEFAULT NULL,
  PRIMARY KEY (codigo_oficina)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS empleado (
  codigo_empleado INT NOT NULL,
  nombre VARCHAR(50) NOT NULL,
  apellido1 VARCHAR(50) NOT NULL,
  apellido2 VARCHAR(50) DEFAULT NULL,
  extension VARCHAR(10) NOT NULL,
  email VARCHAR(100) NOT NULL,
  codigo_oficina VARCHAR(10) NOT NULL,
  codigo_jefe INT DEFAULT NULL,
  puesto VARCHAR(50) DEFAULT NULL,
  PRIMARY KEY (codigo_empleado),
  KEY idx_empleado_oficina (codigo_oficina),
  KEY idx_empleado_jefe (codigo_jefe),
  CONSTRAINT fk_empleado_oficina FOREIGN KEY (codigo_oficina)
    REFERENCES oficina (codigo_oficina),
  CONSTRAINT fk_empleado_jefe FOREIGN KEY (codigo_jefe)
    REFERENCES empleado (codigo_empleado)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cliente (
  codigo_cliente INT NOT NULL,
  nombre_cliente VARCHAR(50) NOT NULL,
  nombre_contacto VARCHAR(30) DEFAULT NULL,
  apellido_contacto VARCHAR(30) DEFAULT NULL,
  telefono VARCHAR(15) NOT NULL,
  fax VARCHAR(15) NOT NULL,
  linea_direccion1 VARCHAR(50) NOT NULL,
  linea_direccion2 VARCHAR(50) DEFAULT NULL,
  ciudad VARCHAR(50) NOT NULL,
  region VARCHAR(50) DEFAULT NULL,
  pais VARCHAR(50) DEFAULT NULL,
  codigo_postal VARCHAR(10) DEFAULT NULL,
  codigo_empleado_rep_ventas INT DEFAULT NULL,
  limite_credito DECIMAL(15,2) DEFAULT NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (codigo_cliente),
  KEY idx_cliente_representante (codigo_empleado_rep_ventas),
  CONSTRAINT fk_cliente_representante FOREIGN KEY (codigo_empleado_rep_ventas)
    REFERENCES empleado (codigo_empleado)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS gama_producto (
  gama VARCHAR(50) NOT NULL,
  descripcion_texto TEXT,
  descripcion_html TEXT,
  imagen VARCHAR(256) DEFAULT NULL,
  PRIMARY KEY (gama)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS producto (
  codigo_producto VARCHAR(15) NOT NULL,
  nombre VARCHAR(70) NOT NULL,
  gama VARCHAR(50) NOT NULL,
  dimensiones VARCHAR(25) DEFAULT NULL,
  proveedor VARCHAR(50) DEFAULT NULL,
  descripcion TEXT,
  cantidad_en_stock SMALLINT NOT NULL,
  precio_venta DECIMAL(15,2) NOT NULL,
  precio_proveedor DECIMAL(15,2) DEFAULT NULL,
  PRIMARY KEY (codigo_producto),
  KEY idx_producto_gama (gama),
  CONSTRAINT fk_producto_gama FOREIGN KEY (gama)
    REFERENCES gama_producto (gama)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pedido (
  codigo_pedido INT NOT NULL,
  fecha_pedido DATE NOT NULL,
  fecha_esperada DATE NOT NULL,
  fecha_entrega DATE DEFAULT NULL,
  estado VARCHAR(15) NOT NULL,
  comentarios TEXT,
  codigo_cliente INT NOT NULL,
  PRIMARY KEY (codigo_pedido),
  KEY idx_pedido_cliente (codigo_cliente),
  CONSTRAINT fk_pedido_cliente FOREIGN KEY (codigo_cliente)
    REFERENCES cliente (codigo_cliente)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS detalle_pedido (
  codigo_pedido INT NOT NULL,
  codigo_producto VARCHAR(15) NOT NULL,
  cantidad INT NOT NULL,
  precio_unidad DECIMAL(15,2) NOT NULL,
  numero_linea SMALLINT NOT NULL,
  PRIMARY KEY (codigo_pedido, codigo_producto),
  KEY idx_detalle_producto (codigo_producto),
  CONSTRAINT fk_detalle_pedido FOREIGN KEY (codigo_pedido)
    REFERENCES pedido (codigo_pedido),
  CONSTRAINT fk_detalle_producto FOREIGN KEY (codigo_producto)
    REFERENCES producto (codigo_producto)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pago (
  codigo_cliente INT NOT NULL,
  forma_pago VARCHAR(40) NOT NULL,
  id_transaccion VARCHAR(50) NOT NULL,
  fecha_pago DATE NOT NULL,
  total DECIMAL(15,2) NOT NULL,
  PRIMARY KEY (codigo_cliente, id_transaccion),
  CONSTRAINT fk_pago_cliente FOREIGN KEY (codigo_cliente)
    REFERENCES cliente (codigo_cliente)
) ENGINE=InnoDB;
