package it.unimol.newunimol.user_roles_management.service;

import it.unimol.newunimol.user_roles_management.dto.*;
import it.unimol.newunimol.user_roles_management.dto.converter.UserConverter;
import it.unimol.newunimol.user_roles_management.exceptions.InvalidRequestException;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

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
    
    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role testRole;
    private UserDto testUserDto;
    private RoleDto testRoleDto;
    private UserCreationDto testUserCreationDto;

    @BeforeEach
    void setUp() {
        testRole = new Role("sadmin", "SUPER_ADMIN", "Super Administrator");
        
        testUser = new User("000001", "testuser", "test@test.com", 
                           "Test", "User", PasswordUtils.hashPassword("password"), testRole);
        
        testRoleDto = new RoleDto("sadmin", "SUPER_ADMIN", "Super Administrator");

        testUserDto = new UserDto("000001", "testuser", "test@test.com", 
                                 "Test", "User", "SUPER_ADMIN", System.currentTimeMillis(),
                                System.currentTimeMillis(), testRoleDto);
        
        testUserCreationDto = new UserCreationDto("testuser", "test@test.com", 
                                                  "Test", "User", "password", "SUPER_ADMIN");
    }

    @Test
    void testCreateSuperAdminIfNotExists_Success() throws InvalidRequestException {
        when(userRepository.findByNomeRuolo("SUPER_ADMIN")).thenReturn(Collections.emptyList());
        when(roleRepository.findById("sadmin")).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userConverter.toDto(any(User.class))).thenReturn(testUserDto);

        UserDto result = userService.createSuperAdminIfNotExists(testUserCreationDto);

        assertNotNull(result);
        assertEquals("testuser", result.username());
        verify(messageService).publishUserCreated(testUserDto);
    }

    @Test
    void testCreateSuperAdminIfNotExists_AlreadyExists() {
        when(userRepository.findByNomeRuolo("SUPER_ADMIN")).thenReturn(List.of(testUser));

        assertThrows(InvalidRequestException.class, 
            () -> userService.createSuperAdminIfNotExists(testUserCreationDto));
    }

    @Test
    void testCreateSuperAdminIfNotExists_NullRequest() {
        assertThrows(InvalidRequestException.class, 
            () -> userService.createSuperAdminIfNotExists(null));
    }

    @Test
    void testCreateSuperAdminIfNotExists_EmptyFields() {
        UserCreationDto invalidDto = new UserCreationDto("", "test@test.com", 
                                                         "Test", "User", "password", "SUPER_ADMIN");
        
        assertThrows(InvalidRequestException.class, 
            () -> userService.createSuperAdminIfNotExists(invalidDto));
    }

    @Test
    void testGetAllUsers() {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        ArrayList<UserProfileDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).username());
    }

    @Test
    void testExistsUserId_True() throws InvalidRequestException {
        when(userRepository.findById("000001")).thenReturn(Optional.of(testUser));

        boolean result = userService.existsUserId("000001");

        assertTrue(result);
    }

    @Test
    void testExistsUserId_False() throws InvalidRequestException {
        when(userRepository.findById("999999")).thenReturn(Optional.empty());

        boolean result = userService.existsUserId("999999");

        assertFalse(result);
    }

    @Test
    void testExistsUserId_NullId() {
        assertThrows(InvalidRequestException.class, 
            () -> userService.existsUserId(null));
    }

    @Test
    void testExistsUserId_EmptyId() {
        assertThrows(InvalidRequestException.class, 
            () -> userService.existsUserId(""));
    }

    @Test
    void testFindByUserId_Success() throws UnknownUserException {
        when(userRepository.findById("000001")).thenReturn(Optional.of(testUser));
        when(userConverter.toDto(testUser)).thenReturn(testUserDto);

        UserDto result = userService.findByUserId("000001");

        assertNotNull(result);
        assertEquals("testuser", result.username());
    }

    @Test
    void testFindByUserId_NotFound() {
        when(userRepository.findById("999999")).thenReturn(Optional.empty());

        assertThrows(UnknownUserException.class, 
            () -> userService.findByUserId("999999"));
    }

    @Test
    void testFindByUserId_NullId() {
        assertThrows(UnknownUserException.class, 
            () -> userService.findByUserId(null));
    }

    @Test
    void testFindByUsername_Success() throws UnknownUserException {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userConverter.toDto(testUser)).thenReturn(testUserDto);

        UserDto result = userService.findByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.username());
    }

    @Test
    void testFindByUsername_NotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UnknownUserException.class, 
            () -> userService.findByUsername("unknown"));
    }

    @Test
    void testFindByUsername_NullUsername() {
        assertThrows(UnknownUserException.class, 
            () -> userService.findByUsername(null));
    }

    @Test
    void testUpdateUser_Success() throws UnknownUserException {
        User updatedUserData = new User("000001", "newusername", "new@test.com", 
                                       "New", "Name", "newpassword", testRole);
        
        when(userRepository.findById("000001")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userConverter.toDto(any(User.class))).thenReturn(testUserDto);

        UserDto result = userService.updateUser("000001", updatedUserData);

        assertNotNull(result);
        verify(messageService).publishUserUpdated(testUserDto);
    }

    @Test
    void testUpdateUser_NotFound() {
        when(userRepository.findById("999999")).thenReturn(Optional.empty());

        assertThrows(UnknownUserException.class, 
            () -> userService.updateUser("999999", testUser));
    }

    @Test
    void testUpdateUser_NullId() {
        assertThrows(UnknownUserException.class, 
            () -> userService.updateUser(null, testUser));
    }

    @Test
    void testDeleteUser_Success() throws UnknownUserException {
        when(userRepository.findById("000001")).thenReturn(Optional.of(testUser));

        boolean result = userService.deleteUser("000001");

        assertTrue(result);
        verify(userRepository).delete(testUser);
        verify(messageService).publishUserDeleted("000001");
    }

    @Test
    void testDeleteUser_NotFound() {
        when(userRepository.findById("999999")).thenReturn(Optional.empty());

        assertThrows(UnknownUserException.class, 
            () -> userService.deleteUser("999999"));
    }

    @Test
    void testDeleteUser_NullId() {
        assertThrows(UnknownUserException.class, 
            () -> userService.deleteUser(null));
    }

    @Test
    void testGetUserProfile_Success() throws UnknownUserException {
        String token = "valid.jwt.token";
        when(tokenJWTService.extractUserId(token)).thenReturn("000001");
        when(userRepository.findById("000001")).thenReturn(Optional.of(testUser));

        UserProfileDto result = userService.getUserProfile(token);

        assertNotNull(result);
        assertEquals("testuser", result.username());
        assertEquals("test@test.com", result.email());
    }

    @Test
    void testGetUserProfile_NotFound() {
        String token = "valid.jwt.token";
        when(tokenJWTService.extractUserId(token)).thenReturn("999999");
        when(userRepository.findById("999999")).thenReturn(Optional.empty());

        assertThrows(UnknownUserException.class, 
            () -> userService.getUserProfile(token));
    }

    @Test
    void testGetUserProfile_NullUserId() {
        String token = "valid.jwt.token";
        when(tokenJWTService.extractUserId(token)).thenReturn(null);

        assertThrows(UnknownUserException.class, 
            () -> userService.getUserProfile(token));
    }

    @Test
    void testUpdateUserProfile_Success() throws UnknownUserException {
        String token = "valid.jwt.token";
        UserUpdaterDto updateDto = new UserUpdaterDto("newusername", "NewName", "NewSurname");
        
        when(tokenJWTService.extractUserId(token)).thenReturn("000001");
        when(userRepository.findById("000001")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateUserProfile(token, updateDto);

        verify(userRepository).save(any(User.class));
        verify(messageService).publishProfileUpdated(any(UserProfileDto.class));
    }

    @Test
    void testUpdateUserProfile_NotFound() {
        String token = "valid.jwt.token";
        UserUpdaterDto updateDto = new UserUpdaterDto("newusername", "NewName", "NewSurname");
        
        when(tokenJWTService.extractUserId(token)).thenReturn("999999");
        when(userRepository.findById("999999")).thenReturn(Optional.empty());

        assertThrows(UnknownUserException.class, 
            () -> userService.updateUserProfile(token, updateDto));
    }

    @Test
    void testResetPassword_Success() throws UnknownUserException {
        String token = "valid.jwt.token";
        String oldPassword = "password";
        
        when(tokenJWTService.extractUserId(token)).thenReturn("000001");
        when(userRepository.findById("000001")).thenReturn(Optional.of(testUser));

        assertDoesNotThrow(() -> userService.resetPassword(token, oldPassword));
        
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testResetPassword_WrongPassword() {
        String token = "valid.jwt.token";
        String wrongPassword = "wrongpassword";
        
        when(tokenJWTService.extractUserId(token)).thenReturn("000001");
        when(userRepository.findById("000001")).thenReturn(Optional.of(testUser));

        assertThrows(SecurityException.class, 
            () -> userService.resetPassword(token, wrongPassword));
    }

    @Test
    void testResetPassword_UserNotFound() {
        String token = "valid.jwt.token";
        
        when(tokenJWTService.extractUserId(token)).thenReturn("999999");
        when(userRepository.findById("999999")).thenReturn(Optional.empty());

        assertThrows(UnknownUserException.class, 
            () -> userService.resetPassword(token, "password"));
    }

    @Test
    void testChangePassword_Success() throws UnknownUserException {
        String token = "valid.jwt.token";
        String oldPassword = "password";
        String newPassword = "newpassword";
        
        when(tokenJWTService.extractUserId(token)).thenReturn("000001");
        when(userRepository.findById("000001")).thenReturn(Optional.of(testUser));

        boolean result = userService.changePassword(token, oldPassword, newPassword);

        assertTrue(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testChangePassword_WrongOldPassword() throws UnknownUserException {
        String token = "valid.jwt.token";
        String wrongOldPassword = "wrongpassword";
        String newPassword = "newpassword";
        
        when(tokenJWTService.extractUserId(token)).thenReturn("000001");
        when(userRepository.findById("000001")).thenReturn(Optional.of(testUser));

        boolean result = userService.changePassword(token, wrongOldPassword, newPassword);

        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testChangePassword_UserNotFound() {
        String token = "valid.jwt.token";
        
        when(tokenJWTService.extractUserId(token)).thenReturn("999999");
        when(userRepository.findById("999999")).thenReturn(Optional.empty());

        assertThrows(UnknownUserException.class, 
            () -> userService.changePassword(token, "password", "newpassword"));
    }

    @Test
    void testChangePassword_NullUserId() {
        String token = "valid.jwt.token";
        
        when(tokenJWTService.extractUserId(token)).thenReturn(null);

        assertThrows(UnknownUserException.class, 
            () -> userService.changePassword(token, "password", "newpassword"));
    }
}
