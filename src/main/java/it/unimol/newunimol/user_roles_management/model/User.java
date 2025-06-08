package it.unimol.newunimol.user_roles_management.model;

import jakarta.persistence.*;

@Entity
@Table(name = "utenti")
public class User {
    @Id
    @Column(name = "id", nullable = false, length = 255)
    private String id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "nome", nullable = false, length = 50)
    private String name;

    @Column(name = "cognome", nullable = false, length = 50)
    private String surname;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "data_creazione", nullable = false)
    private Long creationDate;

    @Column(name = "ultimo_login")
    private Long lastLogin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ruolo_id", nullable = false)
    private Role ruolo;

    public User() {}

    public User(String id, String username, String email, String name, String surname, String password, Role ruolo) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.password = password;
        this.ruolo = ruolo;
        this.creationDate = System.currentTimeMillis();
        this.lastLogin = null;
    }

    public User(String id, String username, String email, String nome, String cognome,
                String password, Long creationDate, Long lastLogin, Role ruolo) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.name = nome;
        this.surname = cognome;
        this.password = password;
        this.ruolo = ruolo;
        this.creationDate = creationDate;
        this.lastLogin = lastLogin;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return this.surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getFullName() {
        return this.name + " " + this.surname;
    }

    public Role getRole() {
        return this.ruolo;
    }

    public void setRole(Role ruolo) {
        this.ruolo = ruolo;
    }

    public String getRoleName() {
        return this.ruolo.getNome();
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getCreationDate() {
        return this.creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public Long getLastLogin() {
        return this.lastLogin;
    }

    public void setLastLogin(Long lastLogin) {
        this.lastLogin = System.currentTimeMillis();
    }
}