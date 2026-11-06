package com.example.taichinhcanhan.config;

import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DefaultAccountInitializer {

          private static final Logger logger = LoggerFactory.getLogger(DefaultAccountInitializer.class);

          @Bean
          public CommandLineRunner createDefaultAccount(
                              UserAccountRepository userAccountRepository,
                              PasswordEncoder passwordEncoder,
                              @Value("${app.default-account.enabled:false}") boolean enabled,
                              @Value("${app.default-account.username:admin}") String username,
                              @Value("${app.default-account.email:admin@example.com}") String email,
                              @Value("${app.default-account.password:}") String password) {
                    return args -> {
                              if (!enabled) {
                                        return;
                              }
                              if (password.isBlank()) {
                                        throw new IllegalStateException(
                                                            "Default account is enabled but no password was provided.");
                              }
                              if (userAccountRepository.existsByUsername(username)
                                                  || userAccountRepository.existsByEmail(email)) {
                                        logger.info("Default account was not created because username or email already exists.");
                                        return;
                              }

                              UserAccount userAccount = new UserAccount();
                              userAccount.setUsername(username);
                              userAccount.setEmail(email);
                              userAccount.setFullName("Administrator");
                              userAccount.setPassword(passwordEncoder.encode(password));
                              userAccountRepository.save(userAccount);
                              logger.info("Default test account '{}' was created.", username);
                    };
          }
}