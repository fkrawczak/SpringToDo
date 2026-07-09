package org.example.firstapi.application.usecase.registeruser;

import org.example.firstapi.application.exceptions.EmailAlreadyTakenException;
import org.example.firstapi.application.core.EmailNormalizer;
import org.example.firstapi.domain.model.user.User;
import org.example.firstapi.domain.model.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RegisterUserHandler {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserHandler(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UUID handle(RegisterUserCommand command) {
        String email = EmailNormalizer.normalize(command.email());

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyTakenException(email);
        }

        String hashedPassword = passwordEncoder.encode(command.password());
        User user = new User(email, hashedPassword, command.firstName().trim(), command.lastName().trim());

        try {
            return userRepository.save(user).getId();
        } catch (DataIntegrityViolationException exception) {
            throw new EmailAlreadyTakenException(email);
        }
    }
}
