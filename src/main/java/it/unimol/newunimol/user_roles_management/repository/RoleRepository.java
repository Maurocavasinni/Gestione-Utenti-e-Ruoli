package it.unimol.newunimol.user_roles_management.repository;

import it.unimol.newunimol.user_roles_management.model.Role;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    
    Optional<Role> findByNome(String nome);
}
