package enrollment;

import enrollment.console.Parser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the console parser (R33–R37, R40).
 */
class ParserTest {

    // ────────────────────────────────────────────── R37 blank lines ──

    @Test
    void r37_blank_line_returns_null() {
        assertNull(Parser.parse(""));
        assertNull(Parser.parse("   "));
    }

    // ────────────────────────────────────────────── R33 instruction name ──

    @Test
    void r33_name_case_insensitive() {
        var i = Parser.parse("LOGIN(admin, admin);");
        assertNotNull(i);
        assertEquals("login", i.name());
    }

    @Test
    void r33_spaces_before_paren() {
        var i = Parser.parse("Login  (admin, admin);");
        assertNotNull(i);
        assertEquals("login", i.name());
        assertEquals(2, i.args().size());
    }

    @Test
    void r33_missing_semicolon_throws() {
        assertThrows(Parser.ParseException.class, () -> Parser.parse("Login(admin, admin)"));
    }

    @Test
    void r33_missing_paren_throws() {
        assertThrows(Parser.ParseException.class, () -> Parser.parse("Login admin, admin;"));
    }

    @Test
    void r33_missing_close_paren_throws() {
        assertThrows(Parser.ParseException.class, () -> Parser.parse("Login(admin, admin;"));
    }

    // ────────────────────────────────────────────── R34 numbers ──

    @Test
    void r34_number_parsed_as_string_token() {
        var i = Parser.parse("DeleteUser(42);");
        assertEquals("42", i.args().get(0));
    }

    // ────────────────────────────────────────────── R35 quoted strings ──

    @Test
    void r35_straight_quotes() {
        var i = Parser.parse("Student(1, \"Fernando Marroquín\", \"Quetzaltenango\");");
        assertEquals(3, i.args().size());
        assertEquals("1", i.args().get(0));
        assertEquals("Fernando Marroquín", i.args().get(1));
        assertEquals("Quetzaltenango", i.args().get(2));
    }

    @Test
    void r35_typographic_quotes() {
        var i = Parser.parse("Building(\u201CG\u201D);");
        assertEquals(1, i.args().size());
        assertEquals("G", i.args().get(0));
    }

    @Test
    void r35_unquoted_simple_token() {
        var i = Parser.parse("Building(G);");
        assertEquals("G", i.args().get(0));
    }

    @Test
    void r35_spaces_around_values_stripped() {
        var i = Parser.parse("Login( admin , admin );");
        assertEquals("admin", i.args().get(0));
        assertEquals("admin", i.args().get(1));
    }

    @Test
    void r35_unclosed_quote_throws() {
        assertThrows(Parser.ParseException.class, () -> Parser.parse("Building(\"G);"));
    }

    // ────────────────────────────────────────────── zero-arg instruction ──

    @Test
    void zero_args_parsed() {
        var i = Parser.parse("Logout();");
        assertEquals("logout", i.name());
        assertEquals(0, i.args().size());
    }

    // ────────────────────────────────────────────── R40 invalid ──

    @Test
    void r40_empty_name_throws() {
        assertThrows(Parser.ParseException.class, () -> Parser.parse("(a, b);"));
    }
}
