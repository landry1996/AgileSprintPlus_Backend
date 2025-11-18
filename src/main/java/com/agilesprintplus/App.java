package com.agilesprintplus;

import com.agilesprintplus.agilesprint.api.dto.UserDtos;
import com.agilesprintplus.agilesprint.domain.Role;
import com.agilesprintplus.agilesprint.domain.User;
import com.agilesprintplus.agilesprint.mapper.UserMapper;
import com.agilesprintplus.agilesprint.repo.UserRepository;
import com.agilesprintplus.notification.configs.EmailProperties;
import com.agilesprintplus.security.config.JwtProps;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@SpringBootApplication
@EnableConfigurationProperties({EmailProperties.class, JwtProps.class})
public class App {
    public static void main(String[] args){
        SpringApplication.run(App.class,args);
    }

//    @Bean
//    CommandLineRunner initAdmin(UserRepository userRepository,
//                                PasswordEncoder passwordEncoder) {
//        return args -> {
//            boolean adminExists = userRepository.findByUsernameIgnoreCase("pierre")
//                    .map(user -> user.getRoles().contains(Role.ADMIN))
//                    .orElse(false);
//
//            if (!adminExists) {
//                // Créer l'admin directement sans mapper
//                User adminUser = User.builder()
//                        .username("pierre")
//                        .email("landry73@gmail.com")
//                        .firstName("pierre")
//                        .lastName("landry")
//                        .passwordHash(passwordEncoder.encode("kamer237"))
//                        .roles(Set.of(Role.ADMIN))
//                        .enabled(true)
//                        .passwordChangeRequired(true)
//                        .build();
//
//                userRepository.save(adminUser);
//                System.out.println("✅ Utilisateur ADMIN créé avec succès");
//                System.out.println("👤 Username: pierre");
//                System.out.println("📧 Email: landry73@gmail.com");
//                System.out.println("🔑 Mot de passe temporaire: kamer237");
//                System.out.println("Encoded password: " + adminUser.getPasswordHash());
//            } else {
//                System.out.println("ℹ️  Un utilisateur ADMIN existe déjà");
//            }
//        };
//    }
}