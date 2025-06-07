package it.unimol.newunimol.user_roles_management.dto;

public record RoleDto (
        String id,
        String nome,
        String descrizione
) {
    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(this.nome);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(this.nome);
    }

    public boolean isTeacher() {
        return "DOCENTE".equals(this.nome);
    }

    public boolean isStudent() {
        return "STUDENTE".equals(this.nome);
    }
}
