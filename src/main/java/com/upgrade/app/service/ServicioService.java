package com.upgrade.app.service;

import com.upgrade.app.domain.Servicio;
import com.upgrade.app.exception.ServicioNoEncontradoException;
import com.upgrade.app.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServicioService {

    private final ServicioRepository servicioRepository;

    public Page<Servicio> listar(int pagina, int tamano) {
        int paginaSegura = Math.max(pagina, 0);
        int tamanoSeguro = Math.min(Math.max(tamano, 5), 50);

        PageRequest pageable = PageRequest.of(
                paginaSegura,
                tamanoSeguro,
                Sort.by(Sort.Direction.ASC, "nombre")
        );
        return servicioRepository.buscar(null, null, pageable);
    }

    public Servicio obtenerPorId(Long id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new ServicioNoEncontradoException(id));
    }
}