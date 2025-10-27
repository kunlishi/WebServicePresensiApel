package com.polstat.WebServiceApel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polstat.WebServiceApel.dto.AuthenticationRequest;
import com.polstat.WebServiceApel.dto.AuthenticationResponse;
import com.polstat.WebServiceApel.security.jwt.JwtAuthenticationFilter;
import com.polstat.WebServiceApel.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AuthService authService;

    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void login_ok() throws Exception {
        ResponseEntity<?> resp = ResponseEntity.ok(AuthenticationResponse.builder().token("t").role("MAHASISWA").build());
        when(authService.login(any(AuthenticationRequest.class)))
                .thenReturn((ResponseEntity) resp);

        AuthenticationRequest req = new AuthenticationRequest();
        req.setUsername("u");
        req.setPassword("p");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("token")));
    }
}
