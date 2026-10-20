package com.tfassbender.mrright.e2e;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Shared setup of the browser tests: one Chromium per test class and a fresh browser context per page.
 * Requires both dev servers ({@code ./gradlew :e2e:startE2eServers}); the tests fail if they are down.
 */
abstract class E2eBase {

    static final String BASE_URL = System.getProperty("mrright.e2e.base-url", "http://localhost:5174");
    static final boolean HEADLESS = Boolean.parseBoolean(System.getProperty("playwright.headless", "true"));
    static final String PASSWORD = "secret-password";

    /** The two layouts every UI feature has to work in. */
    enum Viewport {
        DESKTOP(1280, 800, false),
        MOBILE(390, 844, true);

        final int width;
        final int height;
        final boolean mobile;

        Viewport(int width, int height, boolean mobile) {
            this.width = width;
            this.height = height;
            this.mobile = mobile;
        }
    }

    static Playwright playwright;
    static Browser browser;

    private final List<BrowserContext> contexts = new ArrayList<>();

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(HEADLESS));
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @AfterEach
    void closeContexts() {
        contexts.forEach(BrowserContext::close);
        contexts.clear();
    }

    Page newPage(Viewport viewport) {
        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(viewport.width, viewport.height)
                .setIsMobile(viewport.mobile)
                .setHasTouch(viewport.mobile)
                .setLocale("en-US"));
        contexts.add(context);
        return context.newPage();
    }

    /** A display name that is unique per test run and valid (4–15 chars). */
    static String uniqueName() {
        return "e2e" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }
}
