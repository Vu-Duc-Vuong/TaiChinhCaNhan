package com.example.taichinhcanhan.service;

import com.example.taichinhcanhan.dto.ChangePasswordRequest;
import com.example.taichinhcanhan.dto.RegisterRequest;
import com.example.taichinhcanhan.dto.UpdateProfileRequest;
import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountService {

          private final UserAccountRepository userAccountRepository;
          private final PasswordEncoder passwordEncoder;

          public UserAccountService(UserAccountRepository userAccountRepository,
                              PasswordEncoder passwordEncoder) {
                    this.userAccountRepository = userAccountRepository;
                    this.passwordEncoder = passwordEncoder;
          }

          @Transactional
          public void register(RegisterRequest request) {
                    String username = request.getUsername().trim();
                    String email = request.getEmail().trim().toLowerCase();

                    if (userAccountRepository.existsByUsername(username)) {
                              throw new IllegalArgumentException("Username đã được sử dụng.");
                    }
                    if (userAccountRepository.existsByEmail(email)) {
                              throw new IllegalArgumentException("Email đã được sử dụng.");
                    }
                    if (!request.getPassword().equals(request.getConfirmPassword())) {
                              throw new IllegalArgumentException("Mật khẩu xác nhận không trùng khớp.");
                    }

                    UserAccount userAccount = new UserAccount();
                    userAccount.setUsername(username);
                    userAccount.setEmail(email);
                    userAccount.setFullName(request.getFullName().trim());
                    userAccount.setPassword(passwordEncoder.encode(request.getPassword()));
                    userAccountRepository.save(userAccount);
          }

          @Transactional(readOnly = true)
          public UserAccount findByUsername(String username) {
                    return userAccountRepository.findByUsername(username)
                                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
          }

          @Transactional
          public void updateProfile(UserAccount userAccount, UpdateProfileRequest request) {
                    String email = request.getEmail().trim().toLowerCase();
                    userAccountRepository.findByEmail(email).ifPresent(existingUser -> {
                              if (!existingUser.getId().equals(userAccount.getId())) {
                                        throw new IllegalArgumentException("Email đã được sử dụng.");
                              }
                    });

                    userAccount.setEmail(email);
                    userAccount.setFullName(request.getFullName().trim());
                    userAccountRepository.save(userAccount);
          }

          @Transactional
          public void changePassword(UserAccount userAccount, ChangePasswordRequest request) {
                    if (!passwordEncoder.matches(request.getCurrentPassword(), userAccount.getPassword())) {
                              throw new IllegalArgumentException("Mật khẩu hiện tại không đúng.");
                    }
                    if (request.getNewPassword().equals(request.getCurrentPassword())) {
                              throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu cũ.");
                    }
                    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                              throw new IllegalArgumentException("Mật khẩu mới xác nhận không trùng khớp.");
                    }

                    userAccount.setPassword(passwordEncoder.encode(request.getNewPassword()));
                    userAccountRepository.save(userAccount);
          }
}