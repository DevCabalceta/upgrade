package com.upgrade.app.repository;

import com.upgrade.app.domain.Usuario;
import com.upgrade.app.domain.EstadoColaborador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Busca coincidencias tanto en username como en email
    @EntityGraph(attributePaths = {"roles", "roles.modulos", "cargo", "departamento"})
    Optional<Usuario> findByUsernameOrEmail(String username, String email);

    long countDistinctByActivoTrueAndRolesId(Long rolId);

    @Query("select distinct u.username from Usuario u join u.roles r where u.activo = true and r.id = :rolId")
    List<String> findUsernamesActivosByRolId(@Param("rolId") Long rolId);

    List<Usuario> findAllByActivoTrueOrderByNombreAscApellidoAsc();

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    @EntityGraph(attributePaths = {"roles", "cargo", "departamento"})
    @Query(value = """
            select distinct u
            from Usuario u
            left join u.roles r
            where u.activo = true
              and (:buscar is null
                   or lower(u.nombre) like lower(concat('%', :buscar, '%'))
                   or lower(u.apellido) like lower(concat('%', :buscar, '%'))
                   or lower(u.username) like lower(concat('%', :buscar, '%'))
                   or lower(u.email) like lower(concat('%', :buscar, '%'))
                   or lower(u.cargo.nombre) like lower(concat('%', :buscar, '%'))
                   or lower(u.departamento.nombre) like lower(concat('%', :buscar, '%')))
              and (:estado is null or u.estadoColaborador = :estado)
              and (:rolId is null or r.id = :rolId)
            """,
            countQuery = """
            select count(distinct u.id)
            from Usuario u
            left join u.roles r
            where u.activo = true
              and (:buscar is null
                   or lower(u.nombre) like lower(concat('%', :buscar, '%'))
                   or lower(u.apellido) like lower(concat('%', :buscar, '%'))
                   or lower(u.username) like lower(concat('%', :buscar, '%'))
                   or lower(u.email) like lower(concat('%', :buscar, '%'))
                   or lower(u.cargo.nombre) like lower(concat('%', :buscar, '%'))
                   or lower(u.departamento.nombre) like lower(concat('%', :buscar, '%')))
              and (:estado is null or u.estadoColaborador = :estado)
              and (:rolId is null or r.id = :rolId)
            """)
    Page<Usuario> buscarColaboradores(
            @Param("buscar") String buscar,
            @Param("estado") EstadoColaborador estado,
            @Param("rolId") Long rolId,
            Pageable pageable
    );
}
