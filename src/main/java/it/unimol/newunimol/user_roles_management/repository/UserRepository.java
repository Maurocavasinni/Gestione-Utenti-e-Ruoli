package it.unimol.newunimol.user_roles_management.repository;

import it.unimol.newunimol.user_roles_management.model.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.ruolo.nome = :nomeRuolo")
    List<User> findByNomeRuolo(@Param("nomeRuolo") String nomeRuolo);
}
