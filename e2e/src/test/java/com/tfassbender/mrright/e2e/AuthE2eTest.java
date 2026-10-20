package com.tfassbender.mrright.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Register, log out and log in again – on desktop and phone size. The dev backend skips email verification. */
class AuthE2eTest extends E2eBase {

    @ParameterizedTest
    @EnumSource(Viewport.class)
    void registerLogoutAndLogin(Viewport viewport) {
        Page page = newPage(viewport);
        String name = uniqueName();
        String email = name + "@example.com";

        page.navigate(BASE_URL + "/");
        assertThat(page).hasURL(BASE_URL + "/login");

        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Create account")).click();
        page.getByLabel("Email").fill(email);
        page.getByLabel("Display name").fill(name);
        page.getByLabel("Password").fill(PASSWORD);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create account")).click();
        assertThat(page.getByText("Welcome, " + name + "!")).isVisible();

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Log out")).click();
        assertThat(page).hasURL(BASE_URL + "/login");

        page.getByLabel("Email").fill(email);
        page.getByLabel("Password").fill(PASSWORD);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Log in")).click();
        assertThat(page.getByText("Welcome, " + name + "!")).isVisible();
    }

    @ParameterizedTest
    @EnumSource(Viewport.class)
    void wrongPasswordShowsTranslatedError(Viewport viewport) {
        Page page = newPage(viewport);

        page.navigate(BASE_URL + "/login");
        page.getByLabel("Email").fill("nobody-" + uniqueName() + "@example.com");
        page.getByLabel("Password").fill("wrong-password");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Log in")).click();

        assertThat(page.getByRole(AriaRole.ALERT)).hasText("Email or password is wrong.");
    }
}
