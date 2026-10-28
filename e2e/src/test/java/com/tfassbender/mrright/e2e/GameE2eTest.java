package com.tfassbender.mrright.e2e;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.tfassbender.mrright.puzzle.Puzzle;
import com.tfassbender.mrright.puzzle.PuzzleConfig;
import com.tfassbender.mrright.puzzle.PuzzleGenerator;
import com.tfassbender.mrright.puzzle.Suitor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.UUID;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Plays a small puzzle to the end: create it, cross out everyone but the finalists, make one wrong pick,
 * then pick Mr Right. The solution comes from regenerating the puzzle from its seed.
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

        createGame(page, seed);
        assertThat(page.getByTestId("remaining")).hasText("100 of 100 left");

        List<Integer> nonFinalists = puzzle.getSuitors().stream()
                .map(Suitor::getId)
                .filter(id -> !puzzle.getFinalistIds().contains(id))
                .toList();
        nonFinalists.forEach(id -> crossOut(page, id));
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
        assertThat(page.getByTestId("game-item")).containsText("wrong guesses: 1");
    }

    private void registerAndLogIn(Page page) {
        String name = uniqueName();
        page.navigate(BASE_URL + "/register");
        page.getByLabel("Email").fill(name + "@example.com");
        page.getByLabel("Display name").fill(name);
        page.getByLabel("Password").fill(PASSWORD);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create account")).click();
        assertThat(page.getByText("Welcome, " + name + "!")).isVisible();
    }

    private void createGame(Page page, String seed) {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("New puzzle")).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("100").setExact(true)).click();
        page.getByLabel("Finalists").selectOption(String.valueOf(FINALISTS));
        page.getByLabel("Seed (optional)").fill(seed);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create puzzle")).click();
    }

    /** The directory is virtualized: scroll the row into the rendered window first, then tap it. */
    private void crossOut(Page page, int id) {
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

    private void pick(Page page, Suitor suitor) {
        String name = "Mr " + suitor.getFirstName() + " " + suitor.getLastName();
        page.locator(".finalists li").filter(new Locator.FilterOptions().setHasText(name))
                .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("This is Mr Right"))
                .click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Yes, it's him!")).click();
    }
}
