package com.bookmyseat.service;

import com.bookmyseat.dao.UserDAO;
import com.bookmyseat.model.User;
import com.bookmyseat.model.enums.Role;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * AuthService — handles registration and login business logic.
 *
 * Rules:
 *  - Passwords are hashed with BCrypt (cost=12) before storage.
 *  - Email uniqueness is enforced here before calling the DAO.
 *  - Login returns the User object on success; empty Optional on failure.
 */
public class AuthService {

    private static final int BCRYPT_COST = 12;
    private final UserDAO userDAO = new UserDAO();

    /**
     * Register a new user.
     *
     * @return the new user_id on success
     * @throws IllegalArgumentException if email already exists or validation fails
     * @throws SQLException             on DB error
     */
    public int register(Connection conn, String name, String email,
                        String plainPassword, String phone, Role role)
            throws SQLException {

        // Validate
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name cannot be blank.");
        if (email == null || !email.contains("@"))
            throw new IllegalArgumentException("Invalid email address.");
        if (plainPassword == null || plainPassword.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters.");

        // Check uniqueness
        if (userDAO.emailExists(conn, email.toLowerCase()))
            throw new IllegalArgumentException("An account with this email already exists.");

        // Hash & save
        String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_COST));

        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(hash);
        user.setPhone(phone == null ? null : phone.trim());
        user.setRole(role);

        return userDAO.register(conn, user);
    }

    /**
     * Attempt login with email + plain password.
     *
     * @return Optional containing the User on success, empty on wrong credentials
     * @throws SQLException on DB error
     */
    public Optional<User> login(Connection conn, String email, String plainPassword)
            throws SQLException {

        Optional<User> found = userDAO.findByEmail(conn, email.trim().toLowerCase());
        if (found.isEmpty()) return Optional.empty();

        User user = found.get();
        if (!BCrypt.checkpw(plainPassword, user.getPasswordHash())) {
            return Optional.empty();
        }
        return Optional.of(user);
    }
}
