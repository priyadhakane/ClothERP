package com.clotherp.backend.modules.auth;

import com.clotherp.backend.modules.user.UserDTO;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    UserDTO getCurrentUser(String username);
}
