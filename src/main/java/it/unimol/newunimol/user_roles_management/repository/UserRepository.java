package it.unimol.newunimol.user_roles_management.repository;

import it.unimol.newunimol.user_roles_management.model.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    Optional<User> findByMatricola(String userId);
    Optional<User> findByUsername(String username);
    Optional<User> findByNomeRuolo(String nomeRuolo);
}
