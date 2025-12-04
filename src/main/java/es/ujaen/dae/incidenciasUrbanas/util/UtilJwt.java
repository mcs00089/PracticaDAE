package es.ujaen.dae.incidenciasUrbanas.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;


public class UtilJwt {

    private static final SecretKey claveFirmadoTokens = Jwts.SIG.HS256.key().build();

    /**
     * Genera un token Jwt guardando el usuario logueado
     * @param usuario el identificador del usuario, guardado como claim "subject"
     * @param claims información adicional asociada al usuario (p. ej. roles)
     * @param tiempoExpiracion el tiempo de expiración del token
     * @return el token codificado como cadena base64
     */
    public static String crearToken(String usuario, Map<String, ?> claims, int tiempoExpiracion) {
        var ahora = LocalDateTime.now().atZone(ZoneId.systemDefault());

        return Jwts.builder()
                .claims(claims)
                .subject(usuario)
                .expiration(Date.from(ahora.plusMinutes(tiempoExpiracion).toInstant()))
                .signWith(claveFirmadoTokens)
                .compact();
    }

    /**
     * Extrae la información del usuario guardada en el token Jwt
     * @throws JwtException si el token es inválido (corrupto o expirado)
     * @param token el token Jwt codificado en base64
     * @return las claims del token (subject y adicionales)
     */
    public static Claims extraerContenido(String token) {
        return Jwts.parser()
                .verifyWith(claveFirmadoTokens)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
