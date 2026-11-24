package com.mantasguajiras.backend.user;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mantasguajiras.backend.common.exception.BusinessException;
import com.mantasguajiras.backend.user.dto.requests.UpdateUserRequest;
import com.mantasguajiras.backend.user.entity.Role;
import com.mantasguajiras.backend.user.entity.User;
import com.mantasguajiras.backend.user.mapper.UserMapper;
import com.mantasguajiras.backend.user.repository.UserProfileImageRepository;
import com.mantasguajiras.backend.user.repository.UserRepository;
import com.mantasguajiras.backend.user.service.impl.UserServiceImpl;

/**
 * Regla: el sistema nunca se queda sin un administrador activo. Se prueba
 * aislada (sin base de datos) porque la base de pruebas tiene otros
 * administradores creados por otras pruebas.
 */
class LastAdminRuleTest {

    @Test
    void noSePuedeDesactivarNiQuitarleElRolAlUltimoAdministradorActivo() {
        UserRepository repository = mock(UserRepository.class);
        User onlyAdmin = User.builder().username("unico").phone("3000000000")
                .role(Role.ADMIN).active(true).build();
        onlyAdmin.setId(UUID.randomUUID());

        when(repository.findById(onlyAdmin.getId())).thenReturn(Optional.of(onlyAdmin));
        when(repository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(repository.findByPhone(anyString())).thenReturn(Optional.empty());
        when(repository.findAll()).thenReturn(List.of(onlyAdmin));

        UserServiceImpl service = new UserServiceImpl(repository, mock(UserProfileImageRepository.class),
                mock(UserMapper.class), mock(PasswordEncoder.class));

        UpdateUserRequest demote = new UpdateUserRequest();
        demote.setUsername("unico"); demote.setPhone("3000000000");
        demote.setRole(Role.SELLER); demote.setActive(true);
        assertThatThrownBy(() -> service.update(onlyAdmin.getId(), demote, "otro"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("al menos un administrador");

        UpdateUserRequest deactivate = new UpdateUserRequest();
        deactivate.setUsername("unico"); deactivate.setPhone("3000000000");
        deactivate.setRole(Role.ADMIN); deactivate.setActive(false);
        assertThatThrownBy(() -> service.update(onlyAdmin.getId(), deactivate, "otro"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("al menos un administrador");
    }
}
