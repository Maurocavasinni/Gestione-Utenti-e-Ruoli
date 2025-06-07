package it.unimol.newunimol.user_roles_management.controller;

import it.unimol.newunimol.user_roles_management.dto.*;
import it.unimol.newunimol.user_roles_management.dto.converter.UserConverter;
import it.unimol.newunimol.user_roles_management.exceptions.InvalidIdException;
import it.unimol.newunimol.user_roles_management.service.AuthService;
import it.unimol.newunimol.user_roles_management.service.RoleService;
import it.unimol.newunimol.user_roles_management.util.RoleLevelEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import it.unimol.newunimol.user_roles_management.service.UserService;
import it.unimol.newunimol.user_roles_management.model.User;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @Autowired
    private UserConverter userConverter;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private AuthService authService;

    @PostMapping("/init/superadmin")
    public ResponseEntity<Boolean> createSuperAdmin(@RequestHeader TokenJWTDto token, @RequestBody UserDto request) {
        try {
            roleService.checkRole(token.token(), RoleLevelEnum.ADMIN);
            boolean result = userService.createSuperAdminIfNotExists(request);
            return ResponseEntity.ok(result);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(false);
        }
    }
    
    @PostMapping
    public ResponseEntity<Boolean> createNewUser(@RequestHeader TokenJWTDto token, @RequestBody UserDto request) {
        try {
            roleService.checkRole(token.token(), RoleLevelEnum.ADMIN);
            User user = userConverter.convert(request);
            if (!userService.existsUserId(user.getId())) {
                authService.register(user);
                return ResponseEntity.ok(true);
            } else {
                throw new InvalidIdException("Id già in uso da un altro utente");
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        } catch (InvalidIdException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(false);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(false);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@RequestHeader TokenJWTDto token, @PathVariable String id, @RequestBody UserDto request) {
        try {
            roleService.checkRole(token.token(), RoleLevelEnum.ADMIN);
            User user = userConverter.convert(request);
            UserDto updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@RequestHeader TokenJWTDto token, @PathVariable String id) {
        try {
            roleService.checkRole(token.token(), RoleLevelEnum.ADMIN);
            UserDto user = userService.findByUserId(id);
            return ResponseEntity.ok(user);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteUser(@RequestHeader TokenJWTDto token, @PathVariable String id) {
        try {
            roleService.checkRole(token.token(), RoleLevelEnum.ADMIN);
            boolean result = userService.deleteUser(id);
            return ResponseEntity.ok(result);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(false);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponseDto> getUserProfile(@RequestHeader TokenJWTDto token) {
        try {
            UserResponseDto profile = userService.getUserProfile(token.token());
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponseDto> updateUserProfile(@RequestHeader TokenJWTDto token, @RequestBody UserDto request) {
        try {
            User user = userConverter.convert(request);
            userService.updateUserProfile(token.token(), user);
            UserResponseDto profile = userService.getUserProfile(token.token());
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestHeader TokenJWTDto token, @RequestBody ResetPasswordDto request) {
        try {
            userService.resetPassword(token.token(), request.email());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<Boolean> changePassword(@RequestHeader TokenJWTDto token, @RequestBody ChangePasswordRequestDto request) {
        try {
            boolean result = userService.changePassword(token.token(), request.oldPassword(), request.newPassword());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(false);
        }
    }

    @PostMapping("/{id}/roles")
    public ResponseEntity<Boolean> assignRole(@RequestHeader TokenJWTDto token, @PathVariable String id, @RequestBody RoleAssignmentDto role) {
        try {
            roleService.checkRole(token.token(), RoleLevelEnum.ADMIN);
            boolean result = roleService.assignRoleToUser(id, role.id());
            return ResponseEntity.ok(result);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(false);
        }
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<Boolean> updateUserRoles(@RequestHeader TokenJWTDto token, @PathVariable String id, @RequestBody RoleAssignmentDto role) {
        try {
            roleService.checkRole(token.token(), RoleLevelEnum.ADMIN);
            boolean result = roleService.updateRoleForUser(id, role.id());
            return ResponseEntity.ok(result);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(false);
        }
    }
}
