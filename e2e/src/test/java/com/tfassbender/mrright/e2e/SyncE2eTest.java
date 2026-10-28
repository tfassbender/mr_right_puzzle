package com.tfassbender.mrright.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.UUID;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Saving across connection loss and devices: offline play, and the conflict dialog when two devices played. */
class SyncE2eTest extends E2eBase {

    private static boolean isStateSave(Response response) {
        return response.url().endsWith("/state") && response.request().method().equals("PUT");
    }

    private static void crossOutAndWaitForSave(Page page, int id) {
        page.waitForResponse(SyncE2eTest::isStateSave, () -> crossOut(page, id));
    }

    private static void assertCrossedOut(Page page, int id, boolean crossedOut) {
        assertThat(page.locator("[data-suitor-id='" + id + "']")).hasAttribute("aria-pressed", String.valueOf(crossedOut));
    }

    @ParameterizedTest
    @EnumSource(Viewport.class)
    void offlineChangesAreSavedWhenTheConnectionIsBack(Viewport viewport) {
        Page page = newPage(viewport);
        registerAndLogIn(page);
        createGame(page, "sync-" + UUID.randomUUID(), 100, 2);
        crossOutAndWaitForSave(page, 0);

        page.context().setOffline(true);
        crossOut(page, 1);
        assertThat(page.getByTestId("sync-note")).containsText("Offline", new LocatorAssertions.ContainsTextOptions().setTimeout(10_000));

        page.waitForResponse(SyncE2eTest::isStateSave, () -> page.context().setOffline(false));
        assertThat(page.getByTestId("sync-note")).isHidden();

        Object serverCrossedOut = page.evaluate("""
                async () => {
                  const token = JSON.parse(localStorage.getItem('mrright.session')).token;
                  const id = location.pathname.split('/').pop();
                  const response = await fetch(`/api/games/${id}/state`, { headers: { Authorization: `Bearer ${token}` } });
                  return (await response.json()).crossedOut;
                }""");
        assertEquals(List.of(0, 1), serverCrossedOut);
    }

    @ParameterizedTest
    @EnumSource(Viewport.class)
    void conflictingChangesLetThePlayerChoose(Viewport viewport) {
        Page laptop = newPage(viewport);
        String name = registerAndLogIn(laptop);
        createGame(laptop, "conflict-" + UUID.randomUUID(), 100, 2);
        String gameUrl = laptop.url();

        Page phone = newPage(viewport);
        logIn(phone, name);
        phone.navigate(gameUrl);
        assertThat(phone.getByTestId("remaining")).hasText("100 of 100 left");

        crossOutAndWaitForSave(laptop, 0);
        crossOut(phone, 1); // based on the old version → 409 on save

        assertThat(phone.getByRole(AriaRole.DIALOG)).containsText("Which version do you want to keep?",
                new LocatorAssertions.ContainsTextOptions().setTimeout(10_000));
        phone.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Take the other one")).click();

        assertThat(phone.getByRole(AriaRole.DIALOG)).isHidden();
        assertCrossedOut(phone, 0, true);
        assertCrossedOut(phone, 1, false);
    }
}
