package it.unimol.newunimol.user_roles_management.dto;

public record ChangePasswordRequestDto(
        String oldPassword,
        String newPassword
) {
}
