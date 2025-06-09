package it.unimol.newunimol.user_roles_management.service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import it.unimol.newunimol.user_roles_management.dto.TokenJWTDto;

@Service
public class TokenJWTService {
    @Value("${jwt.private-key}")
    private String privateKeyString;

    @Value("${jwt.public-key}")
    private String publicKeyString;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    private static final Set<String> invalidatedTokens = ConcurrentHashMap.newKeySet();

    /**
     * Decifra e restituisce la chiave privata per la firma dei token JWT.
     * 
     * @return La chiave privata come oggetto PrivateKey.
     * @throws RuntimeException Se la chiave privata non è in formato Base64 valido o se si verifica un errore durante la generazione della chiave.
     */
    private PrivateKey getPrivateKey() {
        if (this.privateKey == null) {
            try {
                byte[] keyBytes = Base64.getDecoder().decode(this.privateKeyString);
                PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                this.privateKey = keyFactory.generatePrivate(spec);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Chiave privata non è in formato Base64 valido", e);
            } catch (Exception e) {
                throw new RuntimeException("Errore prv_key, controlla application.properties", e);
            }
        }
        return privateKey;
    }

    /**
     * Decifra e restituisce la chiave pubblica per la verifica dei token JWT.
     * 
     * @return La chiave pubblica come oggetto PublicKey.
     * @throws RuntimeException Se la chiave pubblica non è in formato Base64 valido o se si verifica un errore durante la generazione della chiave.
     */
    private PublicKey getPublicKey() {
        if (this.publicKey == null) {
            try {
                byte[] keyBytes = Base64.getDecoder().decode(this.publicKeyString);
                X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                this.publicKey = keyFactory.generatePublic(spec);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Chiave pubblica non è in formato Base64 valido", e);
            } catch (Exception e) {
                throw new RuntimeException("Errore pub_key, controlla application.properties", e);
            }
        }
        return publicKey;
    }

    public <T> T extractClaim (String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Estrae tutti i claims dal token JWT.
     * 
     * @param token Il token JWT da cui estrarre i claims.
     * @return Un oggetto Claims contenente tutti i claims del token.
     * @throws RuntimeException Se il token non è valido o se si verifica un errore durante l'estrazione dei claims.
     */
    private Claims extractAllClaims (String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getPublicKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    /**
     * Estrae l'ID utente dal token JWT.
     * 
     * @param token Il token JWT da cui estrarre l'ID utente.
     * @return L'ID utente come stringa.
     */
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Estrae il nome utente dal token JWT.
     * 
     * @param token Il token JWT da cui estrarre il nome utente.
     * @return Il nome utente come stringa.
     */
    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.get("username", String.class));
    }
    
    /**
     * Estrae il ruolo dell'utente dal token JWT.
     * 
     * @param token Il token JWT da cui estrarre il ruolo.
     * @return Il ruolo come stringa.
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Estrae la data di scadenza dal token JWT.
     * 
     * @param token Il token JWT da cui estrarre la data di scadenza.
     * @return La data di scadenza come oggetto Date.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Verifica se il token JWT è scaduto.
     * 
     * @param token Il token JWT da verificare.
     * @return true se il token è scaduto, false altrimenti.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Verifica se il token JWT è valido.
     * 
     * @param token Il token JWT da verificare.
     * @return true se il token è valido, false altrimenti.
     */    
    public boolean isTokenValid (String token) {
        return !isTokenExpired(token) && !invalidatedTokens.contains(token);
    }

    /**
     * Genera un nuovo token JWT con i dati dell'utente.
     * 
     * @param userId L'ID dell'utente.
     * @param username Il nome utente.
     * @param role Il ruolo dell'utente.
     * @return Un oggetto TokenJWTDto contenente il token JWT generato.
     */
    public TokenJWTDto generateToken (String userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        
        long now = System.currentTimeMillis();
        long expiration = now + (this.jwtExpiration * 1000);
        
        String token = Jwts.builder()
            .setClaims(claims)
            .setSubject(userId)
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(expiration))
            .claim("username", username)
            .claim("role", role)
            .signWith(getPrivateKey(), SignatureAlgorithm.RS256)
            .compact();

        return new TokenJWTDto(token);
    }

    /**
     * Analizza un token JWT e restituisce un oggetto TokenJWTDto.
     * 
     * @param token Il token JWT da analizzare.
     * @return Un oggetto TokenJWTDto contenente il token JWT.
     */
    public TokenJWTDto parseToken(String token) {
        return new TokenJWTDto(token);
    }

    /**
     * Invalida un token aggiungendolo all'insieme di token invalidati.
     * 
     * @param token Il token da invalidare.
     */
    public void invalidateToken(String token) {
        invalidatedTokens.add(token);
    }

    /**
     * Effettua il refresh di un token JWT, generando un nuovo token con gli stessi dati dell'utente.
     * 
     * @param token Il token JWT da aggiornare.
     * @return Un nuovo oggetto TokenJWTDto contenente il token JWT aggiornato.
     * @throws RuntimeException Se il token è già stato invalidato.
     */
    public TokenJWTDto refreshToken(String token) throws RuntimeException {
        if (invalidatedTokens.contains(token)) {
            throw new RuntimeException("Token già invalidato, non è possibile effettuare il refresh");
        }

        Claims claims = extractAllClaims(token);
        String userId = claims.getSubject();
        String username = claims.get("username", String.class);
        String role = claims.get("role", String.class);

        invalidateToken(token);

        return generateToken(userId, username, role);
    }
}
