package it.unimol.newunimol.user_roles_management.controller;

import it.unimol.newunimol.user_roles_management.dto.*;
import it.unimol.newunimol.user_roles_management.dto.converter.UserConverter;
import it.unimol.newunimol.user_roles_management.dto.converter.UserCreationConverter;
import it.unimol.newunimol.user_roles_management.exceptions.InvalidIdException;
import it.unimol.newunimol.user_roles_management.exceptions.UnknownUserException;
import it.unimol.newunimol.user_roles_management.service.AuthService;
import it.unimol.newunimol.user_roles_management.service.RoleService;
import it.unimol.newunimol.user_roles_management.service.TokenJWTService;
import it.unimol.newunimol.user_roles_management.util.RoleLevelEnum;

import java.util.ArrayList;
import java.util.List;

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
import it.unimol.newunimol.user_roles_management.service.UserService;
import it.unimol.newunimol.user_roles_management.model.User;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "API per la gestione degli utenti")
public class UserController {
    @Autowired
    private UserConverter userConverter;
    @Autowired
    private UserCreationConverter userCreationConverter;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private AuthService authService;
    @Autowired
    private TokenJWTService tokenJWTService;

    @Operation(
        summary = "Crea SuperAdmin iniziale",
        description = "Crea il primo SuperAdmin del sistema se non esiste già"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "SuperAdmin creato con successo",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "403", description = "SuperAdmin già esistente"),
        @ApiResponse(responseCode = "400", description = "Dati non validi")
    })
    @PostMapping("/init/superadmin")
    public ResponseEntity<UserDto> createSuperAdmin(@RequestBody UserCreationDto request) {
        try {
            UserDto newUser = userService.createSuperAdminIfNotExists(request);
            return ResponseEntity.ok(newUser);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            System.err.println("Eccezione catturata: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(
        summary = "Crea nuovo utente",
        description = "Crea un nuovo utente nel sistema. Richiede privilegi di amministratore."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Utente creato con successo",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "403", description = "Privilegi insufficienti"),
        @ApiResponse(responseCode = "400", description = "Dati non validi")
    })
    @PostMapping
    public ResponseEntity<UserDto> createNewUser(@RequestHeader ("Authorization") String authHeader, @RequestBody UserCreationDto request) {
        try {
            String token = authHeader.replace("Bearer ", "");
            roleService.checkRole(token, RoleLevelEnum.ADMIN);
            User user = userCreationConverter.convert(request);
            authService.register(user);
            return ResponseEntity.ok(userConverter.toDto(user));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            System.err.println("Eccezione catturata: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
        summary = "Ottieni tutti gli utenti",
        description = "Restituisce la lista di tutti gli utenti. Richiede privilegi di amministratore."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista utenti ottenuta con successo",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserProfileDto.class))),
        @ApiResponse(responseCode = "403", description = "Privilegi insufficienti"),
        @ApiResponse(responseCode = "400", description = "Errore nella richiesta")
    })
    @GetMapping
    public ResponseEntity<List<UserProfileDto>> getAllUsers(@RequestHeader ("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            roleService.checkRole(token, RoleLevelEnum.ADMIN);
            ArrayList<UserProfileDto> utenti = userService.getAllUsers();
            return ResponseEntity.ok(utenti);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            System.err.println("Eccezione catturata: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }

    }
    
    @Operation(
        summary = "Aggiorna utente",
        description = "Aggiorna i dati di un utente esistente. Richiede privilegi di amministratore."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Utente aggiornato con successo",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "403", description = "Privilegi insufficienti"),
        @ApiResponse(responseCode = "400", description = "Dati non validi")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@RequestHeader ("Authorization") String authHeader, @PathVariable String id, @RequestBody UserDto request) {
        try {
            String token = authHeader.replace("Bearer ", "");
            roleService.checkRole(token, RoleLevelEnum.ADMIN);
            User user = userConverter.convert(request);
            UserDto updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            System.err.println("Eccezione catturata: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(
        summary = "Ottieni utente per ID",
        description = "Restituisce i dettagli di un utente specifico. Richiede privilegi di amministratore."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Utente trovato",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "403", description = "Privilegi insufficienti"),
        @ApiResponse(responseCode = "404", description = "Utente non trovato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@RequestHeader ("Authorization") String authHeader, @PathVariable String id) {
        try {
            String token = authHeader.replace("Bearer ", "");
            roleService.checkRole(token, RoleLevelEnum.ADMIN);
            UserDto user = userService.findByUserId(id);
            return ResponseEntity.ok(user);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (UnknownUserException e) {
            return ResponseEntity.notFound().build();
        }  catch (Exception e) {
            System.err.println("Eccezione catturata: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
        summary = "Elimina utente",
        description = "Elimina un utente dal sistema. Richiede privilegi di amministratore."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Utente eliminato con successo"),
        @ApiResponse(responseCode = "403", description = "Privilegi insufficienti"),
        @ApiResponse(responseCode = "400", description = "Richiesta non valida")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteUser(@RequestHeader ("Authorization") String authHeader, @PathVariable String id) {
        try {
            String token = authHeader.replace("Bearer ", "");
            roleService.checkRole(token, RoleLevelEnum.ADMIN);
            if (tokenJWTService.extractUserId(token).equals(id)) {
                throw new InvalidIdException("Richiesta non valida.");
            }
            boolean result = userService.deleteUser(id);
            return ResponseEntity.ok(result);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        } catch (Exception e) {
            System.err.println("Eccezione catturata: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return ResponseEntity.badRequest().body(false);
        }
    }

    @Operation(
        summary = "Ottieni profilo utente corrente",
        description = "Restituisce il profilo dell'utente autenticato"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profilo ottenuto con successo",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserProfileDto.class))),
        @ApiResponse(responseCode = "400", description = "Token non valido")
    })
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getUserProfile(@RequestHeader ("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            UserProfileDto profile = userService.getUserProfile(token);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            System.err.println("Eccezione catturata: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
        summary = "Aggiorna profilo utente corrente",
        description = "Aggiorna il profilo dell'utente autenticato"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profilo aggiornato con successo",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserProfileDto.class))),
        @ApiResponse(responseCode = "400", description = "Dati non validi")
    })
    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateUserProfile(@RequestHeader ("Authorization") String authHeader, @RequestBody UserUpdaterDto request) {
        try {
            String token = authHeader.replace("Bearer ", "");
            userService.updateUserProfile(token, request);
            UserProfileDto profile = userService.getUserProfile(token);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            System.err.println("Eccezione catturata: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
        summary = "Reset password",
        description = "Resetta la password dell'utente corrente a una temporanea"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password resettata con successo"),
        @ApiResponse(responseCode = "400", description = "Password attuale errata")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestHeader ("Authorization") String authHeader, @RequestBody ChangePasswordRequestDto request) {
        try {
            String token = authHeader.replace("Bearer ", "");
            userService.resetPassword(token, request.oldPassword());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Eccezione catturata: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
        summary = "Cambia password",
        description = "Cambia la password dell'utente corrente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password cambiata con successo"),
        @ApiResponse(responseCode = "400", description = "Password attuale errata o nuova password non valida")
    })
    @PutMapping("/change-password")
    public ResponseEntity<Boolean> changePassword(@RequestHeader ("Authorization") String authHeader, @RequestBody ChangePasswordRequestDto request) {
        try {
            String token = authHeader.replace("Bearer ", "");
            boolean result = userService.changePassword(token, request.oldPassword(), request.newPassword());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Eccezione catturata: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return ResponseEntity.badRequest().body(false);
        }
    }

    @Operation(
        summary = "Assegna ruolo a utente",
        description = "Assegna un ruolo specifico a un utente. Richiede privilegi di amministratore."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ruolo assegnato con successo"),
        @ApiResponse(responseCode = "403", description = "Privilegi insufficienti"),
        @ApiResponse(responseCode = "400", description = "Dati non validi")
    })
    @PostMapping("/{id}/roles")
    public ResponseEntity<Boolean> assignRole(@RequestHeader ("Authorization") String authHeader, @PathVariable String id, @RequestBody RoleAssignmentDto role) {
        try {
            String token = authHeader.replace("Bearer ", "");
            roleService.checkRole(token, RoleLevelEnum.ADMIN);
            boolean result = roleService.assignRole(id, role.roleId());
            return ResponseEntity.ok(result);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        } catch (Exception e) {
            System.err.println("Eccezione catturata: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return ResponseEntity.badRequest().body(false);
        }
    }

    @Operation(
        summary = "Aggiorna ruolo utente",
        description = "Aggiorna il ruolo di un utente. Richiede privilegi di amministratore."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ruolo aggiornato con successo"),
        @ApiResponse(responseCode = "403", description = "Privilegi insufficienti"),
        @ApiResponse(responseCode = "400", description = "Dati non validi")
    })
    @PutMapping("/{id}/roles")
    public ResponseEntity<Boolean> updateUserRoles(@RequestHeader ("Authorization") String authHeader, @PathVariable String id, @RequestBody RoleAssignmentDto role) {
        try {
            String token = authHeader.replace("Bearer ", "");
            roleService.checkRole(token, RoleLevelEnum.ADMIN);
            boolean result = roleService.assignRole(id, role.roleId());
            return ResponseEntity.ok(result);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        } catch (Exception e) {
            System.err.println("Eccezione catturata: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return ResponseEntity.badRequest().body(false);
        }
    }
}
