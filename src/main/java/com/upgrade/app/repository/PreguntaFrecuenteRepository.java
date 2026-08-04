package com.upgrade.app.repository;

import com.upgrade.app.domain.CategoriaPregunta;
import com.upgrade.app.domain.PreguntaFrecuente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PreguntaFrecuenteRepository extends JpaRepository<PreguntaFrecuente, Long> {

    @Query("""
            SELECT p
            FROM PreguntaFrecuente p
            WHERE (
                :buscar IS NULL
                OR LOWER(p.pregunta) LIKE LOWER(CONCAT('%', :buscar, '%'))
                OR LOWER(p.respuesta) LIKE LOWER(CONCAT('%', :buscar, '%'))
            )
            AND (:activa IS NULL OR p.activa = :activa)
            AND (:categoria IS NULL OR p.categoria = :categoria)
            ORDER BY p.orden ASC, p.id ASC
            """)
    List<PreguntaFrecuente> buscar(
            @Param("buscar") String buscar,
            @Param("activa") Boolean activa,
            @Param("categoria") CategoriaPregunta categoria
    );

    List<PreguntaFrecuente> findAllByOrderByOrdenAscIdAsc();

    List<PreguntaFrecuente> findAllByActivaTrueOrderByOrdenAscIdAsc();

    Optional<PreguntaFrecuente> findFirstByOrdenLessThanOrderByOrdenDesc(Integer orden);

    Optional<PreguntaFrecuente> findFirstByOrdenGreaterThanOrderByOrdenAsc(Integer orden);

    boolean existsByPreguntaIgnoreCase(String pregunta);

    boolean existsByPreguntaIgnoreCaseAndIdNot(String pregunta, Long id);

    long countByActivaTrue();

    long countByActivaFalse();

    @Query("SELECT COUNT(DISTINCT p.categoria) FROM PreguntaFrecuente p")
    long contarCategoriasUtilizadas();

    @Query("SELECT COALESCE(MAX(p.orden), 0) FROM PreguntaFrecuente p")
    int obtenerOrdenMaximo();
}
