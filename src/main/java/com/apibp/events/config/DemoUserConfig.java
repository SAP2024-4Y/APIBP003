package com.apibp.events.config;

import com.apibp.events.model.AppUser;
import com.apibp.events.repository.AppUserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoUserConfig {

    @Bean
    ApplicationRunner seedDemoUser(AppUserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (users.findByUsername("demo").isEmpty()) {
                AppUser u = new AppUser();
                u.setUsername("demo");
                u.setPassword(encoder.encode("demo-password"));
                u.setRole("USER");
                users.save(u);
            }
        };
    }
}
