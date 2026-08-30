package com.clevstack.clevbill.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clevstack.clevbill.dto.UserRequest;
import com.clevstack.clevbill.model.Client;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.model.Role;
import com.clevstack.clevbill.model.User;
import com.clevstack.clevbill.support.AbstractIntegrationTest;
import com.clevstack.clevbill.support.TestEntityFactory;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * User is a fully global master guarded by USER_MGMT:&lt;ACTION&gt;.
 * Special focus areas called out in the task: the response must never leak
 * {@code passwordHash}, duplicate usernames must be rejected, an update
 * with a blank password must leave the existing password unchanged, and
 * disabling a user must actually prevent that user from logging in
 * afterward (CustomUserDetailsService wires {@code enabled} straight into
 * Spring Security's UserDetails.disabled).
 */
class UserControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createUserWithRolesAndPropertyAccessRoundTripsAndHidesPasswordHash() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();
        Property property = factory.createProperty(client);
        Role operator = factory.role("Operator");
        String username = TestEntityFactory.unique("newuser");

        UserRequest request = new UserRequest(
                username, "New User", "SecurePass1", true, List.of(operator.getId()), List.of(property.getId()));

        mockMvc.perform(authJson(post("/api/v1/users"), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.fullName").value("New User"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.roles[0].name").value("Operator"))
                .andExpect(jsonPath("$.propertyIds[0]").value(property.getId()))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void createRejectsBlankUsername() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(post("/api/v1/users"), token)
                        .content(objectMapper.writeValueAsString(
                                new UserRequest("", "Full Name", "SecurePass1", true, List.of(), List.of()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRejectsMissingPassword() throws Exception {
        String token = adminToken();
        String username = TestEntityFactory.unique("nopassuser");

        mockMvc.perform(authJson(post("/api/v1/users"), token)
                        .content(objectMapper.writeValueAsString(
                                new UserRequest(username, "Full Name", null, true, List.of(), List.of()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRejectsDuplicateUsername() throws Exception {
        String token = adminToken();
        User existing = factory.createUser(TestEntityFactory.unique("dupeuser"), "pass1234");

        UserRequest request =
                new UserRequest(existing.getUsername(), "Another Name", "SecurePass1", true, List.of(), List.of());

        mockMvc.perform(authJson(post("/api/v1/users"), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createWithUnknownRoleIdReturnsNotFound() throws Exception {
        String token = adminToken();
        String username = TestEntityFactory.unique("badroleuser");

        UserRequest request =
                new UserRequest(username, "Full Name", "SecurePass1", true, List.of(999999L), List.of());

        mockMvc.perform(authJson(post("/api/v1/users"), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWithUnknownPropertyIdReturnsNotFound() throws Exception {
        String token = adminToken();
        String username = TestEntityFactory.unique("badpropuser");

        UserRequest request =
                new UserRequest(username, "Full Name", "SecurePass1", true, List.of(), List.of(999999L));

        mockMvc.perform(authJson(post("/api/v1/users"), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getReturnsUserWithoutPasswordHash() throws Exception {
        String token = adminToken();
        User user = factory.createUser(TestEntityFactory.unique("getuser"), "pass1234");

        mockMvc.perform(auth(get("/api/v1/users/" + user.getId()), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(user.getUsername()))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void getUnknownUserReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(auth(get("/api/v1/users/999999"), token))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateChangesFullNameAndRoles() throws Exception {
        String token = adminToken();
        User user = factory.createUser(TestEntityFactory.unique("updateuser"), "pass1234");
        Role storeManager = factory.role("Store Manager");

        UserRequest request = new UserRequest(
                user.getUsername(), "Updated Name", null, true, List.of(storeManager.getId()), List.of());

        mockMvc.perform(authJson(put("/api/v1/users/" + user.getId()), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.roles[0].name").value("Store Manager"));
    }

    @Test
    void updateWithBlankPasswordKeepsExistingPassword() throws Exception {
        String token = adminToken();
        String username = TestEntityFactory.unique("keeppassuser");
        User user = factory.createUser(username, "originalPass1");

        UserRequest request = new UserRequest(username, "Full Name", "", true, List.of(), List.of());

        mockMvc.perform(authJson(put("/api/v1/users/" + user.getId()), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Original password must still work.
        String loginToken = login(username, "originalPass1");
        org.junit.jupiter.api.Assertions.assertNotNull(loginToken);
    }

    @Test
    void updateWithNewPasswordChangesPassword() throws Exception {
        String token = adminToken();
        String username = TestEntityFactory.unique("changepassuser");
        User user = factory.createUser(username, "originalPass1");

        UserRequest request = new UserRequest(username, "Full Name", "brandNewPass1", true, List.of(), List.of());

        mockMvc.perform(authJson(put("/api/v1/users/" + user.getId()), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        String loginToken = login(username, "brandNewPass1");
        org.junit.jupiter.api.Assertions.assertNotNull(loginToken);
    }

    @Test
    void updateUnknownUserReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(put("/api/v1/users/999999"), token)
                        .content(objectMapper.writeValueAsString(
                                new UserRequest("nouser", "Full Name", null, true, List.of(), List.of()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void disablingAUserPreventsFurtherLogin() throws Exception {
        String token = adminToken();
        String username = TestEntityFactory.unique("disableuser");
        User user = factory.createUser(username, "pass1234");

        // Confirm the user can log in while enabled.
        String initialLogin = login(username, "pass1234");
        org.junit.jupiter.api.Assertions.assertNotNull(initialLogin);

        UserRequest request = new UserRequest(username, "Full Name", null, false, List.of(), List.of());

        mockMvc.perform(authJson(put("/api/v1/users/" + user.getId()), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginPayload(username, "pass1234"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteRemovesTheUser() throws Exception {
        String token = adminToken();
        User user = factory.createUser(TestEntityFactory.unique("deleteuser"), "pass1234");

        mockMvc.perform(auth(delete("/api/v1/users/" + user.getId()), token))
                .andExpect(status().isOk());

        mockMvc.perform(auth(get("/api/v1/users/" + user.getId()), token))
                .andExpect(status().isNotFound());
    }

    @Test
    void requestsWithoutABearerTokenAreRejected() throws Exception {
        mockMvc.perform(get("/api/v1/users")).andExpect(status().isUnauthorized());
    }

    @Test
    void operatorRoleLacksUserMgmtPermissionAndIsForbidden() throws Exception {
        factory.createUser("operator-user1", "pass1234", "Operator");
        String token = login("operator-user1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/users"), token)).andExpect(status().isForbidden());
    }

    @Test
    void storeManagerLacksUserMgmtPermissionAndIsForbidden() throws Exception {
        factory.createUser("manager-user1", "pass1234", "Store Manager");
        String token = login("manager-user1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/users"), token)).andExpect(status().isForbidden());
    }

    private record LoginPayload(String username, String password) {
    }
}
