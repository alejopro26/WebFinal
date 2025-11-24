package chatnexus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handle(RuntimeException ex) {
        String msg = ex.getMessage() == null ? "Error" : ex.getMessage();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (msg.contains("No autorizado")) status = HttpStatus.FORBIDDEN;
        else if (msg.contains("Contraseña incorrecta")) status = HttpStatus.FORBIDDEN;
        else if (msg.contains("no encontrado") || msg.contains("no encontrada")) status = HttpStatus.NOT_FOUND;
        else if (msg.contains("Token inválido")) status = HttpStatus.UNAUTHORIZED;
        else if (msg.contains("No se envió token")) status = HttpStatus.UNAUTHORIZED;
        else if (msg.contains("Demasiadas solicitudes")) status = HttpStatus.TOO_MANY_REQUESTS;
        return ResponseEntity.status(status).body(Map.of("error", msg));
    }
}