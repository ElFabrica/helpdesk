package helpdesk.api.config;

import helpdesk.api.user.User;
import helpdesk.api.user.UserRepository;
import helpdesk.api.user.UserRole;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DefaultUserSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createIfMissing("Administrador", "admin@helpdesk.local", "admin123", UserRole.ADMIN);
        createIfMissing("Usuario Teste", "user@helpdesk.local", "user123", UserRole.SOLICITANTE);
    }

    private void createIfMissing(String name, String email, String password, UserRole role) {
        if (userRepository.existsByEmail(email)) {
            return;
        }

        userRepository.save(new User(name, email, passwordEncoder.encode(password), role));
    }
}
