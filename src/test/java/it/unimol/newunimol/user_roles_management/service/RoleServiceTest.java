package it.unimol.newunimol.user_roles_management.service;

import it.unimol.newunimol.user_roles_management.dto.RoleDto;
import it.unimol.newunimol.user_roles_management.dto.converter.RoleConverter;
import it.unimol.newunimol.user_roles_management.exceptions.UnknownUserException;
import it.unimol.newunimol.user_roles_management.model.Role;
import it.unimol.newunimol.user_roles_management.model.User;
import it.unimol.newunimol.user_roles_management.repository.RoleRepository;
import it.unimol.newunimol.user_roles_management.repository.UserRepository;
import it.unimol.newunimol.user_roles_management.util.RoleLevelEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private RoleConverter roleConverter;
    
    @Mock
    private TokenJWTService tokenService;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private MessageService messageService;
    
    @InjectMocks
    private RoleService roleService;

    private Role testRole;
    private RoleDto testRoleDto;
    private User testUser;

    @BeforeEach
    void setUp() {
        testRole = new Role("admin", "ADMIN", "Administrator");
        testRoleDto = new RoleDto("admin", "ADMIN", "Administrator");
        testUser = new User("000001", "testuser", "test@test.com", 
                           "Test", "User", "password", testRole);
    }

    @Test
    void testGetAllRoles() {
        List<Role> roles = Arrays.asList(testRole);
        when(roleRepository.findAll()).thenReturn(roles);
        when(roleConverter.toDto(testRole)).thenReturn(testRoleDto);

        List<RoleDto> result = roleService.getAllRoles();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(roleRepository).findAll();
    }

    @Test
    void testFindById_Success() {
        when(roleRepository.findById("admin")).thenReturn(Optional.of(testRole));
        when(roleConverter.toDto(testRole)).thenReturn(testRoleDto);

        RoleDto result = roleService.findById("admin");

        assertNotNull(result);
        assertEquals("admin", result.id());
        verify(roleRepository).findById("admin");
    }

    @Test
    void testFindById_NotFound() {
        when(roleRepository.findById("unknown")).thenReturn(Optional.empty());

        RoleDto result = roleService.findById("unknown");

        assertNull(result);
        verify(roleRepository).findById("unknown");
    }

    @Test
    void testFindById_NullId() {
        RoleDto result = roleService.findById(null);

        assertNull(result);
        verify(roleRepository, never()).findById(any());
    }

    @Test
    void testFindById_EmptyId() {
        RoleDto result = roleService.findById("");

        assertNull(result);
        verify(roleRepository, never()).findById(any());
    }

    @Test
    void testInitializeRoles() {
        when(roleRepository.existsById(anyString())).thenReturn(false);

        assertDoesNotThrow(() -> roleService.initializeRoles());

        verify(roleRepository, times(4)).save(any(Role.class));
    }

    @Test
    void testInitializeRoles_AlreadyExist() {
        when(roleRepository.existsById(anyString())).thenReturn(true);

        assertDoesNotThrow(() -> roleService.initializeRoles());

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void testCheckRole_ValidToken() throws UnknownUserException {
        String token = "valid.jwt.token";
        when(tokenService.isTokenValid(token)).thenReturn(true);
        when(tokenService.extractRole(token)).thenReturn("ADMIN");

        assertDoesNotThrow(() -> roleService.checkRole(token, RoleLevelEnum.ADMIN));
    }

    @Test
    void testCheckRole_InvalidToken() {
        String token = "invalid.jwt.token";
        when(tokenService.isTokenValid(token)).thenReturn(false);

        assertThrows(SecurityException.class, 
            () -> roleService.checkRole(token, RoleLevelEnum.ADMIN));
    }

    @Test
    void testCheckRole_InsufficientPermissions() {
        String token = "valid.jwt.token";
        when(tokenService.isTokenValid(token)).thenReturn(true);
        when(tokenService.extractRole(token)).thenReturn("STUDENTE");

        assertThrows(IllegalArgumentException.class, 
            () -> roleService.checkRole(token, RoleLevelEnum.ADMIN));
    }

    @Test
    void testAssignRole_Success() {
        Role newRole = new Role("teach", "DOCENTE", "Teacher");
        when(userRepository.findById("000001")).thenReturn(Optional.of(testUser));
        when(roleRepository.findById("teach")).thenReturn(Optional.of(newRole));

        boolean result = roleService.assignRole("000001", "teach");

        assertTrue(result);
        verify(userRepository).save(testUser);
        verify(messageService).publishRoleAssigned("000001", "teach");
    }

    @Test
    void testAssignRole_SameRole() {
        when(userRepository.findById("000001")).thenReturn(Optional.of(testUser));
        when(roleRepository.findById("admin")).thenReturn(Optional.of(testRole));

        boolean result = roleService.assignRole("000001", "admin");

        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
        verify(messageService, never()).publishRoleAssigned(anyString(), anyString());
    }

    @Test
    void testAssignRole_UserNotFound() {
        when(userRepository.findById("999999")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, 
            () -> roleService.assignRole("999999", "admin"));
    }

    @Test
    void testAssignRole_RoleNotFound() {
        when(userRepository.findById("000001")).thenReturn(Optional.of(testUser));
        when(roleRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, 
            () -> roleService.assignRole("000001", "unknown"));
    }

    @Test
    void testAssignRole_NullParameters() {
        assertThrows(IllegalArgumentException.class, 
            () -> roleService.assignRole(null, "admin"));
        
        assertThrows(IllegalArgumentException.class, 
            () -> roleService.assignRole("000001", null));
        
        assertThrows(IllegalArgumentException.class, 
            () -> roleService.assignRole("", "admin"));
    }
}
