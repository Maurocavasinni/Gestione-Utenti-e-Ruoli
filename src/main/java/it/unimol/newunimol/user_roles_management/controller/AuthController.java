package it.unimol.newunimol.user_roles_management.controller;

import it.unimol.newunimol.user_roles_management.dto.LoginRequestDto;
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
import it.unimol.newunimol.user_roles_management.service.AuthService;
import it.unimol.newunimol.user_roles_management.dto.TokenJWTDto;
import it.unimol.newunimol.user_roles_management.exceptions.AuthException;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "API per l'autenticazione degli utenti")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Operation(
        summary = "Autenticazione utente",
        description = "Effettua il login di un utente e restituisce un token JWT per l'autenticazione"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login effettuato con successo",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TokenJWTDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Credenziali non valide",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Richiesta non valida",
            content = @Content
        )
    })
    @PostMapping("/login")
    public ResponseEntity<TokenJWTDto> login(@RequestBody LoginRequestDto loginRequest) {
        try {
            TokenJWTDto tokenDto = authService.login(loginRequest.username(), loginRequest.password());
            return ResponseEntity.ok(tokenDto);
        } catch (AuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            System.err.println("Eccezione catturata: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
        summary = "Logout utente",
        description = "Invalida il token JWT dell'utente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout effettuato con successo"),
        @ApiResponse(responseCode = "401", description = "Token non valido"),
        @ApiResponse(responseCode = "400", description = "Header Authorization mancante o malformato")
    })
    @PostMapping("/logout")
    public void logout (@RequestHeader ("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);
    }

    @Operation(
        summary = "Refresh token",
        description = "Genera un nuovo token JWT a partire da uno esistente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token rinnovato con successo",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenJWTDto.class))),
        @ApiResponse(responseCode = "401", description = "Token scaduto o non valido"),
        @ApiResponse(responseCode = "400", description = "Header Authorization mancante o malformato")
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenJWTDto> refreshToken(@RequestHeader ("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            TokenJWTDto newToken = authService.refreshToken(token);
            return ResponseEntity.ok(newToken);
        } catch (Exception e) {
            System.err.println("Eccezione catturata: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
