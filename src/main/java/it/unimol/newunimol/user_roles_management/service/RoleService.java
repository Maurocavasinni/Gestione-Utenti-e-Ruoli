package it.unimol.newunimol.user_roles_management.service;

import it.unimol.newunimol.user_roles_management.dto.RoleDto;
import it.unimol.newunimol.user_roles_management.dto.converter.RoleConverter;
import it.unimol.newunimol.user_roles_management.exceptions.UnknownUserException;
import it.unimol.newunimol.user_roles_management.model.Role;
import it.unimol.newunimol.user_roles_management.model.User;
import it.unimol.newunimol.user_roles_management.repository.RoleRepository;
import it.unimol.newunimol.user_roles_management.repository.UserRepository;
import it.unimol.newunimol.user_roles_management.util.RoleLevelEnum;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private RoleConverter roleConverter;
    @Autowired
    private TokenJWTService tokenService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MessageService messageService;

    /**
     * Restituisce tutti i ruoli presenti nel sistema.
     *
     * @return Una lista di RoleDto che rappresentano i ruoli.
     */
    public List<RoleDto> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(roleConverter::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Trova un ruolo per ID nel database.
     *
     * @param roleId L'ID del ruolo da cercare.
     * @return Un RoleDto se il ruolo esiste, altrimenti null.
     */
    public RoleDto findById(String roleId) {
        if (roleId == null || roleId.isEmpty()) {
            return null;
        }

        Optional<Role> role = roleRepository.findById(roleId);
        return role.map(roleConverter::toDto).orElse(null);
    }

    /**
     * Crea un ruolo se non esiste già nel database.
     * 
     * @param id L'ID del ruolo da creare.
     * @param nome Il nome del ruolo.
     * @param descrizione La descrizione del ruolo.
     * @throws IllegalArgumentException Se l'id del ruolo non esiste.
     */
    private void createRoleIfNotExists(String id, String nome, String descrizione) throws IllegalArgumentException {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Il ruolo non può avere id nullo.");
        }
        
        if (!roleRepository.existsById(id)) {
            Role role = new Role(id, nome, descrizione);
            roleRepository.save(role);
        }
    }

    /**
     * Inizializza i ruoli di base nel database.
     * Questo metodo viene chiamato in automatico una volta all'avvio dell'applicazione.
     */
    @PostConstruct
    public void initializeRoles() throws IllegalArgumentException {
        createRoleIfNotExists("sadmin", "SUPER_ADMIN", "Amministratore di sistema con tutti i privilegi");
        createRoleIfNotExists("admin", "ADMIN", "Amministratore con privilegi di gestione utenti");
        createRoleIfNotExists("teach", "DOCENTE", "Ruolo con permessi aggiuntivi per i docenti");
        createRoleIfNotExists("student", "STUDENTE", "Ruolo base, riservato agli studenti");
    }

    /**
     * Controlla se l'utente ha il ruolo richiesto per eseguire un'operazione.
     *
     * @param token Il token JWT dell'utente.
     * @param role Il ruolo richiesto per l'operazione.
     * @throws UnknownUserException Se l'utente non esiste o il token non è valido.
     */
    public void checkRole(String token, RoleLevelEnum role) throws UnknownUserException {
        if (!tokenService.isTokenValid(token)) {
            throw new SecurityException("Token non valido o scaduto");
        }

        String userRole = tokenService.extractRole(token);

        if (!(RoleLevelEnum.fromRoleName(userRole).getLevel() >= role.getLevel())) {
            throw new SecurityException("Permessi insufficienti per questa operazione.");
        }
    }

    /**
     * Assegna un ruolo a un utente.
     *
     * @param userId L'ID dell'utente a cui assegnare il ruolo.
     * @param roleId L'ID del ruolo da assegnare.
     * @return true se il ruolo è stato assegnato, false se l'utente ha già quel ruolo.
     * @throws IllegalArgumentException Se l'utente o il ruolo non esistono.
     */
    public boolean assignRole(String userId, String roleId) throws IllegalArgumentException {
        if (userId == null || userId.isEmpty() || roleId == null || roleId.isEmpty()) {
            throw new IllegalArgumentException("Id non valido.");
        }
        
        Optional<User> userTemp = userRepository.findById(userId);
        Optional<Role> roleTemp = roleRepository.findById(roleId);

        if (userTemp.isEmpty() || roleTemp.isEmpty()) {
            throw new IllegalArgumentException("Parametro non valido");
        }

        User user = userTemp.get();
        Role role = roleTemp.get();

        if (user.getRole() != null && user.getRole().getId().equals(role.getId())) {
            return false;
        }

        user.setRole(role);
        userRepository.save(user);
        messageService.publishRoleAssigned(userId, roleId);
        return true;
    }
}
