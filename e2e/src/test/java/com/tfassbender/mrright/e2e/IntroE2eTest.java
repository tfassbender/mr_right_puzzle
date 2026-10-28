package com.tfassbender.mrright.e2e;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** The story and the rules open from the game list in a dialog that fits on a phone. */
class IntroE2eTest extends E2eBase {

    @ParameterizedTest
    @EnumSource(Viewport.class)
    void storyAndRulesOpenFromTheGameList(Viewport viewport) {
        Page page = newPage(viewport);
        registerAndLogIn(page);

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Story & rules")).click();
        Locator dialog = page.getByRole(AriaRole.DIALOG);
        assertThat(dialog).containsText("The story");
        assertThat(dialog).containsText("The rules");

        Locator close = dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Let's begin"));
        assertThat(close).isInViewport();
        close.click();
        assertThat(dialog).isHidden();
    }
}
