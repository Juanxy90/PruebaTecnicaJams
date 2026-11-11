package com.ufinet.autos.auth.infrastructure.adapter.in.web;

import com.ufinet.autos.auth.application.port.in.AuthResult;
import com.ufinet.autos.auth.application.port.in.GetUserProfileUseCase;
import com.ufinet.autos.auth.application.port.in.LoginCommand;
import com.ufinet.autos.auth.application.port.in.LoginUseCase;
import com.ufinet.autos.auth.application.port.in.RegisterUserUseCase;
import com.ufinet.autos.auth.domain.exception.EmailAlreadyRegisteredException;
import com.ufinet.autos.auth.domain.exception.InvalidCredentialsException;
import com.ufinet.autos.auth.domain.model.User;
import com.ufinet.autos.auth.infrastructure.adapter.in.web.mapper.AuthWebMapper;
import com.ufinet.autos.auth.infrastructure.adapter.out.security.JwtTokenProvider;
import com.ufinet.autos.auth.infrastructure.config.ClockConfig;
import com.ufinet.autos.auth.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba del adaptador web con la configuración de seguridad real
 * (filtro JWT incluido) y los casos de uso simulados.
 */
@WebMvcTest(controllers = {AuthController.class, UserController.class})
@Import({SecurityConfig.class, JwtTokenProvider.class, ClockConfig.class, AuthWebMapper.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtTokenProvider tokenProvider;

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;
    @MockitoBean
    private LoginUseCase loginUseCase;
    @MockitoBean
    private GetUserProfileUseCase getUserProfileUseCase;

    private final User user = User.builder()
            .id(1L).fullName("Demo User").email("demo@ufinet.com").passwordHash("x")
            .createdAt(LocalDateTime.of(2026, 1, 1, 10, 0))
            .build();

    @Test
    @DisplayName("POST /api/auth/register -> 201 con token")
    void registerReturnsCreated() throws Exception {
        when(registerUserUseCase.register(any())).thenReturn(new AuthResult("jwt", 3600, user));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Demo User","email":"demo@ufinet.com","password":"Password123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("jwt"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("demo@ufinet.com"));
    }

    @Test
    @DisplayName("POST /api/auth/register -> 400 con errores por campo")
    void registerValidatesInput() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"","email":"no-es-correo","password":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    @DisplayName("POST /api/auth/register -> 409 si el correo ya existe")
    void registerConflict() throws Exception {
        when(registerUserUseCase.register(any())).thenThrow(new EmailAlreadyRegisteredException("demo@ufinet.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Demo","email":"demo@ufinet.com","password":"Password123"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST /api/auth/login -> 200 con token")
    void loginOk() throws Exception {
        when(loginUseCase.login(new LoginCommand("demo@ufinet.com", "Password123")))
                .thenReturn(new AuthResult("jwt", 3600, user));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"demo@ufinet.com","password":"Password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    @DisplayName("POST /api/auth/login -> 401 con credenciales inválidas")
    void loginInvalidCredentials() throws Exception {
        when(loginUseCase.login(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"demo@ufinet.com","password":"mala"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/users/me sin token -> 401")
    void meWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("No autorizado"));
    }

    @Test
    @DisplayName("GET /api/users/me con token inválido -> 401")
    void meWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer token-falso"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/users/me con token válido -> 200")
    void meWithValidToken() throws Exception {
        when(getUserProfileUseCase.getById(1L)).thenReturn(user);
        String token = tokenProvider.generateToken(user);

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Demo User"));
    }
}
