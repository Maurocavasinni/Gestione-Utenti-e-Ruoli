package it.unimol.newunimol.user_roles_management.dto;

public record UserCreationDto (
    String username,
    String email,
    String name,
    String surname,
    String password,
    String role
) {
}
