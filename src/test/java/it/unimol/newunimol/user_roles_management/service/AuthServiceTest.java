package it.unimol.newunimol.user_roles_management.service;

import it.unimol.newunimol.user_roles_management.dto.*;
import it.unimol.newunimol.user_roles_management.dto.converter.UserConverter;
import it.unimol.newunimol.user_roles_management.exceptions.AuthException;
import it.unimol.newunimol.user_roles_management.exceptions.TokenException;
import it.unimol.newunimol.user_roles_management.exceptions.UnknownUserException;
import it.unimol.newunimol.user_roles_management.model.Role;
import it.unimol.newunimol.user_roles_management.model.User;
import it.unimol.newunimol.user_roles_management.repository.RoleRepository;
import it.unimol.newunimol.user_roles_management.repository.UserRepository;
import it.unimol.newunimol.user_roles_management.util.PasswordUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private UserConverter userConverter;
    
    @Mock
    private TokenJWTService tokenJWTService;
    
    @Mock
    private MessageService messageService;
    
    @Mock
    private RoleService roleService;
    
    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role testRole;
    private UserDto testUserDto;
    private RoleDto testRoleDto;

    @BeforeEach
    void setUp() {
        testRole = new Role("admin", "ADMIN", "Administrator");
        
        testUser = new User("000001", "testuser", "test@test.com", 
                           "Test", "User", PasswordUtils.hashPassword("password"), testRole);
        
        testRoleDto = new RoleDto("admin", "ADMIN", "Administrator");

        testUserDto = new UserDto("000001", "testuser", "test@test.com", 
                                 "Test", "User", PasswordUtils.hashPassword("password"), 
                                 System.currentTimeMillis(), System.currentTimeMillis(), testRoleDto);
    }

    @Test
    void testRegister_Success() throws AuthException {
        User newUser = new User("000002", "newuser", "new@test.com", 
                               "New", "User", "password", testRole);
        
        when(roleService.findById("admin")).thenReturn(testRoleDto);
        when(userConverter.toDto(any(User.class))).thenReturn(testUserDto);
        
        authService.register(newUser);
        
        verify(userRepository).save(any(User.class));
        verify(messageService).publishUserCreated(any(UserDto.class));
    }

    @Test
    void testRegister_UsernameAlreadyExists() {
        User existingUser = new User("000003", "testuser", "another@test.com", 
                                   "Another", "User", "password", testRole);

        assertThrows(AuthException.class, 
            () -> authService.register(existingUser));
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_NullRequest() {
        assertThrows(AuthException.class, 
            () -> authService.register(null));
    }

    @Test
    void testRegister_InvalidRole() {
        User invalidUser = new User("000001","validuser", "test@test.com", 
                                    "Test", "User", "password", testRole);
        
        when(roleService.findById("admin")).thenReturn(null);

        assertThrows(AuthException.class, 
            () -> authService.register(invalidUser));
    }

    @Test
    void testRegister_SuperAdminRole() {
        Role superAdminRole = new Role("sadmin", "SUPER_ADMIN", "Super Administrator");
        User superAdminUser = new User("000001", "validuser", "test@test.com", 
                                      "Test", "User", "password", superAdminRole);

        assertThrows(AuthException.class, 
            () -> authService.register(superAdminUser));
    }

    @Test
    void testLogin_Success() throws UnknownUserException, AuthException, TokenException {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(tokenJWTService.generateToken("000001", "testuser", "admin"))
            .thenReturn(new TokenJWTDto("valid.jwt.token"));

        TokenJWTDto tokenDto = authService.login("testuser", "password");

        assertNotNull(tokenDto);
        assertNotNull(tokenDto.token());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testLogin_UserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(AuthException.class, 
            () -> authService.login("unknown", "password"));
    }

    @Test
    void testLogin_WrongPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        assertThrows(AuthException.class, 
            () -> authService.login("testuser", "wrongpassword"));
    }

    @Test
    void testLogin_EmptyCredentials() {
        assertThrows(AuthException.class, 
            () -> authService.login("", ""));
        
        assertThrows(AuthException.class, 
            () -> authService.login(null, "password"));
            
        assertThrows(AuthException.class, 
            () -> authService.login("username", null));
    }

    @Test
    void testValidateToken_Success() {
        String token = "valid.jwt.token";
        when(tokenJWTService.isTokenValid(token)).thenReturn(true);

        boolean result = authService.validateToken(token);

        assertTrue(result);
    }

    @Test
    void testValidateToken_Invalid() {
        String token = "invalid.jwt.token";
        when(tokenJWTService.isTokenValid(token)).thenReturn(false);

        boolean result = authService.validateToken(token);

        assertFalse(result);
    }

    @Test
    void testRefreshToken_Success() throws TokenException {
        String token = "valid.jwt.token";
        TokenJWTDto newTokenDto = new TokenJWTDto("new.jwt.token");
        
        when(tokenJWTService.refreshToken(token)).thenReturn(newTokenDto);

        TokenJWTDto result = authService.refreshToken(token);

        assertNotNull(result);
        assertEquals("new.jwt.token", result.token());
    }
}
