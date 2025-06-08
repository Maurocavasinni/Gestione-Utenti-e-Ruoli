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

    public List<RoleDto> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(roleConverter::toDto)
                .collect(Collectors.toList());
    }

    public RoleDto findById(String roleId) {
        Optional<Role> role = roleRepository.findById(roleId);
        return role.map(roleConverter::toDto).orElse(null);
    }

    private void createRoleIfNotExists(String id, String nome, String descrizione) {
        if (!roleRepository.existsById(id)) {
            Role role = new Role(id, nome, descrizione);
            roleRepository.save(role);
        }
    }

    @PostConstruct
    public void initializeRoles() {
        createRoleIfNotExists("sadmin", "SUPER_ADMIN", "Amministratore di sistema con tutti i privilegi");
        createRoleIfNotExists("admin", "ADMIN", "Amministratore con privilegi di gestione utenti");
        createRoleIfNotExists("teach", "DOCENTE", "Ruolo con permessi aggiuntivi per i docenti");
        createRoleIfNotExists("student", "STUDENTE", "Ruolo base, riservato agli studenti");
    }

    public void checkRole(String token, RoleLevelEnum role) throws UnknownUserException {
        if (!tokenService.isTokenValid(token)) {
            throw new SecurityException("Token non valido o scaduto");
        }

        String userRole = tokenService.extractRole(token);

        if (!hasPermission(userRole, role)) {
            throw new SecurityException("Permessi insufficienti per questa operazione.");
        }
    }

    private boolean hasPermission(String userRole, RoleLevelEnum requiredRole) throws IllegalArgumentException {
        return RoleLevelEnum.fromRoleName(userRole).getLevel() >= requiredRole.getLevel();
    }

    public boolean assignRole(String userId, String roleId) throws IllegalArgumentException {
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
        return true;
    }
}
