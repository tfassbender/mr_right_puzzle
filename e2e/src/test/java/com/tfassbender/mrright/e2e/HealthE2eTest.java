package com.tfassbender.mrright.e2e;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Connectivity smoke test: the health endpoint answers through the dev server's /api proxy. */
class HealthE2eTest {

    private static final String BASE_URL = System.getProperty("mrright.e2e.base-url", "http://localhost:5174");

    private static Playwright playwright;
    private static APIRequestContext request;

    @BeforeAll
    static void launch() {
        playwright = Playwright.create();
        request = playwright.request().newContext();
    }

    @AfterAll
    static void close() {
        if (request != null) request.dispose();
        if (playwright != null) playwright.close();
    }

    @Test
    void healthEndpointReportsUp() {
        APIResponse response = request.get(BASE_URL + "/api/health");
        assertThat(response).isOK();
        assertTrue(response.text().contains("\"status\":\"UP\""), "unexpected health body: " + response.text());
    }
}
