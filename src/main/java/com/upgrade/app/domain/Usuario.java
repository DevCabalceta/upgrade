package com.upgrade.app.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(length = 30)
    private String telefono;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cargo_id", nullable = false)
    private CargoColaborador cargo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "departamento_id", nullable = false)
    private DepartamentoColaborador departamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_colaborador", nullable = false, length = 30)
    private EstadoColaborador estadoColaborador = EstadoColaborador.DISPONIBLE;

    @Column(name = "eventos_asignados", nullable = false)
    private Integer eventosAsignados = 0;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_rol",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    @OrderBy("nombre ASC")
    private Set<Rol> roles = new LinkedHashSet<>();

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (fechaActualizacion == null) {
            fechaActualizacion = fechaCreacion;
        }
        if (fechaIngreso == null) {
            fechaIngreso = LocalDate.now();
        }
        if (estadoColaborador == null) {
            estadoColaborador = EstadoColaborador.DISPONIBLE;
        }
        if (eventosAsignados == null) {
            eventosAsignados = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public String getNombreCompleto() {
        return (nombre + " " + apellido).trim();
    }

    public String getIniciales() {
        String inicialNombre = nombre == null || nombre.isBlank() ? "" : nombre.substring(0, 1);
        String inicialApellido = apellido == null || apellido.isBlank() ? "" : apellido.substring(0, 1);
        return (inicialNombre + inicialApellido).toUpperCase();
    }

    public Rol getRolPrincipal() {
        return roles.stream().min(Comparator.comparing(Rol::getId)).orElse(null);
    }

    public Long getRolPrincipalId() {
        Rol rol = getRolPrincipal();
        return rol == null ? null : rol.getId();
    }

    public String getRolPrincipalNombre() {
        Rol rol = getRolPrincipal();
        return rol == null ? "Sin rol" : rol.getNombreMostrado();
    }

    public String getCargoNombre() {
        return cargo == null ? "Sin cargo" : cargo.getNombre();
    }

    public String getDepartamentoNombre() {
        return departamento == null ? "Sin departamento" : departamento.getNombre();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public CargoColaborador getCargo() { return cargo; }
    public void setCargo(CargoColaborador cargo) { this.cargo = cargo; }

    public DepartamentoColaborador getDepartamento() { return departamento; }
    public void setDepartamento(DepartamentoColaborador departamento) { this.departamento = departamento; }

    public EstadoColaborador getEstadoColaborador() { return estadoColaborador; }
    public void setEstadoColaborador(EstadoColaborador estadoColaborador) { this.estadoColaborador = estadoColaborador; }

    public Integer getEventosAsignados() { return eventosAsignados; }
    public void setEventosAsignados(Integer eventosAsignados) { this.eventosAsignados = eventosAsignados; }

    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public Set<Rol> getRoles() { return roles; }
    public void setRoles(Set<Rol> roles) { this.roles = roles == null ? new LinkedHashSet<>() : roles; }
}
