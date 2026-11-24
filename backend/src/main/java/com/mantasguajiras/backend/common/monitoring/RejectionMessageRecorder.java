package com.mantasguajiras.backend.common.monitoring;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.mantasguajiras.backend.common.dto.ErrorResponse;

/**
 * Guarda el mensaje de cada respuesta de error (el mismo que ve el usuario,
 * por ejemplo "No hay suficiente inventario...") para que
 * {@link RequestLoggingFilter} lo escriba en el log junto a la operación.
 */
@RestControllerAdvice
public class RejectionMessageRecorder implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(
            MethodParameter returnType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        if (body instanceof ErrorResponse error
                && request instanceof ServletServerHttpRequest servletRequest) {
            servletRequest.getServletRequest()
                    .setAttribute(RequestLoggingFilter.REJECTION_ATTRIBUTE, error.getMessage());
        }

        return body;
    }
}
