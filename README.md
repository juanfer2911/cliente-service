#  Cliente Service — Microservicio Spring Boot 4.x

Microservicio backend desarrollado en Java 21 y Spring Boot 4.1.1 para la gestión de clientes y regiones. Este repositorio consolida el desarrollo completo realizado a lo largo de 5 Codelabs, cubriendo desde el diseño arquitectónico hasta la suite integral de pruebas con bases de datos en contenedores.

---

##  Módulos y Codelabs Desarrollados

### 🔹 Codelab 01 — Arquitectura Hexagonal / Multicapa & Entidades
* Configuración del entorno de desarrollo con Java 21 y Spring Boot 4.1.1.
* Definición del modelo de dominio: entidades `Cliente` y `Region` con mapeo ORM JPA (`jakarta.persistence`).
* Configuración de la base de datos de persistencia PostgreSQL.

### 🔹 Codelab 02 — Repositorio y Servicios de Dominio
* Implementación del patrón DAO / Repository mediante `IClienteDao` (`JpaRepository`).
* Consultas JPQL personalizadas (`@Query("from Region")`).
* Lógica de negocio encapsulada en `ClienteServiceImpl` con manejo de excepciones de dominio.

### 🔹 Codelab 03 — API RESTful & Manejo Global de Errores
* Expansión de controladores REST con `ClienteRestController` (`/api/v1/cliente-service/clientes`).
* Soporte para operaciones CRUD, paginación (`Pageable`) y endpoints de regiones.
* Manejo centralizado de excepciones con `@ControllerAdvice` (`GlobalExceptionHandler`).

### 🔹 Codelab 04 — Unit, Web Slice & Data Slice Testing
* **Unit Tests:** `ClienteServiceImplTest` aislando lógica de servicio con Mockito (`when`, `verify`).
* **Web Slice Tests:** `ClienteRestControllerTest` verificando controladores con `@WebMvcTest`, `MockMvc` y `@MockitoBean`.
* **Data Slice Tests:** `IClienteDaoTest` probando persistencia y consultas JPA sobre H2 embebido con `@DataJpaTest` y `TestEntityManager`.

### 🔹 Codelab 05 — E2E Tests con Testcontainers & PostgreSQL
* **Pruebas de Extremo a Extremo:** `ClienteIntegrationTest` probando el flujo HTTP completo hasta la base de datos.
* **Testcontainers & `@ServiceConnection`:** Despliegue automatizado de un contenedor **PostgreSQL 16** real durante las pruebas.
* **Paginación & Deserialización:** Verificación del endpoint paginado `/clientes/page/0` deserializando la estructura JSON a un mapa de datos.
* **Cobertura con JaCoCo:** Análisis de cobertura de código ejecutable e instrucción por capa.

---

## Tecnologías y Herramientas

* **Lenguaje & Framework:** Java 21 | Spring Boot 4.1.1
* **Persistencia:** Spring Data JPA | Hibernate 7.x | PostgreSQL 16 | H2 Database
* **Testing:** JUnit 5 | AssertJ | Mockito | Testcontainers | MockMvc
* **Métricas:** JaCoCo 0.8.x
* **Build Tool:** Apache Maven

---

## Ejecución de Pruebas

### Requisitos
* Docker Desktop en ejecución (necesario para los tests E2E con Testcontainers).

### Ejecutar Suite Completa
Para correr todas las pruebas automatizadas (Unit, Slice y E2E):

```bash
./mvnw test
