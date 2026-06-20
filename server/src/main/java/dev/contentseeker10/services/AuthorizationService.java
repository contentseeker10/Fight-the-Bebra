package dev.contentseeker10.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import dev.contentseeker10.dto.*;
import dev.contentseeker10.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthorizationService {

    private static final AuthorizationService INSTANCE = new AuthorizationService();
    private AuthorizationService() {}
    public static AuthorizationService getInstance() { return INSTANCE; }

    private static final DatabaseService db = DatabaseService.getInstance();

    public RegisterResponseDTO register(RegisterRequestDTO request) {
        if (!validateUsername(request.username())) {
            return new RegisterResponseDTO(false, "Username Validation Error");
        } else if (!validatePassword(request.password())) {
            return new RegisterResponseDTO(false, "Password Validation Error");
        }

        String passwordHash = BCrypt.withDefaults().hashToString(12, request.password().toCharArray());

        String sql = "INSERT INTO users(username, password_hash) VALUES(?, ?)";

        try (Connection connection = db.getConnection(); PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, request.username());
            pst.setString(2, passwordHash);
            return pst.executeUpdate() > 0
                    ? new RegisterResponseDTO(true, "")
                    : new RegisterResponseDTO(false, "Database Error");
        } catch (SQLException e) {
            return new RegisterResponseDTO(false, "Database Error");
        }
    }

    public AuthResponseDTO authorize(AuthRequestDTO request) {
        if (!validateUsername(request.username())) {
            return new AuthResponseDTO(false, "Username Validation Error", null);
        } else if (!validatePassword(request.password())) {
            return new AuthResponseDTO(false, "Password Validation Error", null);
        }

        String sql = "SELECT * FROM users WHERE username = ?";

        UserDTO user;
        try (Connection connection = db.getConnection(); PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, request.username());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    BCrypt.Result result = BCrypt.verifyer().verify(request.password().toCharArray(), storedHash);
                    if (!result.verified) {
                        return new AuthResponseDTO(false, "Wrong Password", null);
                    }
                    user = new UserDTO(rs.getInt("id"),
                            rs.getString("username"),
                            rs.getInt("record_score"),
                            rs.getInt("death_count")
                            );
                } else {
                    return new AuthResponseDTO(false, "User Not Found", null);
                }
            }
        } catch (SQLException e) {
            return new AuthResponseDTO(false, "Database Error" + e.getMessage(), null);
        }

        return new AuthResponseDTO(true, "", user);
    }

    private boolean validateUsername(String username) {
        // TODO: validation of username
        return true;
    }

    private boolean validatePassword(String password) {
        // TODO: validation of password
        return true;
    }

}
