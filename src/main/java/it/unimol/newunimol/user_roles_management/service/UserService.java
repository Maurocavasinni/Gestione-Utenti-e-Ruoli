package it.unimol.newunimol.user_roles_management.service;

import it.unimol.newunimol.user_roles_management.dto.UserDto;
import it.unimol.newunimol.user_roles_management.dto.UserProfileDto;
import it.unimol.newunimol.user_roles_management.dto.converter.UserConverter;
import it.unimol.newunimol.user_roles_management.exceptions.UnknownUserException;
import it.unimol.newunimol.user_roles_management.model.Role;
import it.unimol.newunimol.user_roles_management.model.User;
import it.unimol.newunimol.user_roles_management.util.PasswordUtils;
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
    @Autowired
    private TokenJWTService tokenJWTService;

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

    public UserDto updateUser(String userId, User userData) throws UnknownUserException {
        Optional<User> userTemp = userRepository.findByMatricola(userId);
        if (userTemp.isEmpty()) {
            throw new UnknownUserException("Utente non trovato.");
        }
        User user = userTemp.get();

        user.setUsername(userData.getUsername());
        user.setEmail(userData.getEmail());
        user.setName(userData.getName());
        user.setSurname(userData.getSurname());
        user.setPassword(userData.getPassword());

        userRepository.save(user);
        return userConverter.toDto(user);
    }

    public boolean deleteUser(String userId) {
        Optional<User> user = userRepository.findByMatricola(userId);
        if (user.isPresent()) {
            userRepository.delete(user.get());
            return true;
        }
        return false;
    }

    public UserProfileDto getUserProfile(String token) throws UnknownUserException {
        String userId = tokenJWTService.extractUserId(token);
        Optional<User> userTemp = userRepository.findByMatricola(userId);
        if (userTemp.isEmpty()) {
            throw new UnknownUserException("Utente non trovato");
        }

        User user = userTemp.get();
        return new UserProfileDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getSurname(),
                user.getRoleName(),
                user.getCreationDate(),
                user.getLastLogin()
        );
    }

    public void updateUserProfile(String token, User userData) throws UnknownUserException {
        String userId = tokenJWTService.extractUserId(token);
        Optional<User> userTemp = userRepository.findByMatricola(userId);
        if (userTemp.isEmpty()) {
            throw new UnknownUserException("Utente non trovato");
        }

        User user = userTemp.get();
        user.setUsername(userData.getUsername());
        user.setEmail(userData.getEmail());
        user.setName(userData.getName());
        user.setSurname(userData.getSurname());

        userRepository.save(user);
    }

    public void resetPassword(String token, String oldPassword) throws UnknownUserException {
        String userId = tokenJWTService.extractUserId(token);
        Optional<User> userTemp = userRepository.findByMatricola(userId);
        if (userTemp.isEmpty()) {
            throw new UnknownUserException("Utente non trovato");
        }

        User user = userTemp.get();
        if (!PasswordUtils.verificaPassword(user.getPassword(), oldPassword)) {
            throw new SecurityException("Password errata");
        }

        String tempPassword = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        user.setPassword(PasswordUtils.hashPassword(tempPassword));
        userRepository.save(user);
    }

    public boolean changePassword(String token, String oldPassword, String newPassword) throws UnknownUserException {
        String userId = tokenJWTService.extractUserId(token);
        Optional<User> userTemp = userRepository.findByMatricola(userId);
        if (userTemp.isEmpty()) {
            throw new UnknownUserException("Utente non trovato");
        }

        User user = userTemp.get();
        if (!PasswordUtils.verificaPassword(user.getPassword(), oldPassword)) {
            return false;
        }

        user.setPassword(PasswordUtils.hashPassword(newPassword));
        userRepository.save(user);

        return true;
    }
}
