package tictactoe;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.ACCESS_STANDARD_STREAMS;

/** Executable version of docs/domain/modules.md. */
@AnalyzeClasses(packages = "tictactoe", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    @ArchTest
    static final ArchRule domain_does_not_depend_on_the_console =
            noClasses().that().resideInAPackage("tictactoe.domain..")
                    .should().dependOnClassesThat().resideInAPackage("tictactoe.console..")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule domain_does_not_print =
            noClasses().that().resideInAPackage("tictactoe.domain..")
                    .should(ACCESS_STANDARD_STREAMS)
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule domain_does_not_read_input =
            noClasses().that().resideInAPackage("tictactoe.domain..")
                    .should().accessField(System.class, "in")
                    .orShould().dependOnClassesThat().belongToAnyOf(java.util.Scanner.class, java.io.Console.class)
                    .allowEmptyShould(true);
}
