package com.apibp.events;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SmokeTest {

    @Autowired
    MockMvc mvc;

    @Test
    void healthReturnsOk() throws Exception {
        mvc.perform(get("/health"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void authTokenWithDemoCredentials() throws Exception {
        mvc.perform(post("/api/v1/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"demo","password":"demo-password"}
                    """))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.token").isString());
    }

    @Test
    void authTokenWithBadCredentialsReturns401() throws Exception {
        mvc.perform(post("/api/v1/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"demo","password":"wrong"}
                    """))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void mutationWithoutTokenReturns401() throws Exception {
        mvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"slot_id":1,"quantity":1}
                    """))
           .andExpect(status().isUnauthorized());
    }
}
