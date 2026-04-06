package com.finance.service;

import com.finance.dto.request.RegisterRequest;
import com.finance.dto.request.UserUpdateRequest;
import com.finance.dto.response.UserResponse;
import com.finance.entity.User;
import com.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    public UserResponse create(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail()))
            throw new IllegalArgumentException("Email already in use");
        if (userRepository.existsByUsername(req.getUsername()))
            throw new IllegalArgumentException("Username already in use");

        User user = userRepository.save(User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole())
                .status(User.UserStatus.ACTIVE)
                .build());

        return UserResponse.from(user);
    }

    public UserResponse update(Long id, UserUpdateRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (req.getRole() != null)   user.setRole(req.getRole());
        if (req.getStatus() != null) user.setStatus(req.getStatus());

        return UserResponse.from(userRepository.save(user));
    }

    public void delete(Long id, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail).orElseThrow();
        if (id.equals(requester.getId()))
            throw new IllegalArgumentException("Cannot delete your own account");
        if (!userRepository.existsById(id))
            throw new IllegalArgumentException("User not found");
        userRepository.deleteById(id);
    }
}
