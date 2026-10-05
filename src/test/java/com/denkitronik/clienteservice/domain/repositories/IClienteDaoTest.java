package com.denkitronik.clienteservice.domain.repositories;

import com.denkitronik.clienteservice.domain.entities.Cliente;
import com.denkitronik.clienteservice.domain.entities.Region;

// Imports adaptados para Spring Boot 4.x
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Integration tests (data slice) — IClienteDao con H2")
class IClienteDaoTest {

    @Autowired
    private TestEntityManager em; // Para preparar datos de prueba en aislamiento

    @Autowired
    private IClienteDao clienteDao; // El repositorio real bajo prueba

    private Region region;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        // Preparamos datos directamente con TestEntityManager
        region = new Region();
        region.setNombre("Asia");
        em.persist(region);

        cliente = new Cliente();
        cliente.setNombre("Grace");
        cliente.setApellido("Hopper");
        cliente.setEmail("grace@navy.mil");
        cliente.setRegion(region);
        em.persist(cliente);

        em.flush(); // Sincroniza y fuerza los INSERT en H2
    }

    @Test
    @DisplayName("findAllRegiones — devuelve las regiones persistidas")
    void findAllRegiones_debeRetornarListaDeRegiones() {
        List<Region> regiones = clienteDao.findAllRegiones();

        assertThat(regiones).hasSize(1);
        assertThat(regiones.get(0).getNombre()).isEqualTo("Asia");
    }

    @Test
    @DisplayName("findByEmail — email existente → devuelve cliente")
    void findByEmail_emailExistente_debeRetornarCliente() {
        Optional<Cliente> resultado = clienteDao.findByEmail("grace@navy.mil");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Grace");
    }

    @Test
    @DisplayName("findByEmail — email inexistente → devuelve vacío")
    void findByEmail_emailInexistente_debeRetornarVacio() {
        Optional<Cliente> resultado = clienteDao.findByEmail("noexiste@navy.mil");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("save — persiste un nuevo cliente")
    void save_debePersistirCliente() {
        Cliente nuevo = new Cliente();
        nuevo.setNombre("Margaret");
        nuevo.setApellido("Hamilton");
        nuevo.setEmail("margaret@nasa.gov");
        nuevo.setRegion(region);

        Cliente guardado = clienteDao.save(nuevo);

        assertThat(guardado.getId()).isNotNull();
        assertThat(em.find(Cliente.class, guardado.getId())).isNotNull();
    }

    @Test
    @DisplayName("deleteById — elimina el cliente verificado con em.find")
    void deleteById_debeEliminarElCliente() {
        Long id = cliente.getId();

        clienteDao.deleteById(id);
        em.flush();

        // Verificamos directamente en el contexto de persistencia de JPA
        assertThat(em.find(Cliente.class, id)).isNull();
    }
}