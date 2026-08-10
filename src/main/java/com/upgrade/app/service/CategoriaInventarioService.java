package com.upgrade.app.service;

import com.upgrade.app.domain.CategoriaInventario;
import com.upgrade.app.repository.CategoriaInventarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaInventarioService {

    private final CategoriaInventarioRepository categoriaRepository;

    public CategoriaInventarioService(
            CategoriaInventarioRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<CategoriaInventario> listar() {
        return categoriaRepository.findAll();
    }
}