package com.upgrade.app.service;

import com.upgrade.app.domain.Inventario;
import com.upgrade.app.domain.Prestamo;
import com.upgrade.app.repository.PrestamoRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final InventarioService inventarioService;

    public PrestamoService(
            PrestamoRepository prestamoRepository,
            InventarioService inventarioService) {

        this.prestamoRepository = prestamoRepository;
        this.inventarioService = inventarioService;
    }

    @Transactional
    public Prestamo guardar(Prestamo prestamo) {

        Inventario inventario = inventarioService.buscarPorId(
                prestamo.getInventario().getId()
        );

        if (inventario == null) {
            throw new IllegalArgumentException("El equipo seleccionado no existe.");
        }

        if (prestamo.getCantidad() == null || prestamo.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        if (prestamo.getCantidad() > inventario.getCantidadDisponible()) {
            throw new IllegalArgumentException(
                    "No hay suficientes unidades disponibles."
            );
        }

        inventario.setCantidadDisponible(
                inventario.getCantidadDisponible() - prestamo.getCantidad()
        );

        inventarioService.guardar(inventario);

        prestamo.setInventario(inventario);
        prestamo.setEstado("Prestado");

        return prestamoRepository.save(prestamo);
    }

    public List<Prestamo> listar() {
        return prestamoRepository.findAll();
    }

    public Prestamo buscarPorId(Long id) {
        return prestamoRepository.findById(id).orElse(null);
    }

    @Transactional
    public void devolver(Long id) {

        Prestamo prestamo = buscarPorId(id);

        if (prestamo == null) {
            return;
        }

        if ("Devuelto".equalsIgnoreCase(prestamo.getEstado())) {
            return;
        }

        Inventario inventario = prestamo.getInventario();

        inventario.setCantidadDisponible(
                inventario.getCantidadDisponible() + prestamo.getCantidad()
        );

        inventarioService.guardar(inventario);

        prestamo.setEstado("Devuelto");

        prestamoRepository.save(prestamo);
    }

    public void eliminar(Long id) {
        prestamoRepository.deleteById(id);
    }
}