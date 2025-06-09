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

import it.unimol.newunimol.user_roles_management.service.UserService;
import it.unimol.newunimol.user_roles_management.model.User;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1/users")
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
