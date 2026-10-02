package com.sentinel.core.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.sentinel.core");
    }

    @Test
    @DisplayName("Camada de domínio não deve depender de frameworks (Jakarta, Quarkus, Hibernate, MicroProfile)")
    void domainMustNotDependOnFrameworks() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta..",
                        "io.quarkus..",
                        "org.hibernate..",
                        "org.eclipse.microprofile.."
                )
                .because("Entidades e portas de domínio devem ser Java puro, livres de frameworks")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Camada de domínio não deve depender de camadas externas (application ou adapter)")
    void domainMustNotDependOnOuterLayers() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..application..",
                        "..adapter.."
                )
                .because("A regra de dependência da Clean Architecture proíbe domínio de conhecer camadas externas")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Camada de aplicação (casos de uso) não deve depender de adapters")
    void applicationMustNotDependOnAdapters() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..adapter.."
                )
                .because("Casos de uso devem interagir com adaptadores exclusivamente através de portas do domínio")
                .check(importedClasses);
    }
}
