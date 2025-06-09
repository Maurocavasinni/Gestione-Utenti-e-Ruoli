package it.unimol.newunimol.user_roles_management.service;

import it.unimol.newunimol.user_roles_management.dto.UserCreationDto;
import it.unimol.newunimol.user_roles_management.dto.UserProfileDto;
import it.unimol.newunimol.user_roles_management.dto.UserUpdaterDto;
import it.unimol.newunimol.user_roles_management.dto.UserDto;
import it.unimol.newunimol.user_roles_management.dto.converter.UserConverter;
import it.unimol.newunimol.user_roles_management.exceptions.UnknownUserException;
import it.unimol.newunimol.user_roles_management.exceptions.InvalidRequestException;
import it.unimol.newunimol.user_roles_management.model.Role;
import it.unimol.newunimol.user_roles_management.model.User;
import it.unimol.newunimol.user_roles_management.util.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import it.unimol.newunimol.user_roles_management.repository.*;

import java.util.ArrayList;
import java.util.List;
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
    @Autowired
    private MessageService messageService;

    /**
     * Crea un SuperAdmin se non esiste già.
     *
     * @param request Dati per la creazione del SuperAdmin.
     * @return UserDto rappresentante il SuperAdmin creato.
     * @throws InvalidRequestException Se un SuperAdmin esiste già.
     */
    public UserDto createSuperAdminIfNotExists(UserCreationDto request) throws InvalidRequestException {
        List<User> superAdmins = userRepository.findByNomeRuolo("SUPER_ADMIN");
        if (!superAdmins.isEmpty()) {
            throw new InvalidRequestException("SuperAdmin già esistente");
        }

        User superAdmin = new User("000000", request.username(), request.email(), request.name(),
                                    request.surname(), PasswordUtils.hashPassword(request.password()), null);

        Optional<Role> superAdminRole = roleRepository.findById("sadmin");
        superAdmin.setRole(superAdminRole.get());
        userRepository.save(superAdmin);

        UserDto userDto = userConverter.toDto(superAdmin);
        messageService.publishUserCreated(userDto);
        
        return userDto;
    }

    /**
     * Recupera tutti gli utenti dal database e li converte in UserProfileDto.
     * 
     * @return Lista di UserProfileDto contenente i profili di tutti gli utenti.
     */
    public ArrayList<UserProfileDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        ArrayList<UserProfileDto> userProfiles = new ArrayList<>();
    
        for (User user : users) {
            UserProfileDto userProfile = new UserProfileDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getSurname(),
                user.getRole().getId(),
                user.getCreationDate(),
                user.getLastLogin()
            );
            userProfiles.add(userProfile);
        }
        return userProfiles;
    }

    /**
     * Verifica se un utente con l'ID specificato esiste nel database.
     * 
     * @param id ID dell'utente da verificare.
     * @return true se l'utente esiste, false altrimenti.
     */
    public boolean existsUserId(String id) {
        Optional<User> user = userRepository.findById(id);
        return user.isPresent();
    }

    /**
     * Trova un utente per ID.
     * 
     * @param id ID dell'utente da cercare.
     * @return UserDto rappresentante l'utente trovato.
     * @throws UnknownUserException Se l'utente non viene trovato.
     */
    public UserDto findByUserId(String id) throws UnknownUserException {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new UnknownUserException("Username non trovato");
        }
        return userConverter.toDto(user.get());
    }

    /**
     * Trova un utente per username.
     * 
     * @param username Nome utente da cercare.
     * @return UserDto rappresentante l'utente trovato.
     * @throws UnknownUserException Se l'utente non viene trovato.
     */
    public UserDto findByUsername(String username) throws UnknownUserException {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new UnknownUserException("Username non trovato");
        }
        return userConverter.toDto(user.get());
    }

    /**
     * Aggiorna i dati di un utente esistente.
     * 
     * @param userId ID dell'utente da aggiornare.
     * @param userData Dati aggiornati dell'utente.
     * @return UserDto rappresentante l'utente aggiornato.
     * @throws UnknownUserException Se l'utente non viene trovato.
     */
    public UserDto updateUser(String userId, User userData) throws UnknownUserException {
        Optional<User> userTemp = userRepository.findById(userId);
        if (userTemp.isEmpty()) {
            throw new UnknownUserException("Utente non trovato.");
        }
        User user = userTemp.get();

        user.setId(userData.getId());
        user.setUsername(userData.getUsername());
        user.setEmail(userData.getEmail());
        user.setName(userData.getName());
        user.setSurname(userData.getSurname());
        user.setPassword(PasswordUtils.hashPassword(userData.getPassword()));

        userRepository.save(user);
        UserDto userDto = userConverter.toDto(user);
        messageService.publishUserUpdated(userDto);

        return userDto;
    }

    /**
     * Elimina un utente dal database.
     * 
     * @param userId ID dell'utente da eliminare.
     * @return true se l'utente è stato eliminato, false se l'utente non esiste.
     */
    public boolean deleteUser(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            userRepository.delete(user.get());
            messageService.publishUserDeleted(userId);
            return true;
        }
        return false;
    }

    /**
     * Recupera il profilo utente corrente basato sul token JWT.
     * 
     * @param token Token JWT dell'utente.
     * @return UserProfileDto contenente i dettagli del profilo utente.
     * @throws UnknownUserException Se l'utente non viene trovato.
     */
    public UserProfileDto getUserProfile(String token) throws UnknownUserException {
        String userId = tokenJWTService.extractUserId(token);
        Optional<User> userTemp = userRepository.findById(userId);
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

    /**
     * Aggiorna il profilo utente corrente basato sul token JWT.
     * 
     * @param token Token JWT dell'utente.
     * @param userData Dati aggiornati dell'utente.
     * @throws UnknownUserException Se l'utente non viene trovato.
     */
    public void updateUserProfile(String token, UserUpdaterDto userData) throws UnknownUserException {
        String userId = tokenJWTService.extractUserId(token);
        Optional<User> userTemp = userRepository.findById(userId);
        if (userTemp.isEmpty()) {
            throw new UnknownUserException("Utente non trovato");
        }

        User user = userTemp.get();
        user.setUsername(userData.username());
        user.setName(userData.name());
        user.setSurname(userData.surname());

        userRepository.save(user);

        UserProfileDto profile = getUserProfile(token);
        messageService.publishProfileUpdated(profile);
    }

    /**
     * Resetta la password dell'utente corrente basato sul token JWT.
     * 
     * @param token Token JWT dell'utente.
     * @param oldPassword Password attuale dell'utente.
     * @throws UnknownUserException Se l'utente non viene trovato.
     * @throws SecurityException Se la password attuale non corrisponde.
     */
    public void resetPassword(String token, String oldPassword) throws UnknownUserException {
        String userId = tokenJWTService.extractUserId(token);
        Optional<User> userTemp = userRepository.findById(userId);
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

    /**
     * Cambia la password dell'utente corrente basato sul token JWT.
     * 
     * @param token Token JWT dell'utente.
     * @param oldPassword Password attuale dell'utente.
     * @param newPassword Nuova password da impostare.
     * @return true se la password è stata cambiata con successo, false altrimenti.
     * @throws UnknownUserException Se l'utente non viene trovato.
     */
    public boolean changePassword(String token, String oldPassword, String newPassword) throws UnknownUserException {
        String userId = tokenJWTService.extractUserId(token);
        Optional<User> userTemp = userRepository.findById(userId);
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
