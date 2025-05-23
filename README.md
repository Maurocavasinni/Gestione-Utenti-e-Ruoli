# Documentazione

## Panoramica

Il servizio **Gestione Utenti e Ruoli** si occupa dell'autenticazione degli utenti e della gestione dei ruoli nella piattaforma universitaria. Consente di creare utenti, assegnare ruoli, gestire il login e modificare i profili.

## Funzionalità

- Creazione Super Admin (inizializzazione)
- Creazione nuovi utenti
- Assegnazione e modifica ruoli
- Autenticazione (Login)
- Modifica profilo utente
- Visualizzazione utenti e ruoli
- Rimozione utenti

## Architettura del Database (PostgreSQL)

### Tabella: Utenti

| Campo         | Tipo    | Note                              |
|---------------|---------|-----------------------------------|
| Id            | String  | Identificatore univoco            |
| Username      | String  |                                   |
| Email         | String  |                                   |
| Nome          | String  |                                   |
| Cognome       | String  |                                   |
| Ruolo         | FK      | Chiave esterna verso tabella Ruoli |
| DataCreazione | Long    | Timestamp                         |
| UltimoLogin   | Long    | Timestamp                         |

### Tabella: Ruoli

| Campo       | Tipo   |
|-------------|--------|
| Id          | String |
| Nome        | String |
| Descrizione | String |

## Comunicazione

RabbitMQ è previsto come sistema di comunicazione (da valutare).

## JWT Authentication

I token JWT vengono utilizzati per autenticare gli utenti e scambiati con tutti i microservizi del sistema.

### Payload del Token

| Campo | Descrizione |
|-------|-------------|
| sub   | Identificatore utente (id)       |
| iat   | Data creazione (Unix timestamp)  |
| exp   | Data scadenza (Unix timestamp)   |
| role  | Ruolo utente (`sadmin`, `admin`, `teach`, `user`) |

Le richieste devono includere l'header:
```
Authorization: Bearer <JWT_TOKEN>
```
### Esempio contenuto del Token

Il Token JWT è un file JSON che si presenta come segue. Tramite il payload è possibile ottenere le tre infomazioni principali per il riconoscimento utente:
- Id utente univoco
- Ruolo utente (ad ogni utente viene assegnato solo un ruolo)
- Timestamp unix di scadenza del token (3600 secondi dopo la creazione)

```
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "178001",
    "iat": 1716470423,
    "exp": 1716474023,
    "role": "admin"
  },
  "signature": "RwJ6KU8X8DQ9ImTD6O4RrmlQ9znzVz1dAQuvG3k9KZ4"
}
```

## API Endpoints

### Super Admin

| Metodo | Endpoint            | Input                        | Output  | Descrizione                         |
|--------|---------------------|------------------------------|---------|-------------------------------------|
| POST   | /init/superadmin    | username, email, password    | boolean | Crea account Super Admin se assente |

### Admin+

| Metodo | Endpoint             | Input                                     | Output                                         | Descrizione                         |
|--------|----------------------|-------------------------------------------|------------------------------------------------|-------------------------------------|
| POST   | /users               | username, email, password, nome, cognome, idRuolo | boolean                        | Creazione nuovo utente              |
| PUT    | /users/{id}          | email, nome, cognome                      | username, email, nome, cognome, ruolo | Modifica utente                         |
| GET    | /users/{id}          | (id nell’URL)                             | IdUtente, username, email, nome, cognome, ruolo, dataCreazione, ultimoLogin | Visualizzazione utente             |
| DELETE | /users/{id}          | (id nell’URL)                             | boolean                                        | Eliminazione utente                 |
| GET    | /roles               | void                                      | IdRuolo, nome, descrizione              | Lista ruoli disponibili             |
| POST   | /users/{id}/roles    | (id ruolo nell’URL)                       | boolean                                        | Assegna ruolo a utente              |
| PUT    | /users/{id}/roles    | (id ruolo nell’URL)                       | boolean                                        | Aggiorna ruoli utente               |

### Docenti+

Nessun permesso specifico per ruolo Docente

### Permessi Generici

| Metodo | Endpoint               | Input                                  | Output                                               | Descrizione                            |
|--------|------------------------|----------------------------------------|------------------------------------------------------|----------------------------------------|
| POST   | /auth/login            | username, password                      | Token JWT                                            | Login utente                           |
| POST   | /auth/logout           | Token JWT                               | void                                                 | Logout utente                          |
| POST   | /refresh-token         | Token JWT                               | Token JWT                                            | Rinnovo scadenza token                 |
| GET    | /users/profile         | Token JWT                               | IdUtente, username, email, nome, cognome, dataCreazione, ultimoLogin | Visualizza profilo utente  |
| PUT    | /users/profile         | email, nome, cognome                    | Id, username, email, nome, cognome                   | Modifica profilo utente                |
| POST   | /users/forgot-password | email                                   | void                                                 | Reset password utente                  |
| PUT    | /users/change-password | currentPassword, newPassword            | boolean                                              | Modifica password                      |

