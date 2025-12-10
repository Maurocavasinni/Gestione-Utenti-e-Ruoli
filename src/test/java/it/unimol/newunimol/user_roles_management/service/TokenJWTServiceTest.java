package it.unimol.newunimol.user_roles_management.service;

import it.unimol.newunimol.user_roles_management.dto.TokenJWTDto;
import it.unimol.newunimol.user_roles_management.exceptions.TokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TokenJWTServiceTest {

    @InjectMocks
    private TokenJWTService tokenJWTService;

    private String testUserId;
    private String testUsername;
    private String testRole;

    @BeforeEach
    void setUp() {
        testUserId = "000001";
        testUsername = "testuser";
        testRole = "admin";

        // Set required properties for testing
        ReflectionTestUtils.setField(tokenJWTService, "privateKeyString", "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDVVtj2Q7e+XB9eMrVoB8kj2aTrh7eWwwmUO6WkYl4LteS9OXx5uYFL0WcPXIoPbum+VGzZYxQglcM7JyAB+2LtNAxlQoEGDmS01AeGldaJO1/9PiENr+whR5kYO2hfciquI4kiOyN+H9U/rUTKcVxZ0SIAgmFC3v3wKBACniplmADCzCJ3AqGJvfz9yliF4NBJacqnuP3CLK1zglWRh1ByZTAPqXUaczosRegWb3wrBhgjcGaot13m5cEfyobYL/FuGHI0quG5igZnwaZ25KbC2xHMKIDECWix/0zRckjJ2PK4KxKP7JLZWE6gVqD2EoRSAcEshe8Mk1pLPU78g4J9AgMBAAECggEAeXnjPo6eoJkKD3QmUV8li/FQ9AAqbIEQAkTYc7rDPD3NSmXiV9lmIwrQeTHDNzh7hjHCbSH6gvdj3FnR4u8GjHR3nmz41L77Xu/gfSTyrN+PL77hyU5j0StYpDCnpLl4TgHUAxbTheyQW5rIsdFMuaPYjZuv0AjzZX0aaLMxBvk1NOkD2SsEzIXkp/H6Zw/54wtZFBK0xOW3R6dUiG7nqU5IHYvnD/zzQ0DfT1sW9X4Re60keIrn6z7x5cebzpua9FnANaPzZKS91hDtPOwOooq7VTNWIyF8qpri4C0Q7MPhaEEDNXIXfn+c7YjjdktRgDQfIiGMe6En99IZhJi3TQKBgQDvhebek/RriBsqTpuxzapEUi0GECPdTz2xWmKEZvkAcxg5WiqYPyaYeXujTfexsc+x/61/dVFq5Sy+RyfvjaTt/NRQytCLZSzjFy6xvMr/u/jp85Pq9vAAyjz4f6ZraaE6Fv4pNFkC/vJ+u10BE6Ov48lWuAZzGmoueXrThHqRSwKBgQDkA9aWDLRQ3DIKfiC6yQSQZ3X7DwvrCB/nOh/PFRZnQ/NTgB3c60wPfl0swBwKixG7T2T5GBzkdI/8iWrldEU8LReQLLiVyCQ8kzqU3UUn2XP5yZfgLri/cm0fgnuySBXDJvIDmDmSKvSk6+XzHgyRBqtJYW3QqR+Tm02sHAEmVwKBgQDjnpQvoNr99XIbWmiLJ69PDejLgjsS7WLrT8GfoVuwGbBDkHQ6ColbDNd58XYZ36hIt3jhT1P4CaHjkStac3jw0PvTa2mTjqqBhF5Ted5P+QorCdbfy5t4pLgcTvKP0OnukjsmXSDZv/4igVt40nkThZyoVGmMqOWPFNKsf5Ea0QKBgBWaK31hzL+QbQlDc9RecRlBRBM6FLX6uhMNFbBn3gyAipARpkKk5DrfjPJNbRqunEpztixHVY1rYazaVA9TGbAe0YmuQvql0JEQnc82u3OQDYXPzJzHsGcq7x26HgABBlbL3MfsZx/rA+yQEOQcp7IhwJ6eJWpMa5pvb0dsC8vXAoGBAM6fgbtRqyzZBuywbAkq2Eu16JFcOnVvc9fRYLKZNKwV/ofoA2DDbMFg/PlzEJzl5tZm/cOx7G9GEkUdp/at3yjSCIiY3FhVxr+9n8TMerpj0dJGenjXLDRVLbMp97PHDSZEFPYAO9Qa5z8xnmpnK+5lwr/jx6987I6xLA5Xz0NI");
        ReflectionTestUtils.setField(tokenJWTService, "publicKeyString", "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1VbY9kO3vlwfXjK1aAfJI9mk64e3lsMJlDulpGJeC7XkvTl8ebmBS9FnD1yKD27pvlRs2WMUIJXDOycgAfti7TQMZUKBBg5ktNQHhpXWiTtf/T4hDa/sIUeZGDtoX3IqriOJIjsjfh/VP61EynFcWdEiAIJhQt798CgQAp4qZZgAwswidwKhib38/cpYheDQSWnKp7j9wiytc4JVkYdQcmUwD6l1GnM6LEXoFm98KwYYI3BmqLdd5uXBH8qG2C/xbhhyNKrhuYoGZ8GmduSmwtsRzCiAxAlosf9M0XJIydjyuCsSj+yS2VhOoFag9hKEUgHBLIXvDJNaSz1O/IOCfQIDAQAB");
        ReflectionTestUtils.setField(tokenJWTService, "jwtExpiration", 3600L);
    }

    @Test
    void testGenerateToken_Success() throws TokenException {
        TokenJWTDto result = tokenJWTService.generateToken(testUserId, testUsername, testRole);

        assertNotNull(result);
        assertNotNull(result.token());
    }

    @Test
    void testGenerateToken_NullUserId() {
        assertThrows(TokenException.class, 
            () -> tokenJWTService.generateToken(null, testUsername, testRole));
    }

    @Test
    void testGenerateToken_NullUsername() {
        assertThrows(TokenException.class, 
            () -> tokenJWTService.generateToken(testUserId, null, testRole));
    }

    @Test
    void testGenerateToken_NullRole() {
        assertThrows(TokenException.class, 
            () -> tokenJWTService.generateToken(testUserId, testUsername, null));
    }

    @Test
    void testIsTokenValid_ValidToken() throws TokenException {
        TokenJWTDto tokenDto = tokenJWTService.generateToken(testUserId, testUsername, testRole);
        
        boolean result = tokenJWTService.isTokenValid(tokenDto.token());

        assertTrue(result);
    }

    @Test
    void testExtractUserId_Success() throws TokenException {
        TokenJWTDto tokenDto = tokenJWTService.generateToken(testUserId, testUsername, testRole);
        
        String result = tokenJWTService.extractUserId(tokenDto.token());

        assertEquals(testUserId, result);
    }

    @Test
    void testExtractUsername_Success() throws TokenException {
        TokenJWTDto tokenDto = tokenJWTService.generateToken(testUserId, testUsername, testRole);
        
        String result = tokenJWTService.extractUsername(tokenDto.token());

        assertEquals(testUsername, result);
    }

    @Test
    void testExtractRole_Success() throws TokenException {
        TokenJWTDto tokenDto = tokenJWTService.generateToken(testUserId, testUsername, testRole);
        
        String result = tokenJWTService.extractRole(tokenDto.token());

        assertEquals(testRole, result);
    }

    @Test
    void testParseToken() {
        String token = "test.token.here";
        
        TokenJWTDto result = tokenJWTService.parseToken(token);

        assertNotNull(result);
        assertEquals(token, result.token());
    }

    @Test
    void testRefreshToken_Success() throws InterruptedException, TokenException {
        TokenJWTDto originalToken = tokenJWTService.generateToken(testUserId, testUsername, testRole);
        
        Thread.sleep(1000);

        TokenJWTDto result = tokenJWTService.refreshToken(originalToken.token());

        
        assertNotNull(result);
        assertNotNull(result.token());
        assertNotEquals(originalToken.token(), result.token());
    }

    @Test
    void testTokenLifecycle() throws InterruptedException, TokenException {
        // Generate token
        TokenJWTDto tokenDto = tokenJWTService.generateToken(testUserId, testUsername, testRole);
        assertNotNull(tokenDto);
        
        // Validate token
        assertTrue(tokenJWTService.isTokenValid(tokenDto.token()));
        
        // Extract information
        assertEquals(testUserId, tokenJWTService.extractUserId(tokenDto.token()));
        assertEquals(testUsername, tokenJWTService.extractUsername(tokenDto.token()));
        assertEquals(testRole, tokenJWTService.extractRole(tokenDto.token()));
        
        Thread.sleep(1000);

        // Refresh token
        TokenJWTDto refreshedToken = tokenJWTService.refreshToken(tokenDto.token());
        assertNotEquals(tokenDto.token(), refreshedToken.token());
    }
}
