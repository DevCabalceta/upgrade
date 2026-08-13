package com.upgrade.app.dto;

import jakarta.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import java.util.Set;

public class AccesosRolForm {
    @NotNull private Long rolId;
    private Set<Long> moduloIds = new LinkedHashSet<>();
    public Long getRolId() { return rolId; } public void setRolId(Long v) { rolId = v; }
    public Set<Long> getModuloIds() { return moduloIds; }
    public void setModuloIds(Set<Long> v) { moduloIds = v == null ? new LinkedHashSet<>() : v; }
}
