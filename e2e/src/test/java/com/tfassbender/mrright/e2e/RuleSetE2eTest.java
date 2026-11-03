package com.tfassbender.mrright.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Players without unlocked rule sets only play the book – "New puzzle" offers nothing to choose. */
class RuleSetE2eTest extends E2eBase {

    @ParameterizedTest
    @EnumSource(Viewport.class)
    void newPuzzleHasNoRuleSetChoiceWithoutUnlockedRuleSets(Viewport viewport) {
        Page page = newPage(viewport);
        registerAndLogIn(page);

        page.navigate(BASE_URL + "/new");
        assertThat(page.getByLabel("Number of suitors")).isVisible();
        assertThat(page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Rule set"))).hasCount(0);
    }
}
