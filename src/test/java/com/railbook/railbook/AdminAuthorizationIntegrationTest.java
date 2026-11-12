package com.railbook.railbook;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Public endpoints should be accessible anonymously")
    void testPublicEndpoints() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/search"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/pnr"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Unauthenticated access to /admin/dashboard should redirect to /login")
    void testUnauthenticatedAdminAccessRedirects() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("ROLE_USER accessing /admin/dashboard should be forbidden (403)")
    void testNormalUserForbiddenFromAdmin() throws Exception {
        mockMvc.perform(get("/admin/dashboard").with(user("user@railbook.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ROLE_ADMIN accessing /admin/dashboard should be permitted (200 OK)")
    void testAdminUserAccessPermitted() throws Exception {
        mockMvc.perform(get("/admin/dashboard").with(user("admin@railbook.com").roles("ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/trains").with(user("admin@railbook.com").roles("ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/stations").with(user("admin@railbook.com").roles("ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/bookings").with(user("admin@railbook.com").roles("ADMIN")))
                .andExpect(status().isOk());
    }
}
