package it.unimol.newunimol.user_roles_management.service;

import it.unimol.newunimol.user_roles_management.dto.TokenJWTDto;
import it.unimol.newunimol.user_roles_management.exceptions.AuthException;
import it.unimol.newunimol.user_roles_management.exceptions.UnknownUserException;
import it.unimol.newunimol.user_roles_management.model.User;
import it.unimol.newunimol.user_roles_management.repository.UserRepository;
import it.unimol.newunimol.user_roles_management.util.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private TokenJWTService tokenService;
    @Autowired
    private UserRepository userRepository;


    public void register(User user) throws AuthException {
        try {
            if (user.getRole().getId().equals("sadmin")) {
                throw new AuthException("Ehh, volevi!");
            }

            String password = PasswordUtils.hashPassword(user.getPassword());
            user.setPassword(password);
            userRepository.save(user);
        } catch (Exception e) {
            throw new AuthException(e.getMessage());
        }
    }

    public TokenJWTDto login(String username, String password) throws AuthException, UnknownUserException {
        Optional<User> existsUser = userRepository.findByUsername(username);
        if (existsUser.isPresent()) {
            User user = existsUser.get();
            if (PasswordUtils.verificaPassword(user.getPassword(), password)) {
                user.setLastLogin(System.currentTimeMillis());

                userRepository.save(user);
                return tokenService.generateToken(user.getId(), user.getUsername(), user.getRole().getId());
            }
        }
        throw new AuthException("Username o password non valida");
    }

    public void logout(String token) {
        tokenService.invalidateToken(token);
    }

    public TokenJWTDto refreshToken(String token) throws RuntimeException {
        return tokenService.refreshToken(token);
    }
}
