package helpdesk.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import helpdesk.api.user.entity.User;
import helpdesk.api.user.repository.UserRepository;
import helpdesk.api.user.entity.UserRole;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class DefaultUserSeederTests {

    @Autowired
    private DefaultUserSeeder defaultUserSeeder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createsDefaultTestUsersWithEncodedPasswords() {
        defaultUserSeeder.run(null);

        User admin = userRepository.findByEmail("admin@helpdesk.local").orElseThrow();
        User requester = userRepository.findByEmail("user@helpdesk.local").orElseThrow();

        assertThat(admin.getName()).isEqualTo("Administrador");
        assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(admin.getPasswordHash()).isNotEqualTo("admin123");
        assertThat(passwordEncoder.matches("admin123", admin.getPasswordHash())).isTrue();

        assertThat(requester.getName()).isEqualTo("Usuario Teste");
        assertThat(requester.getRole()).isEqualTo(UserRole.SOLICITANTE);
        assertThat(requester.getPasswordHash()).isNotEqualTo("user123");
        assertThat(passwordEncoder.matches("user123", requester.getPasswordHash())).isTrue();
    }

    @Test
    void doesNotDuplicateDefaultUsersWhenRunAgain() {
        defaultUserSeeder.run(null);

        long usersBefore = countDefaultUsers();
        defaultUserSeeder.run(null);

        assertThat(countDefaultUsers()).isEqualTo(usersBefore);
    }

    private long countDefaultUsers() {
        List<String> defaultEmails = List.of("admin@helpdesk.local", "user@helpdesk.local");

        return userRepository.findAll().stream()
                .filter(user -> defaultEmails.contains(user.getEmail()))
                .count();
    }
}
