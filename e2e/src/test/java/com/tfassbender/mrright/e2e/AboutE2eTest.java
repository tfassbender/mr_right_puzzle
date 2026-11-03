package com.tfassbender.mrright.e2e;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** The About dialog opens from a header button that exists only on the game list. */
class AboutE2eTest extends E2eBase {

    private static final String GITHUB_URL = "https://github.com/tfassbender/mr_right_puzzle";

    @ParameterizedTest
    @EnumSource(Viewport.class)
    void aboutDialogOpensFromTheHeaderOfTheGameList(Viewport viewport) {
        Page page = newPage(viewport);
        registerAndLogIn(page);

        aboutButton(page).click();
        Locator dialog = page.getByRole(AriaRole.DIALOG);
        assertThat(dialog).containsText("Version");
        assertThat(dialog.getByRole(AriaRole.LINK)).hasAttribute("href", GITHUB_URL);

        Locator close = dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Close"));
        assertThat(close).isInViewport();
        close.click();
        assertThat(dialog).isHidden();

        page.navigate(BASE_URL + "/new");
        assertThat(aboutButton(page)).isHidden();
    }

    private static Locator aboutButton(Page page) {
        return page.locator("header").getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("About"));
    }
}
