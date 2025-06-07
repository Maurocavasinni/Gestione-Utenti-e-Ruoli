package it.unimol.newunimol.user_roles_management.dto;

public record UserDto (
        String id,
        String username,
        String email,
        String name,
        String surname,
        String password,
        Long creationDate,
        Long lastLogin,
        RoleDto ruolo
) {
    public String getRoleName() {
        return this.ruolo.nome();
    }
}
