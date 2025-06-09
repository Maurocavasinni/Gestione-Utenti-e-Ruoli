package it.unimol.newunimol.user_roles_management.controller;

import it.unimol.newunimol.user_roles_management.dto.LoginRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import it.unimol.newunimol.user_roles_management.service.AuthService;
import it.unimol.newunimol.user_roles_management.dto.TokenJWTDto;
import it.unimol.newunimol.user_roles_management.exceptions.AuthException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    
    @PostMapping("/login")
    public ResponseEntity<TokenJWTDto> login(@RequestBody LoginRequestDto loginRequest) {
        try {
            TokenJWTDto tokenDto = authService.login(loginRequest.username(), loginRequest.password());
            return ResponseEntity.ok(tokenDto);
        } catch (AuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (Exception e) {
            System.err.println("Eccezione catturata: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/logout")
    public void logout (@RequestHeader ("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);
    }

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
