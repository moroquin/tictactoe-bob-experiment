package enrollment.application;

import enrollment.domain.RuleViolation;
import enrollment.domain.access.*;
import enrollment.domain.campus.*;
import enrollment.domain.sections.SectionCode;
import enrollment.infrastructure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for CampusService (I1, I2, I11, I12). */
class CampusServiceTest {

    private CampusService campusService;
    private InMemoryBuildingRepository buildingRepo;
    private InMemorySectionRepository sectionRepo;

    @BeforeEach
    void setup() {
        var userRepo = new InMemoryUserRepository();
        var studentRepo = new InMemoryStudentRepository();
        var session = new Session();
        var permissions = new Permissions();
        var accessService = new AccessService(session, userRepo, studentRepo, permissions);
        userRepo.add(new User(1L, "admin", "admin", UserType.ADMIN, null));
        accessService.login("admin", "admin");

        buildingRepo = new InMemoryBuildingRepository();
        sectionRepo = new InMemorySectionRepository();
        campusService = new CampusService(accessService, buildingRepo, sectionRepo);
    }

    @Test
    void I1_duplicate_building_name_rejected() {
        campusService.createBuilding("G");
        assertThrows(RuleViolation.class, () -> campusService.createBuilding("G"));
    }

    @Test
    void I12_rename_to_existing_name_rejected() {
        campusService.createBuilding("G");
        campusService.createBuilding("D");
        assertThrows(RuleViolation.class, () -> campusService.modifyBuilding("G", "D"));
    }

    @Test
    void I2_cannot_delete_building_with_classrooms() {
        campusService.createBuilding("G");
        campusService.createClassroom("G", 101, 30);
        assertThrows(RuleViolation.class, () -> campusService.deleteBuilding("G"));
    }

    @Test
    void I11_capacity_must_be_positive() {
        campusService.createBuilding("G");
        assertThrows(RuleViolation.class, () -> campusService.createClassroom("G", 101, 0));
    }
}
