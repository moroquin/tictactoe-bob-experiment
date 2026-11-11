package enrollment;

import enrollment.console.Console;
import enrollment.store.Store;
import enrollment.service.EnrollmentService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) {
        var store = new Store();
        var service = new EnrollmentService(store);
        var reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        var out = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
        var err = new PrintWriter(System.err, true, StandardCharsets.UTF_8);
        new Console(service, reader, out, err).run();
    }
}
