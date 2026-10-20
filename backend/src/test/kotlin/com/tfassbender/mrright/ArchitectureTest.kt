package com.tfassbender.mrright

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import org.junit.jupiter.api.Test

/**
 * The only two architecture rules of this project (see doc/architecture.md) – deliberately no layering.
 * Checks the production classes only.
 */
class ArchitectureTest {

    private val productionClasses: JavaClasses = ClassFileImporter()
        .withImportOption(ImportOption.DoNotIncludeTests())
        .importPackages("com.tfassbender.mrright")

    @Test
    fun `packages have no dependency cycles`() {
        slices().matching("com.tfassbender.mrright.(*)..")
            .should().beFreeOfCycles()
            .check(productionClasses)
    }

    @Test
    fun `puzzle package is plain Kotlin without Quarkus or REST`() {
        noClasses().that().resideInAPackage("..puzzle..")
            .should().dependOnClassesThat().resideInAnyPackage("io.quarkus..", "jakarta..", "io.vertx..")
            .because("the generator must stay fast to unit-test and deterministic")
            // The puzzle package doesn't exist yet; the rule applies as soon as it does.
            .allowEmptyShould(true)
            .check(productionClasses)
    }
}
