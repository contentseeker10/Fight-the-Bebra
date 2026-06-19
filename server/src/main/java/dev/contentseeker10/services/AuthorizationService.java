package dev.contentseeker10.services;

import at.favre.lib.crypto.bcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthorizationService {

    private static final AuthorizationService INSTANCE = new AuthorizationService();
    private AuthorizationService() {}
    public static AuthorizationService getInstance() { return INSTANCE; }

    private static final DatabaseService db = DatabaseService.getInstance();

    public String register(String username, String password) {
        if (!validateUsername(username)) {
            return "Username validation error";
        } else if (!validatePassword(password)) {
            return "Password validation error";
        }

        String passwordHash = BCrypt.withDefaults().hashToString(12, password.toCharArray());

        String sql = "INSERT INTO users(username, password_hash) VALUES(?, ?)";

        try (Connection connection = db.getConnection(); PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, username);
            pst.setString(2, passwordHash);
            return pst.executeUpdate() > 0 ? "Success" : "Database error";
        } catch (SQLException e) {
            return "Database error";
        }
    }

    public String authorize(String username, String password) {
        if (!validateUsername(username)) {
            return "Username validation error";
        } else if (!validatePassword(password)) {
            return "Password validation error";
        }

        String sql = "SELECT * FROM users WHERE username = ?";

        String storedHash;

        try (Connection connection = db.getConnection(); PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, username);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    storedHash = rs.getString("password_hash");
                } else {
                    return "User not found";
                }
            }
        } catch (SQLException e) {
            return "Database error";
        }

        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), storedHash);
        return result.verified ? "Success" : "Wrong password";
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
