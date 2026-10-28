package com.tfassbender.mrright.e2e;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.tfassbender.mrright.puzzle.Puzzle;
import com.tfassbender.mrright.puzzle.PuzzleConfig;
import com.tfassbender.mrright.puzzle.PuzzleGenerator;
import com.tfassbender.mrright.puzzle.Suitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.UUID;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Plays a small puzzle to the end: create it, star a wrong set of finalists, then the right one, make one
 * wrong pick, then pick Mr Right. The solution comes from regenerating the puzzle from its seed.
 */
class GameE2eTest extends E2eBase {

    private static final int SUITORS = 100;
    private static final int FINALISTS = 2;

    @ParameterizedTest
    @EnumSource(Viewport.class)
    void playSmallPuzzleToTheEnd(Viewport viewport) {
        Page page = newPage(viewport);
        registerAndLogIn(page);
        String seed = "e2e-" + UUID.randomUUID();
        Puzzle puzzle = PuzzleGenerator.INSTANCE.generate(seed, new PuzzleConfig(SUITORS, FINALISTS));

        createGame(page, seed, SUITORS, FINALISTS);
        assertThat(page.getByTestId("remaining")).hasText("100 of 100 left");

        List<Integer> finalists = puzzle.getFinalistIds().stream().sorted().toList();
        int nonFinalist = puzzle.getSuitors().stream()
                .map(Suitor::getId)
                .filter(id -> !puzzle.getFinalistIds().contains(id))
                .findFirst().orElseThrow();
        crossOut(page, nonFinalist);
        Locator check = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Check").setExact(true));
        assertThat(check).isDisabled();

        toggleStar(page, finalists.get(0));
        toggleStar(page, nonFinalist); // replaces the no-match marker
        assertThat(page.getByTestId("match-count")).containsText("2 / 2");
        submit(page, check);
        assertThat(page.getByRole(AriaRole.ALERT)).containsText("These are not the finalists");

        toggleStar(page, nonFinalist);
        toggleStar(page, finalists.get(1));
        submit(page, check);
        assertThat(page.getByTestId("remaining")).hasText("2 of 100 left");
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("The finalists"))).isVisible();

        int wrongId = puzzle.getFinalistIds().stream().filter(id -> id != puzzle.getSolutionId()).findFirst().orElseThrow();
        pick(page, puzzle.getSuitors().get(wrongId));
        assertThat(page.getByRole(AriaRole.ALERT)).containsText("That is not Mr Right");

        Suitor mrRight = puzzle.getSuitors().get(puzzle.getSolutionId());
        page.waitForResponse(
                response -> response.url().endsWith("/state") && response.request().method().equals("PUT"),
                () -> pick(page, mrRight));
        assertThat(page.getByText("Congratulations!")).isVisible();

        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("My puzzles").setExact(true)).first().click();
        assertThat(page.getByTestId("game-item")).containsText("Solved – Mr Right found!");
        assertThat(page.getByTestId("game-item")).containsText("wrong guesses: 2");
    }

    /** On a phone the clues are a bottom sheet; the "Clues" button opens and closes it. */
    @Test
    void cluesButtonTogglesTheCluesOnAPhone() {
        Page page = newPage(Viewport.MOBILE);
        registerAndLogIn(page);
        createGame(page, "clues-" + UUID.randomUUID(), 100, 2);
        Locator cluesButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Clues").setExact(true));
        Locator panel = page.locator(".clue-panel");

        cluesButton.click();
        assertThat(panel).isVisible();
        assertThat(cluesButton).hasAttribute("aria-expanded", "true");

        cluesButton.click();
        assertThat(panel).isHidden();
        assertThat(cluesButton).hasAttribute("aria-expanded", "false");
    }

    private void submit(Page page, Locator check) {
        check.click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Check them")).click();
    }

    @ParameterizedTest
    @EnumSource(Viewport.class)
    void openingAGameJumpsToTheFirstUnmarkedSuitor(Viewport viewport) {
        Page page = newPage(viewport);
        registerAndLogIn(page);
        createGame(page, "jump-" + UUID.randomUUID(), 500, 2);

        List.of(0, 1, 2).forEach(id -> crossOut(page, id));
        page.waitForResponse(
                response -> response.url().endsWith("/state") && response.request().method().equals("PUT"),
                () -> toggleStar(page, 3));
        page.reload();

        assertThat(page.locator("[data-suitor-id='4']")).isInViewport();
        assertThat(page.locator("[data-suitor-id='3']")).isInViewport(); // at least one row above for orientation
        assertThat(page.locator("[data-suitor-id='0']")).not().isInViewport();
    }

    private void pick(Page page, Suitor suitor) {
        String name = "Mr " + suitor.getFirstName() + " " + suitor.getLastName();
        page.locator(".finalists li").filter(new Locator.FilterOptions().setHasText(name))
                .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("This is Mr Right"))
                .click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Yes, it's him!")).click();
    }
}
