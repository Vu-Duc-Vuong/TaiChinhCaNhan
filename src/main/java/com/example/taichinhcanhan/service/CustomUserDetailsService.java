package com.example.taichinhcanhan.service;

import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

          private final UserAccountRepository userAccountRepository;

          public CustomUserDetailsService(UserAccountRepository userAccountRepository) {
                    this.userAccountRepository = userAccountRepository;
          }

          @Override
          public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
                    UserAccount userAccount = userAccountRepository.findByUsername(login)
                                        .or(() -> userAccountRepository.findByEmail(login.toLowerCase()))
                                        .orElseThrow(() -> new UsernameNotFoundException(
                                                            "Invalid username or password"));

                    return User.withUsername(userAccount.getUsername())
                                        .password(userAccount.getPassword())
                                        .roles("USER")
                                        .build();
          }
}