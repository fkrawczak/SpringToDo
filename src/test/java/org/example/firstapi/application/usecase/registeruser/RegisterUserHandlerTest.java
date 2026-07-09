package org.example.firstapi.application.usecase.registeruser;

import org.example.firstapi.application.exceptions.EmailAlreadyTakenException;
import org.example.firstapi.domain.model.user.User;
import org.example.firstapi.domain.model.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUserHandler handler;

    @Test
    void handleCreatesUserWithNormalizedEmailTrimmedNamesAndEncodedPassword() {
        // given
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("raw-password")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterUserCommand command = new RegisterUserCommand(
                " USER@EXAMPLE.COM ",
                " Jane ",
                " Doe ",
                "raw-password"
        );

        // when
        UUID userId = handler.handle(command);

        // then
        assertThat(userId).isNotNull();
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("user@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("hashed-password");
        assertThat(savedUser.getFirstName()).isEqualTo("Jane");
        assertThat(savedUser.getLastName()).isEqualTo("Doe");
    }

    @Test
    void handleThrowsEmailAlreadyTakenWhenEmailExists() {
        // given
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        // when + then
        assertThatThrownBy(() -> handler.handle(new RegisterUserCommand(
                "TAKEN@example.com",
                "Jane",
                "Doe",
                "raw-password"
        )))
                .isInstanceOf(EmailAlreadyTakenException.class)
                .hasMessage("Email is already taken: taken@example.com");

        // then
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void handleMapsDatabaseUniqueConstraintViolationToEmailAlreadyTaken() {
        // given
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(false);
        when(passwordEncoder.encode("raw-password")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        // when + then
        assertThatThrownBy(() -> handler.handle(new RegisterUserCommand(
                "taken@example.com",
                "Jane",
                "Doe",
                "raw-password"
        )))
                .isInstanceOf(EmailAlreadyTakenException.class)
                .hasMessage("Email is already taken: taken@example.com");
    }
}
