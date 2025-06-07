package it.unimol.newunimol.user_roles_management.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ruoli")
public class Role {
    @Id
    @Column(name = "id", nullable = false, length = 255)
    private String id;

    @Column(name = "nome", nullable = false, unique = true, length = 100)
    private String nome;

    @Column(name = "descrizione", length = 500)
    private String descrizione;

    public Role(String id, String nome, String descrizione) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescrizione() {
        return this.descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(this.nome);
    }
    
    public boolean isAdmin() {
        return "ADMIN".equals(this.nome);
    }
    
    public boolean isTeacher() {
        return "DOCENTE".equals(this.nome);
    }
    
    public boolean isStudent() {
        return "STUDENTE".equals(this.nome);
    }
}
