package com.pbl7.identity_service.configuration;


import com.pbl7.identity_service.constant.PredefinedRole;
import com.pbl7.identity_service.dto.request.CreateCartRequest;
import com.pbl7.identity_service.entity.Role;
import com.pbl7.identity_service.entity.User;
import com.pbl7.identity_service.repository.RoleRepository;
import com.pbl7.identity_service.repository.UserRepository;
import com.pbl7.identity_service.repository.httpclient.CartClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;
    CartClient cartClient;

    @NonFinal
    static final String ADMIN_USER_NAME = "admin";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @NonFinal
    static final String ADMIN_EMAIL = "admin@gmail.com";

    @Bean
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driverClassName",
            havingValue = "com.mysql.cj.jdbc.Driver")
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        log.info("Initializing application.....");
        return args -> {
            if (userRepository.findByUsername(ADMIN_USER_NAME).isEmpty()) {
                roleRepository.save(Role.builder()
                        .name(PredefinedRole.USER_ROLE)
                        .description("User role")
                        .build());

                Role adminRole = roleRepository.save(Role.builder()
                        .name(PredefinedRole.ADMIN_ROLE)
                        .description("Admin role")
                        .build());

                var roles = new HashSet<Role>();
                roles.add(adminRole);

                User user = User.builder()
                        .username(ADMIN_USER_NAME)
                        .emailVerified(true)
                        .isEnabled(true)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .email(ADMIN_EMAIL)
                        .roles(roles)
                        .build();

                CreateCartRequest request = new CreateCartRequest();
                request.setUserId(user.getId());

                cartClient.createCart(request);

                userRepository.save(user);
                log.warn("admin user has been created with default password: admin, please change it");
            }
            log.info("Application initialization completed .....");
        };
    }
}
