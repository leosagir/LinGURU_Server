package lingvo.app.config;

import lingvo.app.auth.entity.Role;
import lingvo.app.auth.entity.RoleType;
import lingvo.app.auth.entity.User;
import lingvo.app.auth.repository.RoleRepository;
import lingvo.app.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashSet;

@Configuration
public class DatabaseInitializer {

    @Value("${app.super-admin.username}")
    private String superAdminUsername;

    @Value("${app.super-admin.email}")
    private String superAdminEmail;

    @Value("${app.super-admin.password}")
    private String superAdminPassword;

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository,
                                          RoleRepository roleRepository,
                                          PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                // Создаем все роли, если они еще не существуют
                for (RoleType roleType : RoleType.values()) {
                    if (!roleRepository.existsByName(roleType)) {
                        Role role = new Role();
                        role.setName(roleType);
                        roleRepository.save(role);
                    }
                }

                // Получаем роль SUPER_ADMINISTRATOR
                Role superAdminRole = roleRepository.findByName(RoleType.SUPER_ADMINISTRATOR)
                        .orElseThrow(() -> new RuntimeException("Роль SUPER_ADMINISTRATOR не найдена"));

                // Создаем суперадмина
                User superAdmin = new User();
                superAdmin.setUsername(superAdminUsername);
                superAdmin.setEmail(superAdminEmail);
                superAdmin.setPassword(passwordEncoder.encode(superAdminPassword));
                superAdmin.setRoles(new HashSet<>(Collections.singletonList(superAdminRole)));

                userRepository.save(superAdmin);

                System.out.println("Суперадмин создан успешно");
            } else {
                System.out.println("База данных уже инициализирована");
            }
        };
    }
}
