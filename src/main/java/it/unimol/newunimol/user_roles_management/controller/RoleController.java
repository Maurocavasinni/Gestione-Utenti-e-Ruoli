package it.unimol.newunimol.user_roles_management.controller;

import it.unimol.newunimol.user_roles_management.dto.RoleDto;
import it.unimol.newunimol.user_roles_management.service.RoleService;
import it.unimol.newunimol.user_roles_management.util.RoleLevelEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Roles", description = "API per la gestione dei ruoli")
public class RoleController {
    @Autowired
    private RoleService roleService;

    @Operation(
        summary = "Ottieni tutti i ruoli",
        description = "Restituisce la lista di tutti i ruoli disponibili. Richiede privilegi di amministratore."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista ruoli ottenuta con successo",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoleDto.class))),
        @ApiResponse(responseCode = "403", description = "Privilegi insufficienti - richiesto ruolo ADMIN"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping
    public ResponseEntity<List<RoleDto>> getAllRoles(@RequestHeader ("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            roleService.checkRole(token, RoleLevelEnum.ADMIN);
            List<RoleDto> ruoli = roleService.getAllRoles();
            return ResponseEntity.ok(ruoli);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            System.err.println("Eccezione catturata: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
