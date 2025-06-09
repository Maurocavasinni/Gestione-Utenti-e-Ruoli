# Documentazione

## Panoramica

Il servizio **Gestione Utenti e Ruoli** si occupa dell'autenticazione degli utenti e della gestione dei ruoli nella piattaforma universitaria. Consente di creare utenti, assegnare ruoli, gestire il login e modificare i profili.

## Funzionalità

- Creazione Super Admin (inizializzazione)
- Creazione nuovi utenti
- Assegnazione e modifica ruoli
- Autenticazione
- Modifica profilo utente
- Visualizzazione utenti e ruoli
- Rimozione utenti

## Portda di Default

```
Server: http://localhost:23109
```

## Documentazione API

```
Swagger UI: http://localhost:23109/swagger-ui.html
```

## Architettura del Database (PostgreSQL)

### Tabella: Utenti

| Campo         | Tipo    | Note                              |
|---------------|---------|-----------------------------------|
| Id            | String  | Identificatore univoco (6 cifre)  |
| Username      | String  |                                   |
| Email         | String  |                                   |
| Nome          | String  |                                   |
| Cognome       | String  |                                   |
| Password      | String  | Password hashata con Argon2       |
| DataCreazione | Long    | Timestamp                         |
| UltimoLogin   | Long    | Timestamp                         |
| Ruolo         | FK      | Chiave esterna verso tabella Ruoli |

### Tabella: Ruoli

| Campo       | Tipo   |
|-------------|--------|
| Id          | String |
| Nome        | String |
| Descrizione | String |

### Ruoli Predefiniti

| ID       | Nome         | Livello | Descrizione                                    |
|----------|--------------|---------|------------------------------------------------|
| `student`| STUDENTE     | 0       | Ruolo base per studenti                        |
| `teach`  | DOCENTE      | 1       | Ruolo per docenti con permessi aggiuntivi      |
| `admin`  | ADMIN        | 2       | Amministratore con gestione utenti             |
| `sadmin` | SUPER_ADMIN  | 3       | Amministratore di sistema con tutti i privilegi |

## Comunicazione

È stato concordato l'utilizzo di **RabbitMQ** come sistema di messaggistica asincrono.
Il servizio Gestione Utenti e Ruoli pubblica le modifiche al database come segue:
```
Exchange: users.exchange (Topic)
├── user.created   → Notifica creazione utente
├── user.updated   → Notifica aggiornamento utente  
├── user.deleted   → Notifica eliminazione utente
└── role.assigned  → Notifica assegnazione ruolo
```

## JWT Authentication

I token JWT vengono utilizzati per autenticare gli utenti e scambiati con tutti i microservizi del sistema.

### Configurazione predefinita

Nel file `application.properties` sono stati impostati i seguenti campi:
- **Scadenza**: 3600 secondi (1 ora)
- **Chiavi**: coppia pubblica-privata a 2048 bit codificata in formato Base64

### Esempio contenuto del Token

Il Token JWT è un file JSON che si presenta come segue. Tramite il payload è possibile ottenere le tre infomazioni principali per il riconoscimento utente:
- Id utente univoco (matricola)
- Ruolo utente (ad ogni utente viene assegnato solo un ruolo)
- Timestamp unix di scadenza del token (3600 secondi dopo la creazione)

```
{
  "header": {
    "alg": "RS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "178001",
    "iat": 1716470423,
    "exp": 1716474023,
    "username": "mauro.cavasinni",
    "role": "admin"
  },
  "signature": "RwJ6KU8X8DQ9ImTD6O4RrmlQ9znzVz1dAQuvG3k9KZ4"
}
```
### Payload del Token

| Campo | Descrizione |
|-------|-------------|
| sub   | Identificatore utente (matricola)       |
| iat   | Data creazione (Unix timestamp)  |
| exp   | Data scadenza (Unix timestamp)   |
| username   | Nome e Cognome utente              |
| role  | Ruolo utente (`sadmin`, `admin`, `teach`, `student`) |

### Postman & Co.

Si sconsiglia il testing tramite Swagger UI, poichè non comunica correttamente l'header alla Business Logic.
Se si vuole testare lo stub del progetto (fornito sul gruppo Discord), seguire questi semplici passaggi:
1. Il token, necessario per qualsiasi altra chiamata API, è ottibile contattando l'API `/auth/login`;
2. Le successive richieste dovranno includere un header personalizzato con key: `Authorization` e value: `Bearer <hash del Token>`.

```
Authorization: Bearer <hash del TOKEN>

Ex:
Authorization: Bearer eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxNzA2OTAiLCJpYXQiOjE3NDg1OTY1NDksImV4cCI6MTc0ODYwMDE0OSwicm9sZSI6ImFkbWluIn0.wFoSClyfi8DTFyedFmLuVm8Lrb4NrbRAzDLbaNiKNw479PzdMWGuJs_UkUOv_WoqB1ootWyk2BMr5D3gmm_RzdA172VH5JBkTb2L5qMCmPQkr2eHjTgNalc6yCbDw2BmV5xZ1rcYFeKo4aPB7xNwcTKUs-HHwLQ3tN2eYIaHe_L42iMuyDkAI8QvQLUahVhBG6SKwE3DrZ6SMYnHG6JCLfCqQzGqKRwglEZes8H-wXyKUzgHqTb5muRbzNT1OB0gIt46kXZacOLyOiSLwzaqGG1f3-GLGsW7Y8N3QE9n_aCBN1f7r4BbBIATtQ6aUB8t-u4mmRhj9vMx5hMvYycuUQ
```

## API Endpoints

### Super Admin

Nessun permesso specifico per ruolo Super Admin

### Admin+

| Metodo | Endpoint             | Input                                     | Output                                         | Descrizione                         |
|--------|----------------------|-------------------------------------------|------------------------------------------------|-------------------------------------|
| POST   | /users/init/superadmin    | username, email, password            | boolean                                 | Crea account Super Admin se assente |
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
| POST   | /auth/refresh-token         | Token JWT                               | Token JWT                                            | Rinnovo scadenza token                 |
| GET    | /users/profile         | Token JWT                               | IdUtente, username, email, nome, cognome, dataCreazione, ultimoLogin | Visualizza profilo utente  |
| PUT    | /users/profile         | email, nome, cognome                    | Id, username, email, nome, cognome                   | Modifica profilo utente                |
| POST   | /users/reset-password | email                                   | void                                                 | Reset password utente                  |
| PUT    | /users/change-password | currentPassword, newPassword            | boolean                                              | Modifica password                      |

