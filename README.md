# Sistema de gestión de Jardinería

Aplicación de escritorio hecha con Java 21, Swing y MySQL 8.4. Permite consultar
y administrar clientes, pedidos, productos, gamas, pagos y las demás tablas del
sistema.

## Requisitos

- JDK 21.
- Apache Maven 3.9 o posterior.
- Docker Desktop con Docker Compose.

Comprueba que las herramientas estén disponibles:

```sh
java -version
mvn -version
docker compose version
```

## Preparar el proyecto

Ejecuta estos comandos desde la carpeta raíz del repositorio:

```sh
cp .env.example .env
cp db.properties.example src/main/resources/db.properties
```

Edita los archivos creados y reemplaza los valores de ejemplo:

- `.env` configura el usuario y las contraseñas que Docker Compose utilizará
  al inicializar MySQL.
- `src/main/resources/db.properties` configura las credenciales que usará la
  aplicación Java. Su `db.user` y `db.password` deben coincidir con `DB_USER`
  y `DB_PASSWORD` de `.env`.

No guardes ni compartas `.env` o `src/main/resources/db.properties`: contienen
credenciales locales y están excluidos de Git.

La aplicación también acepta `DB_URL`, `DB_USER` y `DB_PASSWORD` como variables
de entorno. Estas tienen prioridad sobre `db.properties`. Docker Compose carga
`.env` automáticamente para configurar el contenedor; la aplicación Java no
carga ese archivo automáticamente, por eso se prepara `db.properties`.

## Iniciar MySQL

Desde la raíz del proyecto:

```sh
docker compose up -d
docker compose ps
```

En `docker compose ps`, espera a que el servicio `mysql` aparezca como `healthy`.
La base se llama `jardineria`; el puerto del host es `3307` y el contenedor
escucha en `3306`. La aplicación se conecta a:

```text
jdbc:mysql://localhost:3307/jardineria
```

El esquema y las ocho tablas se crean mediante
[`db/init/01-schema.sql`](db/init/01-schema.sql) únicamente cuando MySQL inicializa
un volumen de datos nuevo. El script crea tablas, pero no carga datos de ejemplo.
Por eso, las tablas pueden aparecer vacías hasta que agregues registros.

Comandos útiles para administrar MySQL:

```sh
docker compose logs -f mysql
docker compose stop
docker compose start
docker compose down
```

`docker compose down` elimina el contenedor, pero conserva el volumen con los
datos. **No ejecutes `docker compose down -v` salvo que quieras borrar
permanentemente la base local.**

## Compilar, probar y ejecutar

Compila el proyecto y ejecuta las pruebas:

```sh
mvn clean verify
```

Inicia la aplicación:

```sh
mvn exec:java
```

La clase de entrada es `com.conectiontomysql.Main`. Para ejecutar solo las
pruebas:

```sh
mvn test
```

## Funciones

- **Dashboard:** indicadores, pedidos recientes y alertas de stock; se actualiza
  al abrir, manualmente o cada minuto.
- **Módulos de gestión:** listar, buscar, crear, editar y eliminar registros de
  las ocho tablas. Las operaciones respetan las claves foráneas.
- **Productos:** filtro opcional por stock máximo. El dashboard muestra los
  productos con stock crítico (cinco unidades o menos).
- **Gamas de producto:** elegir una imagen local o proporcionar una URL HTTP/HTTPS.
  Los archivos locales se copian a `imagenes/gamas/`; la vista previa aparece al
  seleccionar una gama. Ejecuta la aplicación desde la raíz del proyecto para
  que las rutas locales se resuelvan correctamente. Esta carpeta no se incluye
  en Git.
- **Pedidos:** el formulario de creación valida el cliente y las existencias,
  guarda el pedido y sus detalles, y descuenta el inventario dentro de una sola
  transacción. Si una operación falla, se revierte la transacción.

## Esquema y migraciones

El esquema inicial está en [`db/init/01-schema.sql`](db/init/01-schema.sql).
Los archivos de [`db/migrations/`](db/migrations/) son cambios para bases que ya
están creadas; Docker Compose no los aplica automáticamente.

Si una base existente no tiene la columna `cliente.activo`, aplica la migración
desde la raíz del proyecto:

```sh
docker compose exec -T mysql sh -c \
  'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" jardineria' \
  < db/migrations/V1__add_customer_active_status.sql
```

La migración agrega la columna con valor `1` por defecto y no elimina los
clientes existentes. Haz una copia de seguridad antes de modificar una base con
datos importantes.

## Estructura

```text
src/main/java/com/conectiontomysql/
├── application/       Casos de uso, modelos auxiliares y contratos
├── domain/            Entidades del negocio y contratos de repositorio
├── infrastructure/    Adaptadores JDBC para MySQL
└── *.java             Ventanas Swing, configuración y punto de entrada

src/main/resources/    Configuración de la aplicación
src/test/java/         Pruebas automatizadas
db/init/               Esquema inicial de MySQL
db/migrations/         Cambios para bases ya existentes
```

La interfaz Swing llama a los casos de uso de `application`; estos coordinan el
dominio y los adaptadores JDBC de `infrastructure`. Así, las ventanas no
construyen ni ejecutan SQL directamente.

Las pruebas que requieren una base de datos deben ejecutarse contra una
instancia de prueba, no contra una base con datos importantes.
