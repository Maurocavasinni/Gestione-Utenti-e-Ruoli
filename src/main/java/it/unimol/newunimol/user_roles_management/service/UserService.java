package it.unimol.newunimol.user_roles_management.service;

import it.unimol.newunimol.user_roles_management.dto.UserDto;
import it.unimol.newunimol.user_roles_management.dto.UserResponseDto;
import it.unimol.newunimol.user_roles_management.dto.converter.UserConverter;
import it.unimol.newunimol.user_roles_management.exceptions.UnknownUserException;
import it.unimol.newunimol.user_roles_management.model.Role;
import it.unimol.newunimol.user_roles_management.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import it.unimol.newunimol.user_roles_management.repository.*;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserConverter userConverter;

    public boolean createSuperAdminIfNotExists(UserDto request) {
        Optional<User> existsSuperAdmin = userRepository.findByNomeRuolo("SUPER_ADMIN");
        if (existsSuperAdmin.isPresent()) {
            return false;
        }

        User superAdmin = new User("000000", request.username(), request.email(), request.name(),
                                    request.surname(), request.password(), null);

        Optional<Role> superAdminRole = roleRepository.findByNome("SUPER_ADMIN");
        superAdmin.setRole(superAdminRole.get());
        userRepository.save(superAdmin);

        return true;
    }

    public boolean existsUserId(String id) {
        Optional<User> user = userRepository.findByMatricola(id);
        return user.isPresent();
    }

    public UserDto findByUserId(String id) throws UnknownUserException {
        Optional<User> user = userRepository.findByMatricola(id);
        if (user.isEmpty()) {
            throw new UnknownUserException("Username non trovato");
        }
        return userConverter.toDto(user.get());
    }

    public UserDto findByUsername(String username) throws UnknownUserException {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new UnknownUserException("Username non trovato");
        }
        return userConverter.toDto(user.get());
    }

    public UserDto updateUser(String id, User user) throws UnknownUserException {
        //TODO: Implementare il metodo
        return null;
    }

    public boolean deleteUser(String id) {
        //TODO: Implementare il metodo
        return true;
    }

    public UserResponseDto getUserProfile(String token) throws UnknownUserException {
        //TODO: Implementare il metodo
        return null;
    }

    public void updateUserProfile(String token, User user) throws UnknownUserException {
        //TODO: Implementare il metodo
    }

    public void resetPassword(String token, String email) throws UnknownUserException {
        //TODO: Implementare il metodo
    }

    public boolean changePassword(String token, String oldPassword, String newPassword) throws UnknownUserException {
        //TODO: Implementare il metodo
        return true;
    }
}
