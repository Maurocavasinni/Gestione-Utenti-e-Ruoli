package it.unimol.newunimol.user_roles_management.dto;

public record UserProfileDto(
        String id,
        String username,
        String email,
        String nome,
        String cognome,
        String nomeRuolo,
        Long dataCreazione,
        Long ultimoLogin
) {
}
