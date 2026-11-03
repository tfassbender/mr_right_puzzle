package com.tfassbender.mrright.e2e;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.tfassbender.mrright.puzzle.EvaRuleSet;
import com.tfassbender.mrright.puzzle.Puzzle;
import com.tfassbender.mrright.puzzle.PuzzleConfig;
import com.tfassbender.mrright.puzzle.PuzzleGenerator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Rule sets beyond the book are unlocked per user. Players without one see no choice at all;
 * an unlocked player picks Eva's rules in "New puzzle" and plays them to the end.
 */
class RuleSetE2eTest extends E2eBase {

    private static final Path DATA_DIR = Path.of(System.getProperty("mrright.e2e.data-dir", "../backend/data"));

    @ParameterizedTest
    @EnumSource(Viewport.class)
    void newPuzzleHasNoRuleSetChoiceWithoutUnlockedRuleSets(Viewport viewport) {
        Page page = newPage(viewport);
        registerAndLogIn(page);

        page.navigate(BASE_URL + "/new");
        assertThat(page.getByLabel("Number of suitors")).isVisible();
        assertThat(ruleSetChoice(page)).hasCount(0);
    }

    @ParameterizedTest
    @EnumSource(Viewport.class)
    void unlockedPlayerPlaysEvasRulesToTheEnd(Viewport viewport) {
        Page page = newPage(viewport);
        registerAndLogIn(page);
        unlockEvasRules(page);
        page.reload(); // the app re-reads the user (and its rule sets) on start
        String seed = "eva-" + UUID.randomUUID();
        Puzzle puzzle = PuzzleGenerator.INSTANCE.generate(EvaRuleSet.INSTANCE, seed, new PuzzleConfig(100, 2));

        createGame(page, seed, 100, 2, "EVA");

        openClues(page);
        assertThat(page.locator(".clue-list li")).hasCount(12);
        assertThat(page.locator(".clue-list")).containsText("He has a car.");
        closeClues(page);
        assertThat(page.locator("[data-suitor-id='0']").getByRole(AriaRole.IMG).first())
                .hasAttribute("aria-label", Pattern.compile("Height: \\d\\.\\d\\d m"));

        List<Integer> finalists = puzzle.getFinalistIds().stream().sorted().toList();
        finalists.forEach(id -> toggleStar(page, id));
        submit(page, page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Check").setExact(true)));
        page.waitForResponse(
                response -> response.url().endsWith("/state") && response.request().method().equals("PUT"),
                () -> pick(page, puzzle.getSuitors().get(puzzle.getSolutionId())));
        assertThat(page.getByText("Congratulations!")).isVisible();

        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("My puzzles").setExact(true)).first().click();
        assertThat(page.getByTestId("game-item")).containsText("Eva's rules · ");
    }

    private static Locator ruleSetChoice(Page page) {
        return page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Rule set"));
    }

    /** On a phone the clues are a bottom sheet behind the "Clues" button; on a desktop they are always shown. */
    private static void openClues(Page page) {
        Locator button = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Clues").setExact(true));
        if (button.isVisible()) button.click();
    }

    private static void closeClues(Page page) {
        Locator close = page.locator(".clue-panel-close");
        if (close.isVisible()) close.click();
    }

    /** Like on the server: add the rule set to the user file by hand. */
    private static void unlockEvasRules(Page page) {
        String userId = (String) page.evaluate("JSON.parse(localStorage.getItem('mrright.session')).user.userId");
        Path file = DATA_DIR.resolve("users").resolve(userId + ".json");
        try {
            String json = Files.readString(file);
            String unlocked = json.replaceFirst("\"unlockedRuleSets\"\\s*:\\s*\\[\\s*]", "\"unlockedRuleSets\":[\"EVA\"]");
            if (unlocked.equals(json)) throw new IllegalStateException("No empty unlockedRuleSets in " + file);
            Files.writeString(file, unlocked);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
