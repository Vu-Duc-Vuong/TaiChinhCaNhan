package com.example.taichinhcanhan;

import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
class AuthPageIntegrationTest {

          @Autowired
          private MockMvc mockMvc;

          @Autowired
          private UserAccountRepository userAccountRepository;

          @Autowired
          private PasswordEncoder passwordEncoder;

          @Test
          void registerPageShouldRender() throws Exception {
                    mockMvc.perform(get("/register"))
                                        .andExpect(status().isOk());
          }

          @Test
          void forgotPasswordPageShouldRender() throws Exception {
                    mockMvc.perform(get("/forgot-password"))
                                        .andExpect(status().isOk());
          }

          @Test
          void profilePageShouldRenderForAuthenticatedUser() throws Exception {
                    UserAccount userAccount = testUser();

                    mockMvc.perform(get("/profile")
                                        .with(user(userAccount.getUsername()).password("Password123@").roles("USER")))
                                        .andExpect(status().isOk());
          }

          @Test
          void createFormsShouldRenderForAuthenticatedUser() throws Exception {
                    UserAccount userAccount = testUser();
                    var authenticatedUser = user(userAccount.getUsername()).password("Password123@").roles("USER");

                    for (String path : List.of("/transactions/new", "/transfers/new", "/categories/new",
                                        "/budgets/new")) {
                              mockMvc.perform(get(path).with(authenticatedUser))
                                                  .andExpect(status().isOk());
                    }
          }

          @Test
          void invalidCreateFormsShouldReturnWithValidationErrors() throws Exception {
                    UserAccount userAccount = testUser();
                    var authenticatedUser = user(userAccount.getUsername()).password("Password123@").roles("USER");

                    for (String path : List.of("/transactions", "/transfers", "/categories", "/budgets")) {
                              mockMvc.perform(post(path).with(authenticatedUser).with(csrf()))
                                                  .andExpect(status().isOk());
                    }
          }

          @Test
          void moneySourceTypesAndZeroStatisticsShouldRenderInVietnamese() throws Exception {
                    UserAccount userAccount = testUser();
                    var authenticatedUser = user(userAccount.getUsername()).password("Password123@").roles("USER");

                    mockMvc.perform(get("/money-sources/new").with(authenticatedUser))
                                        .andExpect(status().isOk())
                                        .andExpect(content().string(org.hamcrest.Matchers.containsString("Tiền mặt")))
                                        .andExpect(content().string(org.hamcrest.Matchers
                                                            .containsString("Tài khoản ngân hàng")));

                    mockMvc.perform(get("/statistics").with(authenticatedUser))
                                        .andExpect(status().isOk())
                                        .andExpect(content().string(org.hamcrest.Matchers.containsString("0.00 đ")));
          }

          private UserAccount testUser() {
                    String username = "profileuser";
                    return userAccountRepository.findByUsername(username)
                                        .orElseGet(() -> {
                                                  UserAccount created = new UserAccount();
                                                  created.setUsername(username);
                                                  created.setEmail("profileuser@example.com");
                                                  created.setFullName("Profile User");
                                                  created.setPassword(passwordEncoder.encode("Password123@"));
                                                  return userAccountRepository.save(created);
                                        });
          }
}
