package com.upgrade.app.controller;

import com.upgrade.app.domain.DisenoGaleria;
import com.upgrade.app.domain.CategoriaGaleria;
import com.upgrade.app.domain.ElementoGaleria;
import com.upgrade.app.domain.FuenteMediaGaleria;
import com.upgrade.app.domain.TipoMediaGaleria;
import com.upgrade.app.dto.GaleriaForm;
import com.upgrade.app.exception.ArchivoGaleriaException;
import com.upgrade.app.service.GaleriaService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/admin/galeria")
@RequiredArgsConstructor
public class GaleriaController {

    private final GaleriaService galeriaService;

    @ModelAttribute("tiposGaleria")
    public TipoMediaGaleria[] tiposGaleria() {
        return TipoMediaGaleria.values();
    }

    @ModelAttribute("fuentesGaleria")
    public FuenteMediaGaleria[] fuentesGaleria() {
        return FuenteMediaGaleria.values();
    }

    @ModelAttribute("disenosGaleria")
    public DisenoGaleria[] disenosGaleria() {
        return DisenoGaleria.values();
    }

    @ModelAttribute("categoriasGaleria")
    public List<CategoriaGaleria> categoriasGaleria() {
        return galeriaService.listarCategorias();
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false, defaultValue = "todos") String estado,
            @RequestParam(required = false) Long categoria,
            @RequestParam(required = false, defaultValue = "orden") String ordenar,
            Model model
    ) {
        cargarListado(model, buscar, tipo, estado, categoria, ordenar);
        agregarFormulariosSiFaltan(model);
        return "admin/galeria";
    }

    @PostMapping
    public String crear(
            @Valid @ModelAttribute("nuevoElemento") GaleriaForm form,
            BindingResult bindingResult,
            @RequestParam(required = false) String buscar,
            @RequestParam(name = "tipoFiltro", required = false) String tipoFiltro,
            @RequestParam(name = "estadoFiltro", required = false, defaultValue = "todos") String estadoFiltro,
            @RequestParam(name = "categoriaFiltro", required = false) Long categoriaFiltro,
            @RequestParam(name = "ordenFiltro", required = false, defaultValue = "orden") String ordenFiltro,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        validarTituloUnico(form, bindingResult);
        validarCondicional(form, true, null, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("editarElemento", new GaleriaForm());
            model.addAttribute("modalAbierto", "nuevo");
            cargarListado(model, buscar, tipoFiltro, estadoFiltro, categoriaFiltro, ordenFiltro);
            return "admin/galeria";
        }

        ElementoGaleria creado;
        try {
            creado = galeriaService.crear(form);
        } catch (ArchivoGaleriaException exception) {
            bindingResult.reject("galeria.archivo", exception.getMessage());
            model.addAttribute("editarElemento", new GaleriaForm());
            model.addAttribute("modalAbierto", "nuevo");
            cargarListado(model, buscar, tipoFiltro, estadoFiltro, categoriaFiltro, ordenFiltro);
            return "admin/galeria";
        }
        redirectAttributes.addFlashAttribute("mensajeExito", "“" + creado.getTitulo() + "” fue agregado a la galería.");
        return "redirect:/admin/galeria";
    }

    @PostMapping("/editar")
    public String editar(
            @Valid @ModelAttribute("editarElemento") GaleriaForm form,
            BindingResult bindingResult,
            @RequestParam(required = false) String buscar,
            @RequestParam(name = "tipoFiltro", required = false) String tipoFiltro,
            @RequestParam(name = "estadoFiltro", required = false, defaultValue = "todos") String estadoFiltro,
            @RequestParam(name = "categoriaFiltro", required = false) Long categoriaFiltro,
            @RequestParam(name = "ordenFiltro", required = false, defaultValue = "orden") String ordenFiltro,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        ElementoGaleria actual = null;
        if (form.getId() == null) {
            bindingResult.reject("galeria.id.requerido", "No fue posible identificar el elemento.");
        } else {
            actual = galeriaService.obtenerPorId(form.getId());
            validarTituloUnico(form, bindingResult);
            validarCondicional(form, false, actual, bindingResult);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("nuevoElemento", new GaleriaForm());
            model.addAttribute("modalAbierto", "editar");
            cargarListado(model, buscar, tipoFiltro, estadoFiltro, categoriaFiltro, ordenFiltro);
            return "admin/galeria";
        }

        ElementoGaleria actualizado;
        try {
            actualizado = galeriaService.actualizar(form);
        } catch (ArchivoGaleriaException exception) {
            bindingResult.reject("galeria.archivo", exception.getMessage());
            model.addAttribute("nuevoElemento", new GaleriaForm());
            model.addAttribute("modalAbierto", "editar");
            cargarListado(model, buscar, tipoFiltro, estadoFiltro, categoriaFiltro, ordenFiltro);
            return "admin/galeria";
        }
        redirectAttributes.addFlashAttribute("mensajeExito", "“" + actualizado.getTitulo() + "” fue actualizado.");
        return redireccionListado(buscar, tipoFiltro, estadoFiltro, categoriaFiltro, ordenFiltro);
    }

    @PostMapping("/publicacion")
    public String cambiarPublicacion(@RequestParam Long id, Filtros filtros, RedirectAttributes redirectAttributes) {
        ElementoGaleria elemento = galeriaService.cambiarPublicacion(id);
        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                Boolean.TRUE.equals(elemento.getPublicado()) ? "El contenido fue publicado en la landing." : "El contenido pasó a borrador."
        );
        return redireccionListado(filtros);
    }

    @PostMapping("/destacado")
    public String cambiarDestacado(@RequestParam Long id, Filtros filtros, RedirectAttributes redirectAttributes) {
        ElementoGaleria elemento = galeriaService.cambiarDestacado(id);
        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                Boolean.TRUE.equals(elemento.getDestacado()) ? "El contenido ahora está destacado." : "Se quitó el destacado."
        );
        return redireccionListado(filtros);
    }

    @PostMapping("/subir")
    public String subir(@RequestParam Long id, Filtros filtros) {
        galeriaService.moverArriba(id);
        return redireccionListado(filtros);
    }

    @PostMapping("/bajar")
    public String bajar(@RequestParam Long id, Filtros filtros) {
        galeriaService.moverAbajo(id);
        return redireccionListado(filtros);
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Long id, Filtros filtros, RedirectAttributes redirectAttributes) {
        ElementoGaleria elemento = galeriaService.obtenerPorId(id);
        galeriaService.eliminar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "“" + elemento.getTitulo() + "” fue eliminado.");
        return redireccionListado(filtros);
    }

    @PostMapping("/categorias")
    public String crearCategoria(@RequestParam String nombre, RedirectAttributes redirectAttributes) {
        try {
            galeriaService.crearCategoria(nombre);
            redirectAttributes.addFlashAttribute("mensajeExito", "Categoría creada correctamente.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("mensajeError", exception.getMessage());
            redirectAttributes.addFlashAttribute("abrirCategorias", true);
        }
        return "redirect:/admin/galeria";
    }

    @PostMapping("/categorias/eliminar")
    public String eliminarCategoria(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            galeriaService.eliminarCategoria(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Categoría eliminada.");
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("mensajeError", exception.getMessage());
            redirectAttributes.addFlashAttribute("abrirCategorias", true);
        }
        return "redirect:/admin/galeria";
    }

    @GetMapping("/exportar")
    public void exportar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false, defaultValue = "todos") String estado,
            @RequestParam(required = false) Long categoria,
            @RequestParam(required = false, defaultValue = "orden") String ordenar,
            HttpServletResponse response
    ) throws IOException {
        List<ElementoGaleria> elementos = galeriaService.listar(
                buscar,
                convertirTipo(tipo),
                convertirEstado(estado),
                categoria,
                ordenar
        );
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=galeria-upgrade.csv");
        try (PrintWriter writer = response.getWriter()) {
            writer.write('\ufeff');
            writer.println("Orden,Tipo,Título,Categoría,Fecha,Estado,Destacado,Origen,Ruta");
            for (ElementoGaleria elemento : elementos) {
                writer.println(String.join(",",
                        csv(elemento.getOrden()),
                        csv(elemento.getTipo().getEtiqueta()),
                        csv(elemento.getTitulo()),
                        csv(elemento.getCategoria().getNombre()),
                        csv(elemento.getFechaEvento()),
                        csv(Boolean.TRUE.equals(elemento.getPublicado()) ? "Publicado" : "Borrador"),
                        csv(Boolean.TRUE.equals(elemento.getDestacado()) ? "Sí" : "No"),
                        csv(elemento.getFuente().getEtiqueta()),
                        csv(elemento.getRutaMedia())
                ));
            }
        }
    }

    private void cargarListado(Model model, String buscar, String tipo, String estado, Long categoria, String ordenar) {
        String tipoNormalizado = normalizarTipo(tipo);
        String estadoNormalizado = normalizarEstado(estado);
        String ordenNormalizado = normalizarOrden(ordenar);
        model.addAttribute("elementos", galeriaService.listar(
                buscar,
                convertirTipo(tipoNormalizado),
                convertirEstado(estadoNormalizado),
                categoria,
                ordenNormalizado
        ));
        model.addAttribute("estadisticas", galeriaService.obtenerEstadisticas());
        model.addAttribute("conteosCategoria", galeriaService.obtenerConteosPorCategoria());
        model.addAttribute("buscar", buscar == null ? "" : buscar.trim());
        model.addAttribute("tipoFiltro", tipoNormalizado);
        model.addAttribute("estadoFiltro", estadoNormalizado);
        model.addAttribute("categoriaFiltro", categoria);
        model.addAttribute("ordenFiltro", ordenNormalizado);
    }

    private void agregarFormulariosSiFaltan(Model model) {
        if (!model.containsAttribute("nuevoElemento")) {
            model.addAttribute("nuevoElemento", new GaleriaForm());
        }
        if (!model.containsAttribute("editarElemento")) {
            model.addAttribute("editarElemento", new GaleriaForm());
        }
    }

    private void validarTituloUnico(GaleriaForm form, BindingResult bindingResult) {
        if (form.getTitulo() != null
                && !bindingResult.hasFieldErrors("titulo")
                && galeriaService.tituloOcupado(form.getTitulo(), form.getId())) {
            bindingResult.rejectValue("titulo", "galeria.titulo.duplicado", "Ya existe contenido con ese título.");
        }
    }

    private void validarCondicional(
            GaleriaForm form,
            boolean creando,
            ElementoGaleria actual,
            BindingResult bindingResult
    ) {
        try {
            galeriaService.validarFormularioCondicional(form, creando, actual);
        } catch (ArchivoGaleriaException exception) {
            bindingResult.reject("galeria.archivo", exception.getMessage());
        }
    }

    private TipoMediaGaleria convertirTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return null;
        }
        try {
            return TipoMediaGaleria.valueOf(tipo);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private Boolean convertirEstado(String estado) {
        if ("publicados".equals(estado)) {
            return true;
        }
        if ("borradores".equals(estado)) {
            return false;
        }
        return null;
    }

    private String normalizarTipo(String tipo) {
        TipoMediaGaleria convertido = convertirTipo(tipo == null ? null : tipo.toUpperCase());
        return convertido == null ? "" : convertido.name();
    }

    private String normalizarEstado(String estado) {
        if ("publicados".equalsIgnoreCase(estado)) {
            return "publicados";
        }
        if ("borradores".equalsIgnoreCase(estado)) {
            return "borradores";
        }
        return "todos";
    }

    private String normalizarOrden(String ordenar) {
        if ("recientes".equalsIgnoreCase(ordenar)
                || "antiguos".equalsIgnoreCase(ordenar)
                || "titulo".equalsIgnoreCase(ordenar)) {
            return ordenar.toLowerCase();
        }
        return "orden";
    }

    private String redireccionListado(Filtros filtros) {
        return redireccionListado(
                filtros.getBuscar(), filtros.getTipoFiltro(), filtros.getEstadoFiltro(),
                filtros.getCategoriaFiltro(), filtros.getOrdenFiltro()
        );
    }

    private String redireccionListado(String buscar, String tipo, String estado, Long categoria, String ordenar) {
        StringBuilder redirect = new StringBuilder("redirect:/admin/galeria?estado=")
                .append(normalizarEstado(estado))
                .append("&ordenar=").append(normalizarOrden(ordenar));
        if (buscar != null && !buscar.isBlank()) {
            redirect.append("&buscar=").append(codificar(buscar));
        }
        String tipoNormalizado = normalizarTipo(tipo);
        if (!tipoNormalizado.isBlank()) {
            redirect.append("&tipo=").append(tipoNormalizado);
        }
        if (categoria != null) {
            redirect.append("&categoria=").append(categoria);
        }
        return redirect.toString();
    }

    private String codificar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }

    private String csv(Object valor) {
        String texto = valor == null ? "" : valor.toString();
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }

    public static class Filtros {
        private String buscar;
        private String tipoFiltro;
        private String estadoFiltro = "todos";
        private Long categoriaFiltro;
        private String ordenFiltro = "orden";

        public String getBuscar() { return buscar; }
        public void setBuscar(String buscar) { this.buscar = buscar; }
        public String getTipoFiltro() { return tipoFiltro; }
        public void setTipoFiltro(String tipoFiltro) { this.tipoFiltro = tipoFiltro; }
        public String getEstadoFiltro() { return estadoFiltro; }
        public void setEstadoFiltro(String estadoFiltro) { this.estadoFiltro = estadoFiltro; }
        public Long getCategoriaFiltro() { return categoriaFiltro; }
        public void setCategoriaFiltro(Long categoriaFiltro) { this.categoriaFiltro = categoriaFiltro; }
        public String getOrdenFiltro() { return ordenFiltro; }
        public void setOrdenFiltro(String ordenFiltro) { this.ordenFiltro = ordenFiltro; }
    }
}
