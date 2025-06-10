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

## Porta di Default

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
Il servizio Gestione Utenti e Ruoli pubblica le modifiche al database tramite 4 routing key specifiche:
```
Exchange: users.exchange (Topic)
├── user.created   → Notifica creazione utente
├── user.updated   → Notifica aggiornamento utente  
├── user.deleted   → Notifica eliminazione utente
└── role.assigned  → Notifica assegnazione ruolo
```

### Esempi di Messaggi

I campi nei messaggi Rabbit corrispondono esattamente ai Dto usati nel sistema.
Ogni messaggio utilizza il routing key a sè omonimo, eccetto **PROFILE_UPDATED** che sfrutta il routing key di `user.updated`.

#### USER_CREATED
```json
{
  "eventType": "USER_CREATED",
  "userId": "123456",
  "username": "m.cavasinni",
  "email": "m.cavasinni@unimol.it",
  "name": "Mauro",
  "surname": "Cavasinni",
  "roleId": "student",
  "roleName": "STUDENTE",
  "timestamp": 1716470423000
}
```

#### USER_UPDATED
```json
{
  "eventType": "USER_UPDATED",
  "userId": "123456",
  "username": "m.casavinni",
  "email": "m.casavinni@unimol.it",
  "name": "Mauro",
  "surname": "Casavinni",
  "roleId": "teacher",
  "roleName": "DOCENTE",
  "timestamp": 1716470523000
}
```

#### USER_DELETED
```json
{
  "eventType": "USER_DELETED",
  "userId": "123456",
  "timestamp": 1716470623000
}
```

#### ROLE_ASSIGNED
```json
{
  "eventType": "ROLE_ASSIGNED",
  "userId": "123456",
  "roleId": "teacher",
  "timestamp": 1716470723000
}
```

#### PROFILE_UPDATED
```json
{
  "eventType": "PROFILE_UPDATED",
  "userId": "123456",
  "username": "m.cavasinni",
  "email": "m.cavasinni@unimol.it",
  "name": "Mauro",
  "surname": "Cavasinni",
  "timestamp": 1716470823000
}
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

| Metodo | Endpoint | Input | Output | Descrizione |
|--------|----------|-------|--------|-------------|
| POST | `/api/v1/users/init/superadmin` | **Body:** `{"username": "string", "email": "string", "name": "string", "surname": "string", "password": "string", "role": "string"}` | `{"id": "string", "username": "string", "email": "string", "name": "string", "surname": "string", "creationDate": 0, "lastLogin": 0 "ruolo": {...}}` | Crea account Super Admin se assente |
| POST | `/api/v1/users` | **Header:** Token JWT<br>**Body:** `{"username": "string", "email": "string", "name": "string", "surname": "string", "password": "string", "role": "string"}` | `{"id": "string", "username": "string", "email": "string", "name": "string", "surname": "string", "creationDate": 0, "lastLogin": 0 "ruolo": {...}}` | Creazione nuovo utente |
| GET | `/api/v1/users` | **Header:** Token JWT | `[{"id": "string", "username": "string", "email": "string", "nome": "string", "cognome": "string", "dataCreazione": 0, "ultimoLogin": 0}]` | Lista tutti gli utenti |
| GET | `/api/v1/users/{id}` | **Header:** Token JWT<br>**PathVariable:** id | `{"id": "string", "username": "string", "email": "string", "name": "string", "surname": "string", "creationDate": 0, "lastLogin": 0 "ruolo": {...}}` | Visualizzazione utente |
| PUT | `/api/v1/users/{id}` | **Header:** Token JWT<br>**PathVariable:** id<br>**Body:** `{"id": "string", "username": "string", "email": "string", "name": "string", "surname": "string", "creationDate": 0, "lastLogin": 0 "ruolo": {...}}` | `{"id": "string", "username": "string", "email": "string", "name": "string", "surname": "string", "creationDate": 0, "lastLogin": 0 "ruolo": {...}}` | Modifica utente |
| DELETE | `/api/v1/users/{id}` | **Header:** Token JWT<br>**PathVariable:** id | boolean | Eliminazione utente |
| GET | `/api/v1/roles` | **Header:** Token JWT | `[{"id": "string", "nome": "string", "descrizione": "string"}]` | Lista ruoli disponibili |
| POST | `/api/v1/users/{id}/roles` | **Header:** Token JWT<br>**PathVariable:** id<br>**Body:** `{"roleId": "string"}` | boolean | Assegna ruolo a utente |
| PUT | `/api/v1/users/{id}/roles` | **Header:** Token JWT<br>**PathVariable:** id<br>**Body:** `{"roleId": "string"}` | boolean | Aggiorna ruoli utente |

### Docenti+

Nessun permesso specifico per ruolo Docente

### Permessi Generici

| Metodo | Endpoint | Input | Output | Descrizione |
|--------|----------|-------|--------|-------------|
| POST | `/api/v1/auth/login` | **Body:** `{"username": "string", "password": "string"}` | `{"token": "string"}` | Login utente |
| POST | `/api/v1/auth/logout` | **Header:** Token JWT | void | Logout utente |
| POST | `/api/v1/auth/refresh-token` | **Header:** Token JWT | `{"token": "string"}` | Rinnovo scadenza token |
| GET | `/api/v1/users/profile` | **Header:** Token JWT | `{"id": "string", "username": "string", "email": "string", "nome": "string", "cognome": "string", "nomeRuolo": "string", "dataCreazione": 0, "ultimoLogin": 0}` | Visualizza profilo utente |
| PUT | `/api/v1/users/profile` | **Header:** Token JWT<br>**Body:** `{"username": "string", "name": "string", "surname": "string"}` | `{"id": "string", "username": "string", "email": "string", "nome": "string", "cognome": "string", "nomeRuolo": "string", "dataCreazione": 0, "ultimoLogin": 0}` | Modifica profilo utente |
| POST | `/api/v1/users/reset-password` | **Header:** Token JWT<br>**Body:** `{"oldPassword": "string", "newPassword": <campo ignorato>}` | void | Reset password utente |
| PUT | `/api/v1/users/change-password` | **Header:** Token JWT<br>**Body:** `{"oldPassword": "string", "newPassword": "string"}` | boolean | Modifica password |
