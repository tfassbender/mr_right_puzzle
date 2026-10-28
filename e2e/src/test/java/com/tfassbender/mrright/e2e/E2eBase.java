package com.tfassbender.mrright.e2e;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

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

    /** A display name that is unique per test run and valid (3–15 chars). */
    static String uniqueName() {
        return "e2e" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    /** Registers a new account (the dev backend logs in directly) and returns its display name. */
    static String registerAndLogIn(Page page) {
        String name = uniqueName();
        page.navigate(BASE_URL + "/register");
        page.getByLabel("Email").fill(name + "@example.com");
        page.getByLabel("Display name").fill(name);
        page.getByLabel("Password").fill(PASSWORD);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create account")).click();
        assertThat(page.getByText("Welcome, " + name + "!")).isVisible();
        return name;
    }

    static void logIn(Page page, String name) {
        page.navigate(BASE_URL + "/login");
        page.getByLabel("Email").fill(name + "@example.com");
        page.getByLabel("Password").fill(PASSWORD);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Log in")).click();
        assertThat(page.getByText("Welcome, " + name + "!")).isVisible();
    }

    static void createGame(Page page, String seed, int suitors, int finalists) {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("New puzzle")).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(String.valueOf(suitors)).setExact(true)).click();
        page.getByLabel("Finalists").selectOption(String.valueOf(finalists));
        page.getByLabel("Seed (optional)").fill(seed);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create puzzle")).click();
        assertThat(page.getByTestId("remaining")).isVisible();
    }

    /** The directory is virtualized: scroll the row into the rendered window first, then tap it. */
    static void crossOut(Page page, int id) {
        page.evaluate("""
                id => {
                  const scroll = document.querySelector('.directory-scroll');
                  const itemHeight = Number(scroll.dataset.itemHeight);
                  const index = id + Math.floor(id / 100) + 1; // every section of 100 starts with a header
                  scroll.scrollTop = index * itemHeight - scroll.clientHeight / 2;
                }""", id);
        page.locator("[data-suitor-id='" + id + "']").click();
        assertThat(page.locator("[data-suitor-id='" + id + "']")).hasAttribute("aria-pressed", "true");
    }
}
