package com.luispiquinrey.backend;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTests {
    private static final JavaClasses CODE = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.luispiquinrey.backend");

    @Test
    @Timeout(5)
    @Tag("architecture")
    void domainDependsOnlyOnDomainAndSharedValues() {
        classes().that().resideInAPackage("..domain..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        "java..", "org.jmolecules..", "..domain..", "..share.identity..", "..share.time..")
                .check(CODE);
    }

    @Test
    @Timeout(5)
    @Tag("architecture")
    void applicationIsIndependentOfFrameworksAndAdapters() {
        classes().that().resideInAPackage("..application..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        "java..", "..application..", "..domain..", "..account.api..",
                        "..share.identity..", "..share.time..", "..share.events..", "..share.logging..")
                .check(CODE);
    }

    @Test
    @Timeout(5)
    @Tag("architecture")
    void sharedContractsRemainIndependent() {
        classes().that().resideInAnyPackage("..share.identity..", "..share.time..", "..share.events..", "..share.logging..")
                .and().doNotHaveSimpleName("package-info")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        "java..", "org.jmolecules..", "..share.identity..", "..share.time..",
                        "..share.events..", "..share.logging..")
                .check(CODE);
    }

    @Test
    @Timeout(5)
    @Tag("architecture")
    void presentationDoesNotDependOnInfrastructure() {
        noClasses().that().resideInAPackage("..presentation..")
                .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..adapter..")
                .check(CODE);
    }
}
