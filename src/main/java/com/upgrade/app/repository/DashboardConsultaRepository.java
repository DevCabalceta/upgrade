package com.upgrade.app.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class DashboardConsultaRepository {

    private final EntityManager entityManager;

    /**
     * Agrupa las altas reales de clientes, préstamos y mantenimientos.
     * Se usa una sola consulta para evitar ejecutar tres consultas por cada mes.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Long> contarActividadMensual(LocalDateTime desde) {
        String sql = """
                SELECT DATE_FORMAT(actividad.fecha, '%Y-%m') AS periodo, COUNT(*) AS total
                FROM (
                    SELECT fecha_registro AS fecha FROM cliente WHERE activo = TRUE
                    UNION ALL
                    SELECT fecha_creacion AS fecha FROM prestamo
                    UNION ALL
                    SELECT fecha_creacion AS fecha FROM orden_mantenimiento WHERE activo = TRUE
                ) actividad
                WHERE actividad.fecha >= :desde
                GROUP BY DATE_FORMAT(actividad.fecha, '%Y-%m')
                ORDER BY periodo
                """;
        List<Object[]> filas = entityManager.createNativeQuery(sql)
                .setParameter("desde", Timestamp.valueOf(desde))
                .getResultList();
        Map<String, Long> resultado = new LinkedHashMap<>();
        for (Object[] fila : filas) {
            resultado.put(String.valueOf(fila[0]), ((Number) fila[1]).longValue());
        }
        return resultado;
    }
}
