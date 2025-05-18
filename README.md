# Documentazione
1. Panoramica  
Il servizio “Gestione Utenti e Ruoli” ha il compito di autenticare i profili utente, consentendo una gestione delle informazioni e fornendo controllo per la gestione delle autorizzazioni nella piattaforma universitaria.  
In breve, gestisce gli utenti e assegna loro uno o più ruoli.

2. Architettura  
2.1. Funzionalità  
• Creazione Super Admin (solo in fase di inizializzazione)  
• Creazione nuovi utenti  
• Assegnazione e modifica ruoli  
• Gestione autenticazione (Login)  
• Modifica profilo utente  
• Visualizzazione utenti e ruoli  
• Rimozione utenti  

2.2. Database  
Per il funzionamento del microservizio è necessario dedicare un database relazionale in PostgreSQL, composto dalle seguenti tabelle:  
- Utenti  
- Ruoli (Super Admin, Admin, Docente, Studente)  
- Permessi  
- Altra roba che non so ora  

2.3. Comunicazione tramite RabbitMQ  
// Da riempire  

3. API Endpoint  

3.1. Riservate Super Admin  
| Metodo | Endpoint           | Descrizione                                                   |
|--------|--------------------|---------------------------------------------------------------|
| POST   | /init/superadmin   | Crea l’account con ruolo Super Admin, solo se non esiste già.|

3.2. Riservate Admin+  
| Metodo | Endpoint              | Descrizione                             |
|--------|-----------------------|-----------------------------------------|
| POST   | /users                | Creazione nuovo utente.                 |
| PUT    | /users/{id}           | Modifica i dati di un utente specifico.|
| GET    | /users/{id}           | Visualizzazione utente specifico.      |
| DELETE | /users/{id}           | Eliminazione utente specifico.         |
| GET    | /roles                | Lista dei ruoli disponibili.           |
| POST   | /users/{id}/roles     | Assegna un ruolo ad un utente.         |
| PUT    | /users/{id}/roles     | Aggiorna i ruoli di un utente.         |

3.3. Riservate Docenti+  
| Metodo | Endpoint            | Descrizione                               |
|--------|---------------------|-------------------------------------------|
| GET    | /users/students     | Restituisce la lista di studenti per docente.|

3.4. Permessi generici  
| Metodo | Endpoint                    | Descrizione                              |
|--------|-----------------------------|------------------------------------------|
| POST   | /auth/login                 | Effettua il login dell’utente e restituisce un token. |
| POST   | /auth/logout                | Effettua il logout dell’utente e invalida il token.   |
| GET    | /users/profile              | Visualizza profilo utente.              |
| PUT    | /users/profile              | Modifica profilo utente.                |
| POST   | /users/forgot-password      | Reset della password utente.            |
| PUT    | /users/change-password      | Modifica la password utente.            |
