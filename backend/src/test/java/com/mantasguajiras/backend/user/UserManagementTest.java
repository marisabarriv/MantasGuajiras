package com.mantasguajiras.backend.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mantasguajiras.backend.common.exception.BusinessException;
import com.mantasguajiras.backend.common.exception.DuplicateResourceException;
import com.mantasguajiras.backend.support.IntegrationTestBase;
import com.mantasguajiras.backend.user.dto.requests.ChangePasswordRequest;
import com.mantasguajiras.backend.user.dto.requests.ResetPasswordRequest;
import com.mantasguajiras.backend.user.dto.requests.UpdateUserRequest;
import com.mantasguajiras.backend.user.dto.requests.UserRequest;
import com.mantasguajiras.backend.user.dto.response.UserResponse;
import com.mantasguajiras.backend.user.entity.Role;
import com.mantasguajiras.backend.user.service.UserService;

/** Administración de usuarios, contraseñas y foto de perfil (SEG-*, IMG-*). */
class UserManagementTest extends IntegrationTestBase {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 13, 'I', 'H', 'D', 'R'};

    @Autowired private UserService userService;
    @Autowired private PasswordEncoder passwordEncoder;

    private UserResponse create(Role role) {
        UserRequest request = new UserRequest();
        request.setUsername("m" + randomDigits(8));
        request.setPhone("35" + randomDigits(8));
        request.setPassword("Clave123");
        request.setRole(role);
        request.setActive(true);
        return userService.create(request);
    }

    private static UpdateUserRequest update(UserResponse user, Role role, boolean active) {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername(user.getUsername());
        request.setPhone(user.getPhone());
        request.setRole(role);
        request.setActive(active);
        return request;
    }

    @Test
    void crearConsultarYNoDuplicarUsuarioNiTelefono() {
        UserResponse seller = create(Role.SELLER);

        assertThat(userService.findById(seller.getId()).getRole()).isEqualTo(Role.SELLER);
        assertThat(userService.findAll()).anyMatch(user -> user.getId().equals(seller.getId()));

        UserRequest sameName = new UserRequest();
        sameName.setUsername(seller.getUsername()); sameName.setPhone("36" + randomDigits(8));
        sameName.setPassword("Clave123"); sameName.setActive(true);
        assertThatThrownBy(() -> userService.create(sameName)).isInstanceOf(DuplicateResourceException.class);

        UserRequest samePhone = new UserRequest();
        samePhone.setUsername("n" + randomDigits(8)); samePhone.setPhone(seller.getPhone());
        samePhone.setPassword("Clave123"); samePhone.setActive(true);
        assertThatThrownBy(() -> userService.create(samePhone)).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void unAdministradorNoCambiaSuPropioRolNiSeDesactivaASiMismo() {
        UserResponse admin = create(Role.ADMIN);

        assertThatThrownBy(() -> userService.update(admin.getId(), update(admin, Role.SELLER, true), admin.getUsername()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("propio rol");
        assertThatThrownBy(() -> userService.update(admin.getId(), update(admin, Role.ADMIN, false), admin.getUsername()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("propia cuenta");
    }

    @Test
    void otroAdministradorPuedeCambiarRolDesactivarYPromover() {
        UserResponse seller = create(Role.SELLER);

        UserResponse inactive = userService.update(seller.getId(), update(seller, Role.SELLER, false), ADMIN);
        assertThat(inactive.getActive()).isFalse();

        UserResponse promoted = userService.promoteToAdmin(seller.getId());
        assertThat(promoted.getRole()).isEqualTo(Role.ADMIN);
        assertThatThrownBy(() -> userService.promoteToAdmin(seller.getId()))
                .hasMessageContaining("ya es administrador");
    }

    @Test
    void losUsuariosNoSeEliminan() {
        UserResponse seller = create(Role.SELLER);
        assertThatThrownBy(() -> userService.delete(seller.getId()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("no se eliminan");
    }

    @Test
    void restablecerYCambiarLaPropiaContrasena() {
        UserResponse seller = create(Role.SELLER);

        ResetPasswordRequest reset = new ResetPasswordRequest();
        reset.setNewPassword("Nueva456");
        userService.resetPassword(seller.getId(), reset);
        assertThat(passwordEncoder.matches("Nueva456",
                userRepository.findById(seller.getId()).orElseThrow().getPassword())).isTrue();

        ChangePasswordRequest wrongCurrent = new ChangePasswordRequest();
        wrongCurrent.setCurrentPassword("Mala999"); wrongCurrent.setNewPassword("Otra789");
        assertThatThrownBy(() -> userService.changeOwnPassword(seller.getUsername(), wrongCurrent))
                .hasMessageContaining("actual es incorrecta");

        ChangePasswordRequest same = new ChangePasswordRequest();
        same.setCurrentPassword("Nueva456"); same.setNewPassword("Nueva456");
        assertThatThrownBy(() -> userService.changeOwnPassword(seller.getUsername(), same))
                .hasMessageContaining("distinta");

        ChangePasswordRequest ok = new ChangePasswordRequest();
        ok.setCurrentPassword("Nueva456"); ok.setNewPassword("Otra789");
        userService.changeOwnPassword(seller.getUsername(), ok);
        assertThat(passwordEncoder.matches("Otra789",
                userRepository.findById(seller.getId()).orElseThrow().getPassword())).isTrue();

        assertThat(userService.getMe(seller.getUsername()).getUsername()).isEqualTo(seller.getUsername());
    }

    @Test
    void fotoDePerfilSeValidaGuardaConSuTipoRealYSeQuita() {
        UserResponse seller = create(Role.SELLER);
        String username = seller.getUsername();

        // Declara JPEG pero es PNG: se guarda como PNG (el tipo real).
        userService.updateOwnPhoto(username, new MockMultipartFile("file", "foto.jpg", "image/jpeg", PNG));
        assertThat(userService.getOwnPhoto(username).getContentType()).isEqualTo("image/png");
        assertThat(userService.getMe(username).getHasPhoto()).isTrue();

        assertThatThrownBy(() -> userService.updateOwnPhoto(username,
                new MockMultipartFile("file", "virus.png", "image/png", "<script>x</script>   ".getBytes())))
                .hasMessageContaining("no es una imagen");
        assertThatThrownBy(() -> userService.updateOwnPhoto(username,
                new MockMultipartFile("file", "doc.pdf", "application/pdf", PNG)))
                .hasMessageContaining("JPG, PNG, WEBP o GIF");
        assertThatThrownBy(() -> userService.updateOwnPhoto(username,
                new MockMultipartFile("file", "vacia.png", "image/png", new byte[0])))
                .hasMessageContaining("Elige una foto");
        byte[] tooBig = new byte[21 * 1024 * 1024];
        System.arraycopy(PNG, 0, tooBig, 0, PNG.length);
        assertThatThrownBy(() -> userService.updateOwnPhoto(username,
                new MockMultipartFile("file", "grande.png", "image/png", tooBig)))
                .hasMessageContaining("20 MB");

        userService.deleteOwnPhoto(username);
        assertThat(userService.getMe(username).getHasPhoto()).isFalse();
    }
}
