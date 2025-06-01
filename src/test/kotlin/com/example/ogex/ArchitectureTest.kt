package com.example.ogex

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

class ArchitectureTest {

    private val classes: JavaClasses = ClassFileImporter().importPackages("com.example.ogex.apps")

    @Test
    fun `apps packages should not depend on each other, only on common`() {
        val apps = listOf("foo", "bar", "fooworker")
        for (app in apps) {
            val rule =
                    noClasses()
                            .that()
                            .resideInAPackage("com.example.ogex.apps.$app..")
                            .should()
                            .dependOnClassesThat()
                            .resideInAnyPackage(
                                    *apps
                                            .filter { it != app }
                                            .map { "com.example.ogex.apps.$it.." }
                                            .toTypedArray()
                            )
            rule.check(classes)
        }
    }
}
