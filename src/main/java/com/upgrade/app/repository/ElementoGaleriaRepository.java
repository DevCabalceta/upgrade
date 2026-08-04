package com.upgrade.app.repository;

import com.upgrade.app.domain.ElementoGaleria;
import com.upgrade.app.domain.TipoMediaGaleria;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ElementoGaleriaRepository extends JpaRepository<ElementoGaleria, Long> {

    @Query("""
            SELECT e FROM ElementoGaleria e
            WHERE (:buscar IS NULL
                   OR LOWER(e.titulo) LIKE LOWER(CONCAT('%', :buscar, '%'))
                   OR LOWER(COALESCE(e.descripcion, '')) LIKE LOWER(CONCAT('%', :buscar, '%'))
                   OR LOWER(e.textoAlternativo) LIKE LOWER(CONCAT('%', :buscar, '%')))
              AND (:tipo IS NULL OR e.tipo = :tipo)
              AND (:publicado IS NULL OR e.publicado = :publicado)
              AND (:categoriaId IS NULL OR e.categoria.id = :categoriaId)
            """)
    List<ElementoGaleria> buscar(
            @Param("buscar") String buscar,
            @Param("tipo") TipoMediaGaleria tipo,
            @Param("publicado") Boolean publicado,
            @Param("categoriaId") Long categoriaId,
            Sort sort
    );

    List<ElementoGaleria> findAllByPublicadoTrueOrderByOrdenAscIdAsc();

    Optional<ElementoGaleria> findFirstByOrdenLessThanOrderByOrdenDescIdDesc(Integer orden);
    Optional<ElementoGaleria> findFirstByOrdenGreaterThanOrderByOrdenAscIdAsc(Integer orden);

    boolean existsByTituloIgnoreCase(String titulo);
    boolean existsByTituloIgnoreCaseAndIdNot(String titulo, Long id);

    long countByPublicadoTrue();
    long countByPublicadoFalse();
    long countByTipo(TipoMediaGaleria tipo);
    long countByCategoriaId(Long categoriaId);

    @Query("SELECT COALESCE(MAX(e.orden), 0) FROM ElementoGaleria e")
    Integer obtenerOrdenMaximo();

    @Modifying
    @Query("UPDATE ElementoGaleria e SET e.orden = e.orden - 1 WHERE e.orden > :orden")
    void cerrarHuecoDeOrden(@Param("orden") Integer orden);
}
