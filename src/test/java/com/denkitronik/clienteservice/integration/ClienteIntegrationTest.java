package com.denkitronik.clienteservice.integration;

import com.denkitronik.clienteservice.domain.entities.Cliente;
import com.denkitronik.clienteservice.domain.entities.Region;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// Imports ajustados para Spring Boot 4.x
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate // Requerido en Spring Boot 4.x para registrar TestRestTemplate
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("E2E tests — flujo completo HTTP → PostgreSQL con Testcontainers")
class ClienteIntegrationTest {

    // Testcontainers levanta un PostgreSQL real antes de ejecutar los tests
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate; // Para insertar datos iniciales en la BD

    private static final String BASE = "/api/v1/cliente-service";
    private static final AtomicBoolean seeded = new AtomicBoolean(false);
    private static Long regionId; // Compartido entre tests
    private static Long idCreado; // Compartido entre tests

    @BeforeEach
    void seedDatosIniciales() {
        // Se ejecuta antes de cada test, pero solo inserta en la BD la primera vez
        if (seeded.compareAndSet(false, true)) {
            jdbcTemplate.execute(
                    "INSERT INTO regiones(nombre) VALUES ('América del Sur')");
            regionId = jdbcTemplate.queryForObject(
                    "SELECT id FROM regiones WHERE nombre = 'América del Sur'",
                    Long.class);
        }
    }

    @Test
    @Order(1)
    @DisplayName("POST /clientes → 201 crea el cliente y retorna el ID")
    void crearCliente_debeRetornar201() {
        Region region = new Region();
        region.setId(regionId);

        Cliente nuevo = new Cliente();
        nuevo.setNombre("Margaret");
        nuevo.setApellido("Hamilton");
        nuevo.setEmail("margaret@apollo.nasa");
        nuevo.setRegion(region);

        ResponseEntity<Cliente> respuesta =
                restTemplate.postForEntity(BASE + "/clientes", nuevo, Cliente.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().getId()).isNotNull();
        assertThat(respuesta.getBody().getNombre()).isEqualTo("Margaret");

        idCreado = respuesta.getBody().getId(); // Guardamos el ID generado para las pruebas siguientes
    }

    @Test
    @Order(2)
    @DisplayName("GET /clientes/{id} → 200 encuentra el cliente creado")
    void buscarClienteCreado_debeRetornar200() {
        ResponseEntity<Cliente> respuesta =
                restTemplate.getForEntity(BASE + "/clientes/" + idCreado, Cliente.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().getEmail()).isEqualTo("margaret@apollo.nasa");
    }

    @Test
    @Order(3)
    @DisplayName("GET /clientes/page/0 → 200 devuelve página con content como lista")
    @SuppressWarnings("unchecked")
    void listarClientesPaginado_debeRetornarPaginaConLista() {
        // Act: Hacemos la petición al endpoint de paginación mapeando la respuesta a Map
        ResponseEntity<java.util.Map> respuesta =
                restTemplate.getForEntity(BASE + "/clientes/page/0", java.util.Map.class);

        // Assert: Verificamos el código de estado HTTP 200 OK
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();

        java.util.Map<String, Object> body = respuesta.getBody();

        // Verificamos que el mapa contenga la clave "content"
        assertThat(body).containsKey("content");

        // Verificamos que el campo "content" sea efectivamente una Lista
        assertThat(body.get("content")).isInstanceOf(java.util.List.class);

        // Opcional: Validamos que contenga al menos el cliente creado en Order(1)
        java.util.List<?> listaClientes = (java.util.List<?>) body.get("content");
        assertThat(listaClientes).isNotEmpty();

        // Verificamos la presencia de metadatos de la paginación de Spring Data
        assertThat(body).containsKeys("totalElements", "totalPages");
    }

    @Test
    @Order(5)
    @DisplayName("PUT /clientes/{id} → 201 actualiza los datos")
    void actualizarCliente_debeRetornar201() {
        Region region = new Region();
        region.setId(regionId);

        Cliente actualizado = new Cliente();
        actualizado.setNombre("Margaret");
        actualizado.setApellido("Hamilton-Updated");
        actualizado.setEmail("margaret@apollo.nasa");
        actualizado.setRegion(region);

        HttpEntity<Cliente> entity = new HttpEntity<>(actualizado, headersJson());

        ResponseEntity<Cliente> respuesta =
                restTemplate.exchange(BASE + "/clientes/" + idCreado,
                        HttpMethod.PUT, entity, Cliente.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().getApellido()).isEqualTo("Hamilton-Updated");
    }

    @Test
    @Order(6)
    @DisplayName("GET /clientes/9999 → 404 ID inexistente")
    void idInexistente_debeRetornar404() {
        ResponseEntity<String> respuesta =
                restTemplate.getForEntity(BASE + "/clientes/9999", String.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(7)
    @DisplayName("DELETE /clientes/{id} → 204 y luego GET confirma 404")
    void eliminarCliente_debeRetornar204YLuego404() {
        restTemplate.delete(BASE + "/clientes/" + idCreado);

        ResponseEntity<String> respuesta =
                restTemplate.getForEntity(BASE + "/clientes/" + idCreado, String.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private HttpHeaders headersJson() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }
}