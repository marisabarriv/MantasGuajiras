package com.mantasguajiras.backend.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/** La foto de perfil se valida por su contenido real, no por lo que declare el navegador. */
class ImageSignatureTest {

    private static byte[] withPadding(int... header) {
        byte[] bytes = new byte[16];
        for (int i = 0; i < header.length; i++) {
            bytes[i] = (byte) header[i];
        }
        return bytes;
    }

    @Test
    void detectaJpeg() {
        assertThat(UserServiceImpl.detectImageType(withPadding(0xFF, 0xD8, 0xFF, 0xE0)))
                .isEqualTo("image/jpeg");
    }

    @Test
    void detectaPng() {
        assertThat(UserServiceImpl.detectImageType(
                withPadding(0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)))
                .isEqualTo("image/png");
    }

    @Test
    void detectaGif() {
        assertThat(UserServiceImpl.detectImageType(withPadding('G', 'I', 'F', '8', '9', 'a')))
                .isEqualTo("image/gif");
    }

    @Test
    void detectaWebp() {
        assertThat(UserServiceImpl.detectImageType(
                withPadding('R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P')))
                .isEqualTo("image/webp");
    }

    @Test
    void rechazaTextoDisfrazadoDeImagen() {
        byte[] script = "<script>alert('x')</script>".getBytes(StandardCharsets.UTF_8);

        assertThat(UserServiceImpl.detectImageType(script)).isNull();
    }

    @Test
    void rechazaArchivoDemasiadoCorto() {
        assertThat(UserServiceImpl.detectImageType(new byte[] { (byte) 0xFF, (byte) 0xD8 }))
                .isNull();
    }
}
