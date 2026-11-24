package com.mantasguajiras.backend.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import com.mantasguajiras.backend.common.dto.ErrorResponse;

/** Un error no previsto queda en el log y el usuario recibe un código para encontrarlo. */
@ExtendWith(OutputCaptureExtension.class)
class UnexpectedErrorLoggingTest {

    @Test
    void errorInesperadoSeRegistraConCodigoSinExponerDetalles(CapturedOutput output) {

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/sales");

        ResponseEntity<ErrorResponse> response = new GlobalExceptionHandler()
                .handleGeneral(new IllegalStateException("detalle interno secreto"), request);

        String message = response.getBody().getMessage();
        String code = message.replaceAll(".*Código de referencia: ([0-9a-f]{8})\\..*", "$1");

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(message).doesNotContain("secreto");
        assertThat(code).hasSize(8);
        assertThat(output).contains("Error interno [" + code + "] en POST /api/sales");
        assertThat(output).contains("detalle interno secreto");
    }
}
