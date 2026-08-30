package com.clevstack.clevbill.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

/**
 * Base class for backend integration tests. Boots the full Spring context
 * (real controllers, services, JPA, Spring Security) against a real
 * Postgres 17 instance via Testcontainers, with Flyway applying every
 * migration exactly as it would in production — no H2/mocked-repository
 * shortcuts, since this codebase's correctness lives in real SQL
 * constraints (unique indexes, FKs, the invoice_counters row lock) that an
 * in-memory substitute wouldn't exercise faithfully.
 *
 * <p>The container is started once in a static initializer (the
 * Testcontainers "singleton container" pattern) and never stopped
 * explicitly — Testcontainers' Ryuk reaper cleans it up when the JVM
 * exits. Every subclass with identical Spring context configuration
 * shares one cached ApplicationContext (so Flyway + the default admin
 * seeder each run exactly once for the whole suite), which is why each
 * test method also runs inside a Spring-managed transaction that's rolled
 * back afterward ({@code @Transactional}) — that's what gives each test
 * a clean slate without paying to re-migrate the schema every time.
 *
 * <p>Every {@code @Async} / {@code @Scheduled} / {@code AFTER_COMMIT}
 * event listener (see EInvoiceEventListener) simply does not fire under
 * this rollback-based isolation, since the surrounding transaction never
 * really commits. That's intentional here, not a gap: it keeps
 * e-invoice-adjacent tests deterministic instead of racing a background
 * thread hitting the (randomly-failing) MockIrpClient.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
public abstract class AbstractIntegrationTest {

    protected static final String DEFAULT_ADMIN_USERNAME = "admin";
    protected static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    private static final PostgreSQLContainer<?> POSTGRES;

    static {
        POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected TestEntityFactory factory;

    /** Logs in via the real /api/v1/auth/login endpoint and returns the access token. */
    protected String login(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(new LoginPayload(username, password));
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    /** Convenience for the seeded Super Admin — has every permission and bypasses property-access checks. */
    protected String adminToken() throws Exception {
        return login(DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD);
    }

    protected MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder builder, String token) {
        return builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    protected MockHttpServletRequestBuilder authJson(MockHttpServletRequestBuilder builder, String token) {
        return auth(builder, token).contentType(MediaType.APPLICATION_JSON);
    }

    private record LoginPayload(String username, String password) {
    }
}
