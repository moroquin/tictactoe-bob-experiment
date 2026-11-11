package enrollment.console;

import enrollment.domain.RuleViolation;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for InstructionParser (R33–R37, R40). */
class InstructionParserTest {

    private final InstructionParser parser = new InstructionParser();

    @Test
    void R37_blank_line_returns_empty() {
        assertTrue(parser.parse("").isEmpty());
        assertTrue(parser.parse("   ").isEmpty());
    }

    @Test
    void R33_name_parsed_correctly() {
        var p = parser.parse("Login(admin, admin);").orElseThrow();
        assertEquals("Login", p.name());
    }

    @Test
    void R33_spaces_before_parenthesis_allowed() {
        var p = parser.parse("Classroom (G, 12, 75);").orElseThrow();
        assertEquals("Classroom", p.name());
        assertEquals(3, p.values().size());
    }

    @Test
    void R40_missing_semicolon_is_invalid() {
        assertThrows(RuleViolation.class, () -> parser.parse("Login(admin, admin)"));
    }

    @Test
    void R40_missing_open_paren_is_invalid() {
        assertThrows(RuleViolation.class, () -> parser.parse("Login admin, admin);"));
    }

    @Test
    void R40_missing_close_paren_is_invalid() {
        assertThrows(RuleViolation.class, () -> parser.parse("Login(admin, admin;"));
    }

    @Test
    void R35_quoted_value_with_spaces() {
        var p = parser.parse("Student(200, \"Fernando Marroquín\", Quetzaltenango);").orElseThrow();
        assertEquals("200", p.values().get(0));
        assertEquals("Fernando Marroquín", p.values().get(1));
        assertEquals("Quetzaltenango", p.values().get(2));
    }

    @Test
    void R35_typographic_quotes() {
        var p = parser.parse("Building(\u201CG\u201D);").orElseThrow();
        assertEquals("G", p.values().get(0));
    }

    @Test
    void R35_unquoted_values_stripped() {
        var p = parser.parse("Course ( 777, compilers1, 5, 4 );").orElseThrow();
        assertEquals("777", p.values().get(0));
        assertEquals("compilers1", p.values().get(1));
        assertEquals("5", p.values().get(2));
        assertEquals("4", p.values().get(3));
    }

    @Test
    void R40_unclosed_quote_is_invalid() {
        assertThrows(RuleViolation.class, () -> parser.parse("Building(\"G);"));
    }

    @Test
    void zero_args_instruction() {
        var p = parser.parse("Logout();").orElseThrow();
        assertEquals("Logout", p.name());
        assertEquals(0, p.values().size());
    }

    @Test
    void R34_parseNumber_valid() {
        assertEquals(123L, Dispatcher.parseNumber("123"));
        assertEquals(0L, Dispatcher.parseNumber("0"));
    }

    @Test
    void R34_parseNumber_letters_invalid() {
        assertThrows(RuleViolation.class, () -> Dispatcher.parseNumber("abc"));
    }

    @Test
    void R34_parseNumber_more_than_10_digits_invalid() {
        assertThrows(RuleViolation.class, () -> Dispatcher.parseNumber("12345678901"));
    }
}
