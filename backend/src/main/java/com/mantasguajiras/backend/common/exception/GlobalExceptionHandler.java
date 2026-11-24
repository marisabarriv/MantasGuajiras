package com.mantasguajiras.backend.common.exception;

import com.mantasguajiras.backend.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(
                        ResourceNotFoundException ex,
                        HttpServletRequest request) {

                ErrorResponse response = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.NOT_FOUND.value())
                                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(response);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidation(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

                String message = ex.getBindingResult()
                                .getFieldError()
                                .getDefaultMessage();

                ErrorResponse response = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                                .message(message)
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity
                                .badRequest()
                                .body(response);
        }

        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ErrorResponse> handleDuplicate(
                        DuplicateResourceException ex,
                        HttpServletRequest request) {

                ErrorResponse response = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.CONFLICT.value())
                                .error(HttpStatus.CONFLICT.getReasonPhrase())
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(response);
        }

        @ExceptionHandler(ValidationException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(
                        ValidationException ex,
                        HttpServletRequest request) {

                ErrorResponse response = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(response);
        }

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ErrorResponse> handleBusiness(
                        BusinessException ex,
                        HttpServletRequest request) {

                ErrorResponse response = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(response);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgument(
                        IllegalArgumentException ex,
                        HttpServletRequest request) {

                ErrorResponse response = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(response);
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentials(
                        BadCredentialsException ex,
                        HttpServletRequest request) {

                ErrorResponse response = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                                .message("Usuario o contraseña incorrectos.")
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(response);
        }

        /** Demasiados intentos fallidos de inicio de sesión (protección contra fuerza bruta). */
        @ExceptionHandler(TooManyLoginAttemptsException.class)
        public ResponseEntity<ErrorResponse> handleTooManyAttempts(
                        TooManyLoginAttemptsException ex,
                        HttpServletRequest request) {

                return build(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), request);
        }

        /** Cuenta inactiva o pendiente de aprobación (registro con aprobación). */
        @ExceptionHandler(org.springframework.security.authentication.DisabledException.class)
        public ResponseEntity<ErrorResponse> handleDisabled(
                        org.springframework.security.authentication.DisabledException ex,
                        HttpServletRequest request) {

                return build(HttpStatus.FORBIDDEN,
                                "Tu cuenta está inactiva o pendiente de aprobación. "
                                                + "Pide a un administrador que la active.",
                                request);
        }

        /**
         * JSON mal formado, incompleto o con un tipo de dato incorrecto
         * (por ejemplo, texto donde va un número). Antes caía en el
         * manejador general y respondía 500.
         */
        @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleNotReadable(
                        org.springframework.http.converter.HttpMessageNotReadableException ex,
                        HttpServletRequest request) {

                return build(HttpStatus.BAD_REQUEST,
                                "Los datos enviados no son válidos. Revisa el formulario e intenta de nuevo.",
                                request);
        }

        /**
         * Una restricción de la base de datos rechazó la operación (por
         * ejemplo, la misma venta sin conexión enviada dos veces al mismo
         * tiempo). Se responde 409 para que el dispositivo reintente: en el
         * reintento la venta ya existe y se devuelve sin duplicarla.
         */
        @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
        public ResponseEntity<ErrorResponse> handleDataIntegrity(
                        org.springframework.dao.DataIntegrityViolationException ex,
                        HttpServletRequest request) {

                return build(HttpStatus.CONFLICT,
                                "La operación choca con un registro existente. Actualiza la pantalla e intenta de nuevo.",
                                request);
        }

        /**
         * Un valor de la dirección con formato incorrecto: un identificador
         * mal escrito (/api/products/abc) o una fecha inválida en un filtro.
         * Antes respondía 500.
         */
        @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleTypeMismatch(
                        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex,
                        HttpServletRequest request) {

                return build(HttpStatus.BAD_REQUEST,
                                "El valor de \"" + ex.getName() + "\" no es válido.",
                                request);
        }

        /** Falta un parámetro obligatorio en la dirección. */
        @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
        public ResponseEntity<ErrorResponse> handleMissingParameter(
                        org.springframework.web.bind.MissingServletRequestParameterException ex,
                        HttpServletRequest request) {

                return build(HttpStatus.BAD_REQUEST,
                                "Falta el dato \"" + ex.getParameterName() + "\".",
                                request);
        }

        /** Método HTTP no permitido en esa dirección (por ejemplo, PATCH donde solo hay GET). */
        @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ErrorResponse> handleMethodNotSupported(
                        org.springframework.web.HttpRequestMethodNotSupportedException ex,
                        HttpServletRequest request) {

                return build(HttpStatus.METHOD_NOT_ALLOWED,
                                "Esta operación no está permitida en esta dirección.",
                                request);
        }

        /** Ruta que no existe (antes caía en el manejador general y respondía 500). */
        @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
        public ResponseEntity<ErrorResponse> handleNoResource(
                        org.springframework.web.servlet.resource.NoResourceFoundException ex,
                        HttpServletRequest request) {

                return build(HttpStatus.NOT_FOUND,
                                "La dirección solicitada no existe.",
                                request);
        }

        /** Archivo más grande de lo permitido (foto de perfil: 20 MB). */
        @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
        public ResponseEntity<ErrorResponse> handleTooLarge(
                        org.springframework.web.multipart.MaxUploadSizeExceededException ex,
                        HttpServletRequest request) {

                return build(HttpStatus.CONTENT_TOO_LARGE,
                                "La foto no puede pesar más de 20 MB.",
                                request);
        }

        private ResponseEntity<ErrorResponse> build(
                        HttpStatus status,
                        String message,
                        HttpServletRequest request) {

                return ResponseEntity.status(status).body(
                                ErrorResponse.builder()
                                                .timestamp(LocalDateTime.now())
                                                .status(status.value())
                                                .error(status.getReasonPhrase())
                                                .message(message)
                                                .path(request.getRequestURI())
                                                .build());
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGeneral(
                        Exception ex,
                        HttpServletRequest request) {

                // Error no previsto: se registra con un código corto que también
                // ve el usuario, para encontrarlo en el log. Al usuario no se le
                // muestran detalles internos.
                String errorCode = UUID.randomUUID().toString().substring(0, 8);

                log.error("Error interno [{}] en {} {}",
                                errorCode, request.getMethod(), request.getRequestURI(), ex);

                ErrorResponse response = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                                .message("Ha ocurrido un error interno. Código de referencia: " + errorCode + ".")
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(response);
        }
}