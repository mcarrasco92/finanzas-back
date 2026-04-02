package com.finanzas.app_back.interceptor;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.crypto.spec.SecretKeySpec;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Key;
import java.util.Base64;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final Key jwtKey = new SecretKeySpec(
            Base64.getDecoder().decode("dAHNfXl8x/3oc5zEdUqy+oxUZgthsM13wzhv/WSVWew="),
            SignatureAlgorithm.HS256.getJcaName());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
    
        // Excluir rutas públicas
        if (path.startsWith("/api/users/register") || 
            path.startsWith("/api/users/create-token") || 
            path.startsWith("/api/users/validate-token")) {
            return true; // Permitir estas rutas sin validación
        }
    
        String token = request.getHeader("Authorization");
    
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("{\"coderr\":\"1001\",\"message\":\"Token inválido o no proporcionado.\"}");
            return false; // Detener la ejecución
        }
    
        try {
            String jwt = token.substring(7); // Eliminar el prefijo "Bearer "
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtKey)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
    
            String uid = claims.get("uid", String.class);

            if (uid == null || uid.isEmpty()) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("{\"coderr\":\"1001\",\"message\":\"Token inválido.\"}");
                return false; // Detener la ejecución
            }

            request.setAttribute("uid", uid);
            request.setAttribute("email", claims.get("email", String.class));
    
        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("{\"coderr\":\"1001\",\"message\":\"Token inválido.\"}");
            return false; // Detener la ejecución
        }
    
        return true; // Continuar con la ejecución
    }
}
