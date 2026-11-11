package enrollment;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.ACCESS_STANDARD_STREAMS;

/** Executable version of ARCHITECTURE.md. */
@AnalyzeClasses(packages = "enrollment", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    @ArchTest
    static final ArchRule only_the_console_writes =
            noClasses().that().resideOutsideOfPackages("enrollment.console..")
                    .and().doNotHaveFullyQualifiedName("enrollment.Main")
                    .should(ACCESS_STANDARD_STREAMS)
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule only_the_console_reads =
            noClasses().that().resideOutsideOfPackages("enrollment.console..")
                    .and().doNotHaveFullyQualifiedName("enrollment.Main")
                    .should().accessField(System.class, "in")
                    .orShould().dependOnClassesThat().belongToAnyOf(java.util.Scanner.class, java.io.Console.class)
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule nothing_depends_on_the_console =
            noClasses().that().resideOutsideOfPackages("enrollment.console..")
                    .and().doNotHaveFullyQualifiedName("enrollment.Main")
                    .should().dependOnClassesThat().resideInAPackage("enrollment.console..")
                    .allowEmptyShould(true);
}
