package enrollment;

import enrollment.application.*;
import enrollment.console.*;
import enrollment.domain.access.*;
import enrollment.domain.people.StudentNumber;
import enrollment.domain.sections.*;
import enrollment.infrastructure.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Entry point: wires up all objects and starts the console (R44).
 */
public class Main {

    public static void main(String[] args) throws IOException {
        // Infrastructure
        var userRepo     = new InMemoryUserRepository();
        var buildingRepo = new InMemoryBuildingRepository();
        var courseRepo   = new InMemoryCourseRepository();
        var studentRepo  = new InMemoryStudentRepository();
        var professorRepo= new InMemoryProfessorRepository();
        var sectionRepo  = new InMemorySectionRepository();

        // Domain policies
        var session     = new Session();
        var permissions = new Permissions();
        var gradeScale  = new GradeScale();
        var choicePolicy= new SectionChoicePolicy();

        // Seed initial admin (I3, R5)
        var initialAdmin = new User(1L, "admin", "admin", UserType.ADMIN, null);
        userRepo.add(initialAdmin);

        // Application services
        var accessService  = new AccessService(session, userRepo, studentRepo, permissions);
        var campusService  = new CampusService(accessService, buildingRepo, sectionRepo);
        var catalogService = new CatalogService(accessService, courseRepo, sectionRepo);
        var peopleService  = new PeopleService(accessService, studentRepo, professorRepo,
                                               userRepo, sectionRepo);
        var sectionService = new SectionService(accessService, campusService, catalogService,
                                                peopleService, sectionRepo);
        var enrollmentService = new EnrollmentService(accessService, campusService, catalogService,
                                                      peopleService, sectionRepo, choicePolicy, gradeScale);

        // Console
        var parser    = new InstructionParser();
        var formatter = new ResponseFormatter(sectionService);
        var dispatcher= new Dispatcher(accessService, campusService, catalogService,
                                       peopleService, sectionService, enrollmentService, formatter);
        var console   = new ConsoleLoop(parser, dispatcher, System.out);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8));
        console.run(reader);
        System.exit(0);
    }
}
