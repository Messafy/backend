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
                .and().resideOutsideOfPackage("..notes.application.use_cases..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        "java..", "..application..", "..domain..", "..account.api..",
                        "..share.identity..", "..share.time..", "..share.events..", "..share.logging..", "..share.result..")
                .check(CODE);
    }

    @Test
    @Timeout(5)
    @Tag("architecture")
    void noteUseCasesOnlyDependOnSpringForComponentRegistration() {
        classes().that().resideInAPackage("..notes.application.use_cases..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        "java..", "..application..", "..domain..", "..account.api..",
                        "..share.identity..", "..share.time..", "..share.events..", "..share.logging..",
                        "..share.result..", "org.springframework.stereotype..")
                .check(CODE);
    }

    @Test
    @Timeout(5)
    @Tag("architecture")
    void sharedContractsRemainIndependent() {
        classes().that().resideInAnyPackage("..share.identity..", "..share.time..", "..share.events..", "..share.logging..", "..share.result..")
                .and().doNotHaveSimpleName("package-info")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        "java..", "org.jmolecules..", "..share.identity..", "..share.time..",
                        "..share.events..", "..share.logging..", "..share.result..")
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

    @Test
    @Timeout(5)
    @Tag("architecture")
    void notePresentationDependsOnInputPortsInsteadOfUseCases() {
        noClasses().that().resideInAPackage("..notes.presentation..")
                .should().dependOnClassesThat().resideInAPackage("..notes.application.use_cases..")
                .check(CODE);
    }

    @Test
    @Timeout(5)
    @Tag("architecture")
    void presentersDependOnlyOnApplicationDataAndPresentationModels() {
        classes().that().resideInAnyPackage(
                        "..account.presentation.presenter..", "..notes.presentation.presenter..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        "java..", "..account.application.dto.response..", "..notes.application.dto.response..",
                        "..share.presentation..", "..share.result..",
                        "..account.presentation.presenter..", "..notes.presentation.presenter..",
                        "..account.presentation.viewmodel..", "..notes.presentation.viewmodel..",
                        "org.springframework.stereotype..")
                .check(CODE);
    }

    @Test
    @Timeout(5)
    @Tag("architecture")
    void presentationModelsRemainIndependentOfFrameworksAndApplication() {
        classes().that().resideInAnyPackage(
                        "..share.presentation..", "..account.presentation.viewmodel..", "..notes.presentation.viewmodel..")
                .and().doNotHaveSimpleName("ErrorPresenter")
                .and().doNotHaveSimpleName("HttpResponsePresenter")
                .and().doNotHaveSimpleName("package-info")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        "java..", "..share.presentation..",
                        "..account.presentation.viewmodel..", "..notes.presentation.viewmodel..",
                        "org.springframework.stereotype..")
                .check(CODE);
    }

    @Test
    @Timeout(5)
    @Tag("architecture")
    void sharedPresentationContractsDoNotDependOnAnyContext() {
        classes().that().resideInAPackage("..share.presentation..")
                .and().doNotHaveSimpleName("ErrorPresenter")
                .and().doNotHaveSimpleName("HttpResponsePresenter")
                .and().doNotHaveSimpleName("package-info")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        "java..", "..share.presentation..", "org.springframework.stereotype..")
                .check(CODE);
    }

    @Test
    @Timeout(5)
    @Tag("architecture")
    void sharedHttpResponsePresenterOnlyUsesPresentationAndSpringHttp() {
        classes().that().haveSimpleName("HttpResponsePresenter")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        "java..", "..share.presentation..", "org.springframework.http..",
                        "org.springframework.stereotype..")
                .check(CODE);
    }

    @Test
    @Timeout(5)
    @Tag("architecture")
    void errorPresenterOnlyDependsOnSharedPresentationAndSpringRegistration() {
        classes().that().haveSimpleName("ErrorPresenter")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        "java..", "..share.presentation..", "org.springframework.stereotype..")
                .check(CODE);
    }

    @Test
    @Timeout(5)
    @Tag("architecture")
    void notificationApplicationDoesNotDependOnAdaptersOrOtherContextsExceptAccountApi() {
        classes().that().resideInAPackage("..notify.application..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        "java..", "..notify.application..", "..share.logging..", "..account.api..")
                .check(CODE);
    }

    @Test
    @Timeout(5)
    @Tag("architecture")
    void notificationInfrastructureOnlyBootstrapsItsApplicationAndAdapters() {
        classes().that().resideInAPackage("..notify.infrastructure..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        "java..", "..notify.application..", "..notify.infrastructure..",
                        "org.springframework..")
                .check(CODE);
    }

    @Test
    @Timeout(5)
    @Tag("architecture")
    void notificationInputAdapterDependsOnItsPortAndAccountEvent() {
        classes().that().resideInAPackage("..notify.adapter.in..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        "java..", "..notify.application.port.in..", "..notify.application.dto..",
                        "..account.api..", "org.springframework..", "org.slf4j..")
                .check(CODE);
    }

    @Test
    @Timeout(5)
    @Tag("architecture")
    void sharedInfrastructureIsGroupedByTechnicalResponsibility() {
        classes().that().resideInAPackage("..share.infrastructure..")
                .and().doNotHaveSimpleName("package-info")
                .should().resideInAnyPackage(
                        "..share.infrastructure.config..", "..share.infrastructure.events..",
                        "..share.infrastructure.logging..", "..share.infrastructure.observability..",
                        "..share.infrastructure.web..")
                .check(CODE);
    }
}
