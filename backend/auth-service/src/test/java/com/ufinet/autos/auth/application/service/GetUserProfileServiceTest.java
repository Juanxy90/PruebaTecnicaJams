package com.ufinet.autos.auth.application.service;

import com.ufinet.autos.auth.application.port.out.UserRepositoryPort;
import com.ufinet.autos.auth.domain.exception.UserNotFoundException;
import com.ufinet.autos.auth.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserProfileServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private GetUserProfileService service;

    @Test
    void returnsUserWhenExists() {
        User user = User.builder().id(1L).email("demo@ufinet.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThat(service.getById(1L)).isEqualTo(user);
    }

    @Test
    void throwsWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L)).isInstanceOf(UserNotFoundException.class);
    }
}
