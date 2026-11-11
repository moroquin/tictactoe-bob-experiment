package enrollment;

import enrollment.model.UserType;
import enrollment.service.EnrollmentService;
import enrollment.service.Result;
import enrollment.store.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for REQUIREMENTS.md rules.
 * Tests work directly against EnrollmentService (not the console).
 */
class EnrollmentServiceTest {

    private Store store;
    private EnrollmentService svc;

    @BeforeEach
    void setUp() {
        store = new Store();
        svc = new EnrollmentService(store);
        // log in as admin for most tests
        svc.login("admin", "admin");
    }

    // ────────────────────────────────────────────── R5 initial admin ──

    @Test
    void r5_initial_admin_exists() {
        assertNotNull(store.getUserById(1));
        assertEquals("admin", store.getUserById(1).getName());
        assertEquals(UserType.ADMIN, store.getUserById(1).getType());
    }

    // ────────────────────────────────────────────── R6 session ──

    @Test
    void r6_login_while_session_open_errors() {
        Result r = svc.login("admin", "admin");
        assertFalse(r.ok());
    }

    @Test
    void r6_login_wrong_password_errors() {
        svc.logout();
        Result r = svc.login("admin", "wrong");
        assertFalse(r.ok());
    }

    @Test
    void r6_login_unknown_user_errors() {
        svc.logout();
        assertFalse(svc.login("nobody", "x").ok());
    }

    // ────────────────────────────────────────────── R11 user ID ──

    @Test
    void r11_user_ids_assigned_sequentially() {
        svc.createStudent(1L, "s1", "addr");
        Result r1 = svc.createUser("u1", "pw", UserType.STAFF, null);
        Result r2 = svc.createUser("u2", "pw", UserType.STAFF, null);
        assertTrue(r1.ok());
        assertTrue(r2.ok());
        // IDs should be 2 and 3 (1 is the admin)
        assertNotNull(store.getUserByName("u1"));
        assertEquals(2, store.getUserByName("u1").getId());
        assertEquals(3, store.getUserByName("u2").getId());
    }

    @Test
    void r11_deleted_id_not_reused() {
        svc.createStudent(1L, "s1", "addr");
        svc.createUser("u1", "pw", UserType.STAFF, null);
        svc.deleteUser(2);
        svc.createUser("u2", "pw", UserType.STAFF, null);
        // new user gets id 3, not 2
        assertEquals(3, store.getUserByName("u2").getId());
    }

    // ────────────────────────────────────────────── R12 unique name ──

    @Test
    void r12_duplicate_user_name_rejected() {
        svc.createUser("alice", "pw", UserType.STAFF, null);
        assertFalse(svc.createUser("alice", "pw2", UserType.STAFF, null).ok());
    }

    // ────────────────────────────────────────────── R13 student user ──

    @Test
    void r13_student_user_requires_student_number() {
        assertFalse(svc.createUser("u", "pw", UserType.STUDENT, null).ok());
    }

    @Test
    void r13_student_must_exist() {
        assertFalse(svc.createUser("u", "pw", UserType.STUDENT, 999L).ok());
    }

    @Test
    void r13_student_can_have_at_most_one_user() {
        svc.createStudent(100L, "stu", "addr");
        svc.createUser("u1", "pw", UserType.STUDENT, 100L);
        assertFalse(svc.createUser("u2", "pw", UserType.STUDENT, 100L).ok());
    }

    @Test
    void r13_student_number_not_allowed_for_non_student() {
        svc.createStudent(100L, "stu", "addr");
        assertFalse(svc.createUser("u", "pw", UserType.ADMIN, 100L).ok());
        assertFalse(svc.createUser("u", "pw", UserType.STAFF, 100L).ok());
    }

    // ────────────────────────────────────────────── R14 cannot delete session user ──

    @Test
    void r14_cannot_delete_session_user() {
        // admin is logged in; cannot delete self
        assertFalse(svc.deleteUser(1).ok());
    }

    // ────────────────────────────────────────────── R5 admin cannot be deleted ──

    @Test
    void r5_admin_cannot_be_deleted() {
        // Login as admin; delete should fail
        assertFalse(svc.deleteUser(1).ok());
    }

    @Test
    void r5_admin_type_cannot_change() {
        assertFalse(svc.modifyUser(1, "admin", "admin", UserType.STAFF, null).ok());
    }

    // ────────────────────────────────────────────── R9 keys unique ──

    @Test
    void r9_duplicate_building_name_rejected() {
        svc.createBuilding("G");
        assertFalse(svc.createBuilding("G").ok());
    }

    @Test
    void r9_duplicate_course_code_rejected() {
        svc.createCourse(1L, "c1", 1, 3);
        assertFalse(svc.createCourse(1L, "c2", 2, 3).ok());
    }

    @Test
    void r9_duplicate_student_number_rejected() {
        svc.createStudent(100L, "s1", "addr");
        assertFalse(svc.createStudent(100L, "s2", "addr").ok());
    }

