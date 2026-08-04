package com.upgrade.app.service;

import com.upgrade.app.domain.Cliente;
import com.upgrade.app.dto.ClienteForm;
import com.upgrade.app.exception.ClienteNoEncontradoException;
import com.upgrade.app.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public Page<Cliente> listar(String buscar, Boolean activo, int pagina, int tamano) {
        String termino = normalizarTextoOpcional(buscar);
        int paginaSegura = Math.max(pagina, 0);
        int tamanoSeguro = Math.min(Math.max(tamano, 5), 50);

        PageRequest pageable = PageRequest.of(
                paginaSegura,
                tamanoSeguro,
                Sort.by(Sort.Direction.DESC, "fechaRegistro")
        );
        return clienteRepository.buscar(termino, activo, pageable);
    }

    public List<Cliente> listarParaExportar(String buscar, Boolean activo) {
        return clienteRepository.buscar(
                normalizarTextoOpcional(buscar),
                activo,
                PageRequest.of(0, 10_000, Sort.by(Sort.Direction.ASC, "nombre"))
        ).getContent();
    }

    public Cliente obtenerPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException(id));
    }

    public boolean correoOcupado(String correo, Long idActual) {
        String normalizado = normalizarCorreo(correo);
        return idActual == null
                ? clienteRepository.existsByCorreoIgnoreCase(normalizado)
                : clienteRepository.existsByCorreoIgnoreCaseAndIdNot(normalizado, idActual);
    }

    public boolean identificacionOcupada(String identificacion, Long idActual) {
        String normalizada = limpiar(identificacion);
        return idActual == null
                ? clienteRepository.existsByIdentificacionIgnoreCase(normalizada)
                : clienteRepository.existsByIdentificacionIgnoreCaseAndIdNot(normalizada, idActual);
    }

    @Transactional
    public Cliente crear(ClienteForm form) {
        Cliente cliente = new Cliente();
        copiarFormulario(form, cliente);
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente actualizar(ClienteForm form) {
        Cliente cliente = obtenerPorId(form.getId());
        copiarFormulario(form, cliente);
        return clienteRepository.save(cliente);
    }

    @Transactional
    public void eliminarLogicamente(Long id) {
        Cliente cliente = obtenerPorId(id);
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    private void copiarFormulario(ClienteForm form, Cliente cliente) {
        cliente.setTipoCliente(form.getTipoCliente());
        cliente.setIdentificacion(limpiar(form.getIdentificacion()));
        cliente.setNombre(limpiar(form.getNombre()));
        cliente.setEmpresa(limpiarOpcional(form.getEmpresa()));
        cliente.setCorreo(normalizarCorreo(form.getCorreo()));
        cliente.setTelefono(limpiar(form.getTelefono()));
        cliente.setDireccion(limpiarOpcional(form.getDireccion()));
        cliente.setSegmento(limpiarOpcional(form.getSegmento()));
        cliente.setCondicionesPago(limpiarOpcional(form.getCondicionesPago()));
        cliente.setNotas(limpiarOpcional(form.getNotas()));
        cliente.setActivo(Boolean.TRUE.equals(form.getActivo()));
    }

    private String normalizarCorreo(String correo) {
        return limpiar(correo).toLowerCase(Locale.ROOT);
    }

    private String normalizarTextoOpcional(String valor) {
        String limpio = limpiarOpcional(valor);
        return limpio == null ? null : limpio;
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String limpiarOpcional(String valor) {
        String limpio = limpiar(valor);
        return limpio.isEmpty() ? null : limpio;
    }
}