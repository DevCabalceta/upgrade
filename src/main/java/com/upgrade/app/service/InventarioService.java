package com.upgrade.app.service;

import com.upgrade.app.domain.Inventario;
import com.upgrade.app.repository.InventarioRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InventarioService {

    private final InventarioRepository inventarioRepository;

    public InventarioService(InventarioRepository inventarioRepository) {
        this.inventarioRepository = inventarioRepository;
    }

    public Inventario guardar(Inventario inventario) {
        return inventarioRepository.save(inventario);
    }
    public List<Inventario> listar() {
        return inventarioRepository.findAll();
    }
}