    @Test
    void r9_duplicate_professor_id_rejected() {
        svc.createProfessor(1L, "p1", "addr");
        assertFalse(svc.createProfessor(1L, "p2", "addr").ok());
    }

    @Test
    void r9_duplicate_section_code_rejected() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        assertFalse(svc.createSection(1L, "8:00am-9:00am", "tuesday", 10L, 5L, "G", 100L).ok());
    }

    @Test
    void r9_duplicate_classroom_number_in_same_building_rejected() {
        svc.createBuilding("G");
        svc.createClassroom("G", 5L, 30);
        assertFalse(svc.createClassroom("G", 5L, 40).ok());
    }

    // ────────────────────────────────────────────── R16 classroom in different buildings ──

    @Test
    void r16_same_classroom_number_in_different_buildings_ok() {
        svc.createBuilding("G");
        svc.createBuilding("D");
        assertTrue(svc.createClassroom("G", 5L, 30).ok());
        assertTrue(svc.createClassroom("D", 5L, 30).ok());
    }

    // ────────────────────────────────────────────── R17 classroom capacity ──

    @Test
    void r17_capacity_cannot_fall_below_enrolled() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        svc.createStudent(200L, "stu2", "addr");
        svc.enroll(200L, 10L, 0, 0);
        // classroom has capacity 30, 1 enrolled; lower to 0 must fail
        assertFalse(svc.modifyClassroom("G", 5L, 0).ok());
    }

    @Test
    void r17_capacity_can_equal_enrolled() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        svc.createStudent(200L, "stu2", "addr");
        svc.enroll(200L, 10L, 0, 0);
        // 1 student enrolled; setting capacity to 1 must succeed
        assertTrue(svc.modifyClassroom("G", 5L, 1).ok());
    }

    // ────────────────────────────────────────────── R18 building rename ──

    @Test
    void r18_rename_building_to_existing_name_fails() {
        svc.createBuilding("G");
        svc.createBuilding("D");
        assertFalse(svc.modifyBuilding("G", "D").ok());
    }

    @Test
    void r18_rename_building_to_same_name_ok() {
        svc.createBuilding("G");
        assertTrue(svc.modifyBuilding("G", "G").ok());
    }

    // ────────────────────────────────────────────── R19 delete building/classroom ──

    @Test
    void r19_cannot_delete_building_with_classrooms() {
        svc.createBuilding("G");
        svc.createClassroom("G", 1L, 20);
        assertFalse(svc.deleteBuilding("G").ok());
    }

    @Test
    void r19_cannot_delete_classroom_used_by_section() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        assertFalse(svc.deleteClassroom("G", 5L).ok());
    }

    // ────────────────────────────────────────────── R21 delete course ──

    @Test
    void r21_cannot_delete_course_with_sections() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        assertFalse(svc.deleteCourse(10L).ok());
    }

    // ────────────────────────────────────────────── R22 delete student ──

    @Test
    void r22_cannot_delete_student_with_enrollment() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        // student 1 was created in setupMinimalSectionData
        svc.enroll(1L, 10L, 0, 0);
        assertFalse(svc.deleteStudent(1L).ok());
    }

    @Test
    void r22_cannot_delete_student_with_linked_user() {
        svc.createStudent(1L, "s1", "addr");
        svc.createUser("su", "pw", UserType.STUDENT, 1L);
        assertFalse(svc.deleteStudent(1L).ok());
    }

    // ────────────────────────────────────────────── R23 delete professor ──

    @Test
    void r23_cannot_delete_professor_used_by_section() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        assertFalse(svc.deleteProfessor(100L).ok());
    }

    // ────────────────────────────────────────────── R24 period format ──

    @Test
    void r24_valid_period_accepted() {
        setupMinimalSectionData();
        assertTrue(svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L).ok());
    }

    @Test
    void r24_period_case_insensitive_stored_lowercase() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10AM-8:00AM", "monday", 10L, 5L, "G", 100L);
        assertEquals("7:10am-8:00am", store.getSectionByCode(1L).getPeriod());
    }

    @Test
    void r24_period_start_not_before_end_fails() {
        setupMinimalSectionData();
        // 8:00am-7:10am  — end before start
        assertFalse(svc.createSection(1L, "8:00am-7:10am", "monday", 10L, 5L, "G", 100L).ok());
    }

    @Test
    void r24_invalid_period_format_fails() {
        setupMinimalSectionData();
        assertFalse(svc.createSection(1L, "7:10-8:00am", "monday", 10L, 5L, "G", 100L).ok());
    }

    @Test
    void r24_noon_and_midnight() {
        setupMinimalSectionData();
        // 12:00pm (noon) to 12:00am would be midnight next day; noon < midnight?
        // Actually 12:00pm=720, 12:00am=0 → 720>0 so NOT valid (start >= end)
        assertFalse(svc.createSection(1L, "12:00pm-12:00am", "monday", 10L, 5L, "G", 100L).ok());
        // midnight (12:00am=0) to noon (12:00pm=720) is valid
        assertTrue(svc.createSection(1L, "12:00am-12:00pm", "monday", 10L, 5L, "G", 100L).ok());
    }

    // ────────────────────────────────────────────── R25 day ──

    @Test
    void r25_valid_days_accepted() {
        for (String day : new String[]{"monday","tuesday","wednesday","thursday","friday","saturday","sunday"}) {
            Store s2 = new Store();
            EnrollmentService sv = new EnrollmentService(s2);
            sv.login("admin", "admin");
            sv.createBuilding("G");
            sv.createClassroom("G", 5L, 30);
            sv.createCourse(10L, "c", 1, 3);
            sv.createStudent(1L, "st", "a");
            sv.createProfessor(100L, "p", "a");
            sv.createSection(1L, "7:10am-8:00am", day, 10L, 5L, "G", 100L);
            assertNotNull(s2.getSectionByCode(1L), "Should accept day: " + day);
            assertEquals(day.toLowerCase(), s2.getSectionByCode(1L).getDay());
        }
    }

    @Test
    void r25_invalid_day_rejected() {
        setupMinimalSectionData();
        assertFalse(svc.createSection(1L, "7:10am-8:00am", "funday", 10L, 5L, "G", 100L).ok());
    }

    // ────────────────────────────────────────────── R26 section references ──

    @Test
    void r26_section_course_must_exist() {
        setupMinimalSectionData();
        assertFalse(svc.createSection(1L, "7:10am-8:00am", "monday", 999L, 5L, "G", 100L).ok());
    }

    @Test
    void r26_section_building_must_exist() {
        setupMinimalSectionData();
        assertFalse(svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "X", 100L).ok());
    }

    @Test
    void r26_section_classroom_must_exist() {
        setupMinimalSectionData();
        assertFalse(svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 999L, "G", 100L).ok());
    }

    @Test
    void r26_section_professor_must_exist() {
        setupMinimalSectionData();
        assertFalse(svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 999L).ok());
    }

    @Test
    void r26_section_shows_modified_data() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        // rename building
        svc.modifyBuilding("G", "H");
        Result r = svc.querySection(1L);
        assertTrue(r.message().contains("| H |"), "Section should show new building name");
    }

    // ────────────────────────────────────────────── R27 course multiple sections ──

    @Test
    void r27_course_can_have_multiple_sections() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        svc.createSection(2L, "8:00am-9:00am", "tuesday", 10L, 5L, "G", 100L);
        assertNotNull(store.getSectionByCode(1L));
        assertNotNull(store.getSectionByCode(2L));
    }

    // ────────────────────────────────────────────── R28 grades ──

    @Test
    void r28_coursework_out_of_range_fails() {
        setupEnrollmentScenario();
        assertFalse(svc.enroll(50L, 10L, 76, 0).ok());
        assertFalse(svc.enroll(50L, 10L, -1, 0).ok());
    }

    @Test
    void r28_final_exam_out_of_range_fails() {
        setupEnrollmentScenario();
        assertFalse(svc.enroll(50L, 10L, 0, 26).ok());
        assertFalse(svc.enroll(50L, 10L, 0, -1).ok());
    }

    @Test
    void r28_grade_is_sum_and_pass_at_61() {
        setupEnrollmentScenario();
        svc.enroll(50L, 10L, 36, 25);
        var enrollment = store.findEnrollment(50L, 10L);
        assertNotNull(enrollment);
        assertEquals(61, enrollment.getGrade());
        assertTrue(enrollment.passes());
    }

    @Test
    void r28_grade_below_61_fails() {
        setupEnrollmentScenario();
        svc.enroll(50L, 10L, 36, 24);
        assertFalse(store.findEnrollment(50L, 10L).passes());
    }

    // ────────────────────────────────────────────── R29 section selection ──

    @Test
    void r29_student_goes_into_section_with_lowest_code_with_room() {
        setupMinimalSectionData();
        // sections 2 (lower code) and 5 for course 10
        svc.createSection(2L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        svc.createSection(5L, "8:00am-9:00am", "tuesday", 10L, 5L, "G", 100L);
        svc.createStudent(50L, "stu", "addr");
        svc.enroll(50L, 10L, 0, 0);
        var e = store.findEnrollment(50L, 10L);
        assertEquals(2L, e.getSection().getCode());
    }

    @Test
    void r29_fills_next_available_when_first_full() {
        setupMinimalSectionData(); // classroom capacity 30
        // capacity 1 classroom
        Store s2 = new Store();
        EnrollmentService sv = new EnrollmentService(s2);
        sv.login("admin", "admin");
        sv.createBuilding("B");
        sv.createClassroom("B", 1L, 1); // capacity 1
        sv.createCourse(10L, "c", 1, 3);
        sv.createProfessor(100L, "p", "addr");
        sv.createSection(1L, "7:10am-8:00am", "monday", 10L, 1L, "B", 100L);
        sv.createSection(2L, "8:00am-9:00am", "tuesday", 10L, 1L, "B", 100L);
        sv.createStudent(1L, "s1", "a");
        sv.createStudent(2L, "s2", "a");
        sv.enroll(1L, 10L, 0, 0);
        sv.enroll(2L, 10L, 0, 0);
        // s1 → section 1, s2 → section 2
        assertEquals(1L, s2.findEnrollment(1L, 10L).getSection().getCode());
        assertEquals(2L, s2.findEnrollment(2L, 10L).getSection().getCode());
    }

    @Test
    void r29_enrollment_rejected_when_no_room() {
        Store s2 = new Store();
        EnrollmentService sv = new EnrollmentService(s2);
        sv.login("admin", "admin");
        sv.createBuilding("B");
        sv.createClassroom("B", 1L, 1);
        sv.createCourse(10L, "c", 1, 3);
        sv.createProfessor(100L, "p", "a");
        sv.createSection(1L, "7:10am-8:00am", "monday", 10L, 1L, "B", 100L);
        sv.createStudent(1L, "s1", "a");
        sv.createStudent(2L, "s2", "a");
        sv.enroll(1L, 10L, 0, 0);
        assertFalse(sv.enroll(2L, 10L, 0, 0).ok());
    }

    @Test
    void r29_enrollment_rejected_when_no_sections() {
        svc.createCourse(10L, "c", 1, 3);
        svc.createStudent(1L, "s", "a");
        assertFalse(svc.enroll(1L, 10L, 0, 0).ok());
    }

    // ────────────────────────────────────────────── R30 one enrollment per student per course ──

    @Test
    void r30_student_cannot_enroll_twice_in_same_course() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        svc.createStudent(50L, "stu", "addr");
        svc.enroll(50L, 10L, 0, 0);
        assertFalse(svc.enroll(50L, 10L, 0, 0).ok());
    }

    // ────────────────────────────────────────────── R31 enroll me ──

    @Test
    void r31_enroll_me_starts_with_zero_grades() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        svc.createStudent(50L, "stu", "addr");
        svc.createUser("stuUser", "pw", UserType.STUDENT, 50L);
        svc.logout();
        svc.login("stuUser", "pw");
        svc.enrollMe(10L);
        var e = store.findEnrollment(50L, 10L);
        assertNotNull(e);
        assertEquals(0, e.getCoursework());
        assertEquals(0, e.getFinalExam());
    }

    // ────────────────────────────────────────────── R32 grades and unenroll ──

    @Test
    void r32_change_grades() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        svc.createStudent(50L, "stu", "addr");
        svc.enroll(50L, 10L, 0, 0);
        assertTrue(svc.changeGrades(50L, 10L, 60, 20).ok());
        var e = store.findEnrollment(50L, 10L);
        assertEquals(60, e.getCoursework());
        assertEquals(20, e.getFinalExam());
    }

    @Test
    void r32_unenroll_removes_enrollment() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        svc.createStudent(50L, "stu", "addr");
        svc.enroll(50L, 10L, 0, 0);
        assertTrue(svc.unenroll(50L, 10L).ok());
        assertNull(store.findEnrollment(50L, 10L));
    }

    // ────────────────────────────────────────────── example from section 5 ──

    @Test
    void section5_example_scenario() {
        // Set up the data from the example
        svc.createStudent(200145785L, "Fernando Marroquín", "Quetzaltenango");
        svc.createStudent(200745781L, "Alfonso Antonio", "Cantel");
        svc.createStudent(200478542L, "Mario Morales", "La Esperanza");
        svc.createUser("Secretary", "murphy1", UserType.STAFF, null);
        svc.createUser("Control", "control123", UserType.STAFF, null);
        svc.createUser("FMarroquín", "fffmmm", UserType.STUDENT, 200145785L);
        svc.createProfessor(12345L, "Prof1", "City");
        svc.createProfessor(47854L, "Prof2", "City");
        svc.createBuilding("G");
        svc.createBuilding("D");
        svc.createClassroom("G", 12L, 75);
        svc.createClassroom("G", 105L, 60);
        svc.createClassroom("G", 109L, 40);
        svc.createClassroom("G", 110L, 35);
        svc.createCourse(777L, "compilers1", 5, 4);
        svc.createCourse(771L, "programming2", 4, 4);
        svc.createSection(1L, "7:10am-8:00am", "monday", 777L, 105L, "G", 12345L);
        svc.createSection(2L, "8:00am-8:50am", "monday", 777L, 105L, "G", 12345L);
        svc.createSection(5L, "7:10am-8:00am", "tuesday", 771L, 109L, "G", 47854L);
        svc.enroll(200145785L, 777L, 30, 25);
        svc.enroll(200745781L, 777L, 60, 10);
        svc.enroll(200478542L, 771L, 70, 20);

        // Both students of course 777 are in section 1
        assertEquals(1L, store.findEnrollment(200145785L, 777L).getSection().getCode());
        assertEquals(1L, store.findEnrollment(200745781L, 777L).getSection().getCode());
        assertEquals(5L, store.findEnrollment(200478542L, 771L).getSection().getCode());

        // QuerySection(1) → "1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/60"
        Result qs = svc.querySection(1L);
        assertEquals("1 | 7:10am-8:00am | monday | 777 | 105 | G | 12345 | 2/60", qs.message());

        // QueryUser(4) → "4 | FMarroquín | student | 200145785"
        Result qu = svc.queryUser(4);
        assertEquals("4 | FMarroquín | student | 200145785", qu.message());

        // QueryBuilding(G) → "G | 12 (75), 105 (60), 109 (40), 110 (35)"
        Result qb = svc.queryBuilding("G");
        assertEquals("G | 12 (75), 105 (60), 109 (40), 110 (35)", qb.message());

        // QueryBuilding(D) → "D | no classrooms"
        Result qd = svc.queryBuilding("D");
        assertEquals("D | no classrooms", qd.message());

        // QueryCourse(777) → "777 | compilers1 | 5 | 4"
        Result qc = svc.queryCourse(777L);
        assertEquals("777 | compilers1 | 5 | 4", qc.message());

        // QueryStudent(200145785) → "200145785 | Fernando Marroquín | Quetzaltenango"
        Result qst = svc.queryStudent(200145785L);
        assertEquals("200145785 | Fernando Marroquín | Quetzaltenango", qst.message());

        // QueryProfessor(12345) → "12345 | Prof1 | City"
        Result qp = svc.queryProfessor(12345L);
        assertEquals("12345 | Prof1 | City", qp.message());

        // Now the continuation: enroll 200478542 in 777
        Result enrollR = svc.enroll(200478542L, 777L, 0, 0);
        assertTrue(enrollR.ok());
        assertEquals("200478542 enrolled in course 777 in section 1", enrollR.message());

        // Cannot delete course 777 (has sections)
        assertFalse(svc.deleteCourse(777L).ok());

        // Logout and try to query: needs session
        svc.logout();

        // Login as FMarroquín
        svc.login("FMarroquín", "fffmmm");

        // EnrollMe(771) → enroll student 200145785 in course 771
        Result enrollMe = svc.enrollMe(771L);
        assertTrue(enrollMe.ok());
        assertEquals("200145785 enrolled in course 771 in section 5", enrollMe.message());
    }

    // ────────────────────────────────────────────── query formats ──

    @Test
    void queryUser_admin_format() {
        Result r = svc.queryUser(1);
        assertTrue(r.ok());
        assertEquals("1 | admin | admin", r.message());
    }

    @Test
    void queryCourse_format() {
        svc.createCourse(42L, "calculus", 3, 5);
        assertEquals("42 | calculus | 3 | 5", svc.queryCourse(42L).message());
    }

    @Test
    void queryStudent_format() {
        svc.createStudent(12L, "John Doe", "NYC");
        assertEquals("12 | John Doe | NYC", svc.queryStudent(12L).message());
    }

    @Test
    void queryProfessor_format() {
        svc.createProfessor(7L, "Dr. Smith", "Boston");
        assertEquals("7 | Dr. Smith | Boston", svc.queryProfessor(7L).message());
    }

    @Test
    void queryBuilding_no_classrooms() {
        svc.createBuilding("X");
        assertEquals("X | no classrooms", svc.queryBuilding("X").message());
    }

    @Test
    void queryBuilding_with_classrooms_sorted_by_number() {
        svc.createBuilding("B");
        svc.createClassroom("B", 20L, 10);
        svc.createClassroom("B", 5L, 30);
        // should be sorted: 5 then 20
        assertEquals("B | 5 (30), 20 (10)", svc.queryBuilding("B").message());
    }

    // ────────────────────────────────────────────── R10 reference rules ──

    @Test
    void r10_section_referenced_classroom_cannot_be_deleted() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        assertFalse(svc.deleteClassroom("G", 5L).ok());
    }

    @Test
    void r10_section_referenced_professor_cannot_be_deleted() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        assertFalse(svc.deleteProfessor(100L).ok());
    }

    // ────────────────────────────────────────────── R20 modify course ──

    @Test
    void r20_modify_course_valid() {
        svc.createCourse(1L, "orig", 1, 1);
        assertTrue(svc.modifyCourse(1L, "new", 5, 4).ok());
        assertEquals("1 | new | 5 | 4", svc.queryCourse(1L).message());
    }

    @Test
    void r20_modify_course_semester_out_of_range() {
        svc.createCourse(1L, "c", 1, 1);
        assertFalse(svc.modifyCourse(1L, "c", 11, 1).ok());
        assertFalse(svc.modifyCourse(1L, "c", 0, 1).ok());
    }

    @Test
    void r20_modify_course_credits_out_of_range() {
        svc.createCourse(1L, "c", 1, 1);
        assertFalse(svc.modifyCourse(1L, "c", 1, 0).ok());
    }

    // ────────────────────────────────────────────── CR-1: ModifySection ──

    @Test
    void cr1_modifySection_changes_all_fields() {
        setupMinimalSectionData();
        svc.createCourse(20L, "c2", 2, 3);
        svc.createProfessor(200L, "p2", "addr");
        svc.createClassroom("G", 10L, 20);
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        Result r = svc.modifySection(1L, "8:00am-9:00am", "friday", 20L, 10L, "G", 200L);
        assertTrue(r.ok());
        Result qs = svc.querySection(1L);
        assertTrue(qs.ok());
        assertTrue(qs.message().contains("8:00am-9:00am"), "period should be updated");
        assertTrue(qs.message().contains("friday"), "day should be updated");
        assertTrue(qs.message().contains("| 20 |"), "course should be updated");
        assertTrue(qs.message().contains("| 10 |"), "classroom should be updated");
        assertTrue(qs.message().contains("| 200"), "professor should be updated");
    }

    @Test
    void cr1_modifySection_nonexistent_fails() {
        assertFalse(svc.modifySection(99L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L).ok());
    }

    @Test
    void cr1_modifySection_invalid_period_fails() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        assertFalse(svc.modifySection(1L, "7:10-8:00am", "monday", 10L, 5L, "G", 100L).ok());
    }

    @Test
    void cr1_modifySection_period_start_not_before_end_fails() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        assertFalse(svc.modifySection(1L, "8:00am-7:10am", "monday", 10L, 5L, "G", 100L).ok());
    }

    @Test
    void cr1_modifySection_invalid_day_fails() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        assertFalse(svc.modifySection(1L, "7:10am-8:00am", "funday", 10L, 5L, "G", 100L).ok());
    }

    @Test
    void cr1_modifySection_missing_course_fails() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        assertFalse(svc.modifySection(1L, "7:10am-8:00am", "monday", 999L, 5L, "G", 100L).ok());
    }

    @Test
    void cr1_modifySection_missing_building_fails() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        assertFalse(svc.modifySection(1L, "7:10am-8:00am", "monday", 10L, 5L, "X", 100L).ok());
    }

    @Test
    void cr1_modifySection_missing_classroom_fails() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        assertFalse(svc.modifySection(1L, "7:10am-8:00am", "monday", 10L, 999L, "G", 100L).ok());
    }

    @Test
    void cr1_modifySection_missing_professor_fails() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        assertFalse(svc.modifySection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 999L).ok());
    }

    @Test
    void cr1_modifySection_with_enrollments_cannot_change_course() {
        setupMinimalSectionData();
        svc.createCourse(20L, "c2", 2, 3);
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        svc.enroll(1L, 10L, 0, 0);
        assertFalse(svc.modifySection(1L, "7:10am-8:00am", "monday", 20L, 5L, "G", 100L).ok());
    }

    @Test
    void cr1_modifySection_with_enrollments_same_course_is_ok() {
        setupMinimalSectionData();
        svc.createClassroom("G", 10L, 5);
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        svc.enroll(1L, 10L, 0, 0);
        assertTrue(svc.modifySection(1L, "8:00am-9:00am", "tuesday", 10L, 10L, "G", 100L).ok());
    }

    @Test
    void cr1_modifySection_with_enrollments_classroom_too_small_fails() {
        setupMinimalSectionData();
        svc.createClassroom("G", 10L, 1); // capacity 1
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        // enroll 2 students
        svc.createStudent(50L, "s2", "addr");
        svc.enroll(1L, 10L, 0, 0);
        svc.enroll(50L, 10L, 0, 0);
        // new classroom has capacity 1, but 2 enrolled
        assertFalse(svc.modifySection(1L, "7:10am-8:00am", "monday", 10L, 10L, "G", 100L).ok());
    }

    @Test
    void cr1_modifySection_with_enrollments_classroom_exactly_equals_enrolled_ok() {
        setupMinimalSectionData();
        svc.createClassroom("G", 10L, 1); // capacity 1
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        svc.enroll(1L, 10L, 0, 0);
        // new classroom capacity == enrolled count (1)
        assertTrue(svc.modifySection(1L, "7:10am-8:00am", "monday", 10L, 10L, "G", 100L).ok());
    }

    @Test
    void cr1_modifySection_enrollments_stay_in_section() {
        setupMinimalSectionData();
        svc.createClassroom("G", 10L, 30);
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        svc.enroll(1L, 10L, 0, 0);
        svc.modifySection(1L, "8:00am-9:00am", "tuesday", 10L, 10L, "G", 100L);
        // enrollment still exists
        assertNotNull(store.findEnrollment(1L, 10L));
        assertEquals(1L, store.findEnrollment(1L, 10L).getSection().getCode());
    }

    @Test
    void cr1_modifySection_period_stored_lowercase() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        svc.modifySection(1L, "8:00AM-9:00AM", "FRIDAY", 10L, 5L, "G", 100L);
        assertEquals("8:00am-9:00am", store.getSectionByCode(1L).getPeriod());
        assertEquals("friday", store.getSectionByCode(1L).getDay());
    }

    // ────────────────────────────────────────────── CR-1: DeleteSection ──

    @Test
    void cr1_deleteSection_no_enrollments_succeeds() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        assertTrue(svc.deleteSection(1L).ok());
        assertNull(store.getSectionByCode(1L));
    }

    @Test
    void cr1_deleteSection_nonexistent_fails() {
        assertFalse(svc.deleteSection(99L).ok());
    }

    @Test
    void cr1_deleteSection_with_enrollments_fails() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        svc.enroll(1L, 10L, 0, 0);
        assertFalse(svc.deleteSection(1L).ok());
    }

    @Test
    void cr1_deleteSection_makes_course_deletable() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        assertFalse(svc.deleteCourse(10L).ok()); // has section
        svc.deleteSection(1L);
        assertTrue(svc.deleteCourse(10L).ok()); // now deletable
    }

    @Test
    void cr1_deleteSection_makes_classroom_deletable() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        assertFalse(svc.deleteClassroom("G", 5L).ok()); // used by section
        svc.deleteSection(1L);
        assertTrue(svc.deleteClassroom("G", 5L).ok()); // now deletable
    }

    @Test
    void cr1_deleteSection_makes_professor_deletable() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        assertFalse(svc.deleteProfessor(100L).ok()); // used by section
        svc.deleteSection(1L);
        assertTrue(svc.deleteProfessor(100L).ok()); // now deletable
    }

    // ────────────────────────────────────────────── CR-2 report: StudentCourses ──

    @Test
    void cr2_studentCourses_no_enrollments() {
        svc.createStudent(1L, "Alice", "City");
        Result r = svc.studentCourses(1L, null);
        assertTrue(r.ok());
        assertEquals("No courses", r.message());
    }

    @Test
    void cr2_studentCourses_student_not_found() {
        assertFalse(svc.studentCourses(999L, null).ok());
    }

    @Test
    void cr2_studentCourses_permission_denied_for_other_student() {
        svc.createStudent(1L, "Alice", "City");
        svc.createStudent(2L, "Bob", "Town");
        // student 1 requesting for student 2 → denied
        Result r = svc.studentCourses(2L, 1L);
        assertFalse(r.ok());
        assertEquals("permission denied", r.message());
    }

    @Test
    void cr2_studentCourses_student_can_query_own() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        svc.enroll(1L, 10L, 30, 20);
        Result r = svc.studentCourses(1L, 1L);
        assertTrue(r.ok());
        assertTrue(r.message().contains("section 1"));
        assertTrue(r.message().contains("coursework 30"));
        assertTrue(r.message().contains("final 20"));
    }

    @Test
    void cr2_studentCourses_sorted_by_section_code() {
        svc.createBuilding("G");
        svc.createClassroom("G", 5L, 30);
        svc.createCourse(10L, "c1", 1, 3);
        svc.createCourse(20L, "c2", 1, 3);
        svc.createStudent(1L, "st", "a");
        svc.createProfessor(100L, "p", "a");
        svc.createSection(3L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        svc.createSection(7L, "8:00am-9:00am", "tuesday", 20L, 5L, "G", 100L);
        svc.enroll(1L, 10L, 10, 5);
        svc.enroll(1L, 20L, 20, 10);
        Result r = svc.studentCourses(1L, null);
        assertTrue(r.ok());
        String[] lines = r.message().split("\n");
        assertEquals(2, lines.length);
        assertTrue(lines[0].contains("section 3"), "first line should be section 3");
        assertTrue(lines[1].contains("section 7"), "second line should be section 7");
    }

    @Test
    void cr2_studentCourses_passed_and_failed() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        svc.enroll(1L, 10L, 61, 0); // passes
        Result r = svc.studentCourses(1L, null);
        assertTrue(r.ok());
        assertTrue(r.message().contains("passed"));

        // reset enrollment and test failed
        svc.unenroll(1L, 10L);
        svc.enroll(1L, 10L, 60, 0); // fails
        r = svc.studentCourses(1L, null);
        assertTrue(r.ok());
        assertTrue(r.message().contains("failed"));
    }

    // ────────────────────────────────────────────── CR-2 report: CourseStudents ──

    @Test
    void cr2_courseStudents_course_not_found() {
        assertFalse(svc.courseStudents(999L).ok());
    }

    @Test
    void cr2_courseStudents_no_enrollments() {
        svc.createCourse(1L, "c", 1, 3);
        Result r = svc.courseStudents(1L);
        assertTrue(r.ok());
        assertEquals("No students", r.message());
    }

    @Test
    void cr2_courseStudents_lists_in_section_order_then_enrollment_order() {
        svc.createBuilding("G");
        svc.createClassroom("G", 5L, 2);
        svc.createCourse(10L, "c", 1, 3);
        svc.createStudent(1L, "Alice", "a");
        svc.createStudent(2L, "Bob", "b");
        svc.createStudent(3L, "Carol", "c");
        svc.createProfessor(100L, "p", "a");
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        svc.createSection(2L, "8:00am-9:00am", "tuesday", 10L, 5L, "G", 100L);
        // fill section 1 (capacity 2), third goes to section 2
        svc.enroll(1L, 10L, 0, 0); // section 1, first
        svc.enroll(2L, 10L, 0, 0); // section 1, second
        svc.enroll(3L, 10L, 0, 0); // section 2
        Result r = svc.courseStudents(10L);
        assertTrue(r.ok());
        String[] lines = r.message().split("\n");
        assertEquals(3, lines.length);
        assertTrue(lines[0].startsWith("1 |"));
        assertTrue(lines[1].startsWith("2 |"));
        assertTrue(lines[2].startsWith("3 |"));
        assertTrue(lines[0].contains("section 1"));
        assertTrue(lines[2].contains("section 2"));
    }

    // ────────────────────────────────────────────── CR-2 report: ClassroomSections ──

    @Test
    void cr2_classroomSections_building_not_found() {
        assertFalse(svc.classroomSections("Z", 1L).ok());
    }

    @Test
    void cr2_classroomSections_classroom_not_found() {
        svc.createBuilding("G");
        assertFalse(svc.classroomSections("G", 999L).ok());
    }

    @Test
    void cr2_classroomSections_no_sections() {
        svc.createBuilding("G");
        svc.createClassroom("G", 5L, 30);
        Result r = svc.classroomSections("G", 5L);
        assertTrue(r.ok());
        assertEquals("No sections", r.message());
    }

    @Test
    void cr2_classroomSections_sorted_by_section_code() {
        setupMinimalSectionData();
        svc.createProfessor(200L, "p2", "a");
        svc.createSection(3L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        svc.createSection(1L, "8:00am-9:00am", "tuesday", 10L, 5L, "G", 100L);
        Result r = svc.classroomSections("G", 5L);
        assertTrue(r.ok());
        String[] lines = r.message().split("\n");
        assertEquals(2, lines.length);
        assertTrue(lines[0].startsWith("section 1"), "first line should be section 1");
        assertTrue(lines[1].startsWith("section 3"), "second line should be section 3");
    }

    @Test
    void cr2_classroomSections_format() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        Result r = svc.classroomSections("G", 5L);
        assertTrue(r.ok());
        assertEquals("section 1 | monday | 7:10am-8:00am | 10 | c", r.message());
    }

    // ────────────────────────────────────────────── CR-2 report: PassedBySemester ──

    @Test
    void cr2_passedBySemester_no_courses() {
        Result r = svc.passedBySemester(3);
        assertTrue(r.ok());
        assertEquals("No courses in the semester", r.message());
    }

    @Test
    void cr2_passedBySemester_invalid_semester() {
        assertFalse(svc.passedBySemester(0).ok());
        assertFalse(svc.passedBySemester(11).ok());
    }

    @Test
    void cr2_passedBySemester_with_data() {
        setupMinimalSectionData(); // course 10, semester 1
        svc.createCourse(20L, "c2", 1, 3);
        svc.createClassroom("G", 9L, 30);
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        svc.createSection(2L, "8:00am-9:00am", "tuesday", 20L, 9L, "G", 100L);
        // student 1 enrolled in course 10 with passing grade
        svc.enroll(1L, 10L, 61, 0); // passes
        Result r = svc.passedBySemester(1);
        assertTrue(r.ok());
        String[] lines = r.message().split("\n");
        // course 10 (creation order first), course 20, then Total
        assertEquals(3, lines.length);
        assertTrue(lines[0].contains("10 | c | passed 1 | failed 0"));
        assertTrue(lines[1].contains("20 | c2 | passed 0 | failed 0"));
        assertTrue(lines[2].equals("Total | passed 1 | failed 0"));
    }

    @Test
    void cr2_passedBySemester_counts_per_course_not_global() {
        // student enrolled in two courses of same semester — counted separately in each
        svc.createBuilding("G");
        svc.createClassroom("G", 5L, 30);
        svc.createCourse(10L, "c1", 2, 3);
        svc.createCourse(20L, "c2", 2, 3);
        svc.createStudent(1L, "stu", "addr");
        svc.createProfessor(100L, "p", "a");
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        svc.createSection(2L, "8:00am-9:00am", "tuesday", 20L, 5L, "G", 100L);
        svc.enroll(1L, 10L, 61, 0); // passes c1
        svc.enroll(1L, 20L, 30, 0); // fails c2
        Result r = svc.passedBySemester(2);
        assertTrue(r.ok());
        String[] lines = r.message().split("\n");
        assertEquals(3, lines.length);
        assertTrue(lines[0].contains("passed 1 | failed 0"), "c1: 1 passed");
        assertTrue(lines[1].contains("passed 0 | failed 1"), "c2: 1 failed");
        assertTrue(lines[2].equals("Total | passed 1 | failed 1"));
    }

    // ────────────────────────────────────────────── helpers ──

    /** Creates building G with classroom 5 (capacity 30), course 10, professor 100, student 1. */
    private void setupMinimalSectionData() {
        svc.createBuilding("G");
        svc.createClassroom("G", 5L, 30);
        svc.createCourse(10L, "c", 1, 3);
        svc.createStudent(1L, "st", "a");
        svc.createProfessor(100L, "p", "a");
    }

    /** Builds on setupMinimalSectionData + 1 section + student 50. */
    private void setupEnrollmentScenario() {
        setupMinimalSectionData();
        svc.createSection(1L, "7:10am-8:00am", "monday", 10L, 5L, "G", 100L);
        svc.createStudent(50L, "stu", "addr");
    }
}
