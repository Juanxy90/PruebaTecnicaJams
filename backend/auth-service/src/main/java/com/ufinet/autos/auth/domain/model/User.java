package com.ufinet.autos.auth.domain.model;

import com.ufinet.autos.auth.domain.exception.InvalidUserDataException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Entidad de dominio (POJO puro, sin dependencias de JPA ni de Spring).
 * Inmutable: cualquier cambio genera una nueva instancia.
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@ToString(exclude = "passwordHash")
public class User {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_EMAIL_LENGTH = 150;

    private final Long id;
    private final String fullName;
    private final String email;
    private final String passwordHash;
    private final LocalDateTime createdAt;

    /**
     * Factory Method: crea un usuario nuevo validando las invariantes del dominio.
     */
    public static User newUser(String fullName, String email, String passwordHash, LocalDateTime createdAt) {
        String normalizedName = fullName == null ? "" : fullName.trim();
        String normalizedEmail = normalizeEmail(email);

        if (normalizedName.isEmpty() || normalizedName.length() > MAX_NAME_LENGTH) {
            throw new InvalidUserDataException(
                    "El nombre es obligatorio y debe tener máximo " + MAX_NAME_LENGTH + " caracteres");
        }
        if (normalizedEmail == null || normalizedEmail.length() > MAX_EMAIL_LENGTH
                || !EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new InvalidUserDataException("El correo electrónico no es válido");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new InvalidUserDataException("La contraseña es obligatoria");
        }

        return User.builder()
                .fullName(normalizedName)
                .email(normalizedEmail)
                .passwordHash(passwordHash)
                .createdAt(createdAt)
                .build();
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
