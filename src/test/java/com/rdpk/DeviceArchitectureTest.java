package com.rdpk;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

@AnalyzeClasses(packages = "com.rdpk", 
    importOptions = ImportOption.DoNotIncludeTests.class)
class DeviceArchitectureTest {

    @ArchTest
    static final ArchRule controllers_should_be_annotated_with_restcontroller =
            classes()
                    .that().resideInAPackage("..controller..")
                    .should().beAnnotatedWith(RestController.class);

    @ArchTest
    static final ArchRule services_should_be_annotated_with_service =
            classes()
                    .that().resideInAPackage("..service..")
                    .should().beAnnotatedWith(Service.class);

    @ArchTest
    static final ArchRule repository_interfaces_should_extend_reactive_crud_repository =
            classes()
                    .that().resideInAPackage("..repository..")
                    .and().areInterfaces()
                    .and().haveSimpleNameEndingWith("Repository")
                    .should().beAssignableTo(org.springframework.data.repository.reactive.ReactiveCrudRepository.class);

    @ArchTest
    static final ArchRule controllers_should_only_depend_on_services =
            noClasses()
                    .that().resideInAPackage("..controller..")
                    .should().dependOnClassesThat().resideInAPackage("..repository..");

    @ArchTest
    static final ArchRule services_should_not_depend_on_controllers =
            noClasses()
                    .that().resideInAPackage("..service..")
                    .should().dependOnClassesThat().resideInAPackage("..controller..");

    @ArchTest
    static final ArchRule services_should_not_depend_on_dtos =
            noClasses()
                    .that().resideInAPackage("..service..")
                    .should().dependOnClassesThat().resideInAPackage("..dto..");

    @ArchTest
    static final ArchRule repositories_should_not_depend_on_services_or_controllers =
            noClasses()
                    .that().resideInAPackage("..repository..")
                    .should().dependOnClassesThat().resideInAnyPackage("..service..", "..controller..");

    @ArchTest
    static final ArchRule domain_should_not_depend_on_spring =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().belongToAnyOf(
                            org.springframework.web.bind.annotation.RestController.class,
                            org.springframework.stereotype.Service.class,
                            org.springframework.stereotype.Repository.class
                    );

    @ArchTest
    static final ArchRule controllers_should_have_controller_suffix =
            classes()
                    .that().resideInAPackage("..controller..")
                    .and().areAnnotatedWith(RestController.class)
                    .should().haveSimpleNameEndingWith("Controller");

    @ArchTest
    static final ArchRule services_should_have_service_suffix =
            classes()
                    .that().resideInAPackage("..service..")
                    .and().areAnnotatedWith(Service.class)
                    .should().haveSimpleNameEndingWith("Service");

    @ArchTest
    static final ArchRule repositories_should_have_repository_suffix =
            classes()
                    .that().resideInAPackage("..repository..")
                    .and().areInterfaces()
                    .should().haveSimpleNameEndingWith("Repository");

    @ArchTest
    static final ArchRule domain_should_not_have_spring_annotations =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().beAnnotatedWith(RestController.class)
                    .orShould().beAnnotatedWith(Service.class)
                    .orShould().beAnnotatedWith(Repository.class);

    @ArchTest
    static final ArchRule dto_should_not_have_spring_annotations =
            noClasses()
                    .that().resideInAPackage("..dto..")
                    .should().beAnnotatedWith(RestController.class)
                    .orShould().beAnnotatedWith(Service.class)
                    .orShould().beAnnotatedWith(Repository.class);

    @ArchTest
    static final ArchRule exception_should_not_have_spring_annotations =
            noClasses()
                    .that().resideInAPackage("..exception..")
                    .should().beAnnotatedWith(RestController.class)
                    .orShould().beAnnotatedWith(Service.class)
                    .orShould().beAnnotatedWith(Repository.class);
}

