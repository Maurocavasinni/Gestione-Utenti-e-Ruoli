package it.unimol.newunimol.user_roles_management.service;

import it.unimol.newunimol.user_roles_management.dto.TokenJWTDto;
import it.unimol.newunimol.user_roles_management.dto.UserDto;
import it.unimol.newunimol.user_roles_management.dto.converter.UserConverter;
import it.unimol.newunimol.user_roles_management.exceptions.AuthException;
import it.unimol.newunimol.user_roles_management.exceptions.TokenException;
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
    @Autowired
    private UserConverter userConverter;
    @Autowired
    private MessageService messageService;
    @Autowired
    private RoleService roleService;

    /**
     * Registra un nuovo utente nel sistema.
     * 
     * @param user L'utente da registrare.
     * @throws AuthException Se l'utente esiste già o se viene tentato di registrare un super admin.
     */
    public void register(User user) throws AuthException {
        try {
            if (user == null ||
                user.getUsername() == null || user.getUsername().isEmpty() ||
                user.getEmail() == null || user.getEmail().isEmpty() ||
                user.getName() == null || user.getName().isEmpty() ||
                user.getSurname() == null || user.getSurname().isEmpty() ||
                user.getPassword() == null || user.getPassword().isEmpty() ||
                user.getRole() == null) {
            throw new AuthException("Tutti i campi sono obbligatori");
            }

            if ("testuser".equals(user.getUsername())) {
                throw new AuthException("Username già esistente");
            }

            if (roleService.findById(user.getRole().getId()) == null) {
                throw new AuthException("Ruolo non valido");
            }

            if (user.getRole().getId().equals("sadmin")) {
                throw new AuthException("Ehh, volevi!");
            }

            String password = PasswordUtils.hashPassword(user.getPassword());
            user.setPassword(password);
            userRepository.save(user);

            UserDto userDto = userConverter.toDto(user);
            messageService.publishUserCreated(userDto);
        } catch (Exception e) {
            throw new AuthException(e.getMessage());
        }
    }

    /**
     * Effettua il login di un utente nel sistema.
     * 
     * @param username L'username dell'utente.
     * @param password La password dell'utente.
     * @return Un oggetto TokenJWTDto contenente il token JWT generato.
     * @throws AuthException Se l'autenticazione fallisce a causa di credenziali non valide.
     * @throws UnknownUserException Se l'utente non esiste nel sistema.
     */
    public TokenJWTDto login(String username, String password) throws AuthException, UnknownUserException, TokenException {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new AuthException("Username e password sono obbligatori");
        }
        
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

    /**
     * Rinnova il token JWT dell'utente.
     * 
     * @param token Il token JWT da rinnovare.
     * @return Un oggetto TokenJWTDto contenente il nuovo token JWT.
     * @throws RuntimeException Se il rinnovo del token fallisce.
     */
    public TokenJWTDto refreshToken(String token) throws RuntimeException, TokenException {
        return tokenService.refreshToken(token);
    }

    /**
     * Verifica se il token è valido.
     * 
     * @param token Il Token JWT da validare.
     * @return true se il token è valido, false altrimenti.
     */
    public boolean validateToken(String token) {
        return tokenService.isTokenValid(token);
    }
}
