package com.example.taichinhcanhan.config;

import com.example.taichinhcanhan.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

          @Bean
          public PasswordEncoder passwordEncoder() {
                    return new BCryptPasswordEncoder();
          }

          @Bean
          public AuthenticationProvider authenticationProvider(
                              CustomUserDetailsService userDetailsService,
                              PasswordEncoder passwordEncoder) {
                    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
                    provider.setUserDetailsService(userDetailsService);
                    provider.setPasswordEncoder(passwordEncoder);
                    return provider;
          }

          @Bean
          public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
                              throws Exception {
                    return configuration.getAuthenticationManager();
          }

          @Bean
          public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                    http
                                        .authorizeHttpRequests(authorize -> authorize
                                                            .requestMatchers("/login", "/register", "/forgot-password",
                                                                                "/css/**",
                                                                                "/js/**", "/images/**", "/error")
                                                            .permitAll()
                                                            .anyRequest()
                                                            .authenticated())
                                        .formLogin(form -> form
                                                            .loginPage("/login")
                                                            .loginProcessingUrl("/login")
                                                            .usernameParameter("login")
                                                            .passwordParameter("password")
                                                            .defaultSuccessUrl("/dashboard", true)
                                                            .failureUrl("/login?error=true")
                                                            .permitAll())
                                        .logout(logout -> logout
                                                            .logoutUrl("/logout")
                                                            .logoutSuccessUrl("/login?logout=true")
                                                            .invalidateHttpSession(true)
                                                            .clearAuthentication(true)
                                                            .permitAll())
                                        .sessionManagement(session -> session
                                                            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                                            .sessionFixation(sessionFixation -> sessionFixation
                                                                                .migrateSession()));

                    return http.build();
          }
}