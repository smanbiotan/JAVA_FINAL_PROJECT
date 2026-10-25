package testing;

import model.*;
import service.*;
import repository.FileManager;
import exception.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Phase 5: all 34 cases in ONE file. No JUnit dependency.
 * Place at src/testing/TestClinic.java and run this class.
 * Every public launch creates a fresh temporary working directory and a child
 * JVM, so FileManager's relative filenames cannot overwrite your clinic files.
 * Do not manually use the internal --worker argument.
 * Expected on the revised application: 34 PASS.
 * Actual results are calculated; failures are never converted into passes.
 */
public class TestClinic {
    @FunctionalInterface
    interface Action { void run() throws Exception; }

    private static final List<String> RESULTS = new ArrayList<>();
    private static int passed = 0, failed = 0, blocked = 0;
    private static Path projectRoot;
    private static final String CLASS_PATH = classPath();
    private static final String JAVA = Path.of(System.getProperty("java.home"),
            "bin", System.getProperty("os.name").toLowerCase().contains("win")
            ? "java.exe" : "java").toString();

    private static class Blocked extends Exception {
        Blocked(String message) { super(message); }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 2 && "--worker".equals(args[0])) {
            projectRoot = Path.of(args[1]).toAbsolutePath();
            // The launcher alone creates this marker in a fresh temporary folder.
            if (!Files.isRegularFile(Path.of(".phase5-isolated"))) {
                throw new IllegalStateException("Worker requires an isolated test folder.");
            }
            runTests();
            return;
        }
        projectRoot = findProjectRoot(args.length > 0 ? Path.of(args[0]) : Path.of(""));
        Path isolated = Files.createTempDirectory("clinic-phase5-").toAbsolutePath();
        Files.writeString(isolated.resolve(".phase5-isolated"), "Temporary test data only");
        Path log = isolated.resolve("Phase5_Test_Output.txt");
        List<String> command = Arrays.asList(JAVA, "-cp", CLASS_PATH,
                "testing.TestClinic", "--worker", projectRoot.toString());
        Process process = new ProcessBuilder(command).directory(isolated.toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        if (!process.waitFor(180, TimeUnit.SECONDS)) {
            process.destroyForcibly().waitFor();
            System.out.println("RUN ERROR: test suite timed out after 180 seconds.");
        }
        System.out.print(Files.readString(log));
        System.out.println("\nSaved full output: " + log);
        System.out.println("Temporary test files: " + isolated);
        if (process.exitValue() != 0) {
            System.out.println("RUN ERROR: worker exit code " + process.exitValue()
                    + ". Do not treat an incomplete run as passing.");
        }
        System.out.println("Copy the full BEGIN PHASE 5 through END PHASE 5 block to ChatGPT.");
    }

    private static String classPath() {
        try {
            // Works for classpath runs AND VS Code named-module launches.
            Path own = Path.of(TestClinic.class.getProtectionDomain().getCodeSource()
                    .getLocation().toURI()).toAbsolutePath();
            return own.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot locate compiled project classes", e);
        }
    }

    private static Path findProjectRoot(Path start) {
        for (Path p = start.toAbsolutePath().normalize(); p != null; p = p.getParent()) {
            if (Files.isRegularFile(p.resolve("src/project/Main.java"))) return p;
        }
        // A missing source tree blocks T01; other cases still execute.
        return start.toAbsolutePath().normalize();
    }

    private static void check(String id, String expected, Action action) {
        System.out.println("\n============================================================");
        System.out.println("TEST " + id + " | " + (id.startsWith("G")
                ? "ROBUSTNESS REQUIREMENT" : "BASELINE"));
        System.out.println("EXPECTED: " + expected);
        System.out.println("------------------------------------------------------------");
        String status;
        String detail;
        try {
            action.run();
            status = "PASS";
            detail = "All assertions satisfied";
            passed++;
        } catch (Blocked e) {
            status = "BLOCKED";
            detail = e.getMessage();
            blocked++;
        } catch (Throwable e) {
            status = "FAIL";
            detail = e.getClass().getSimpleName() + ": " + e.getMessage();
            failed++;
        }
        String result = "RESULT|" + id + "|" + status + "|" + detail;
        RESULTS.add(result);
        System.out.println(result);
    }

    private static void ok(boolean condition) {
        ok(condition, "Expected condition was not satisfied");
    }
    private static void ok(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static void expect(Class<?> type, Action action) throws Exception {
        try { action.run(); }
        catch (Exception e) {
            if (type.isInstance(e)) {
                System.out.println("Observed expected " + type.getSimpleName() + ": " + e.getMessage());
                return;
            }
            throw e;
        }
        throw new AssertionError("Expected " + type.getSimpleName() + " but no exception was thrown");
    }
    private static Patient p() {
        return new Patient("P1", "Test Patient", "09000000000", "p@example.test", 20, "X", "Test Address");
    }
    private static Doctor bareDoctor() {
        return new Doctor("D1", "Test Doctor", "09000000001", "d@example.test", "Dermatology");
    }
    private static Doctor d() {
        Doctor doctor = bareDoctor();
        doctor.addSchedule(new Schedule("BASE", "D1", "2026-11-02", "09:00", "12:00"));
        doctor.addSchedule(new Schedule("ROUNDTRIP", "D1", "2026-11-03", "14:30"));
        return doctor;
    }
    private static AppointmentService a() throws Exception {
        AppointmentService service = new AppointmentService();
        service.bookAppointment("A1", p(), d(), "2026-11-02", "09:00", "Consultation");
        return service;
    }

    private static void compileApplication() throws Exception {
        Path src = projectRoot.resolve("src");
        if (!Files.isRegularFile(src.resolve("project/Main.java"))) {
            throw new Blocked("Cannot find src/project/Main.java. Launch from the project root or pass its path as an argument.");
        }
        List<String> command = new ArrayList<>(Arrays.asList(JAVA,
                "-m", "jdk.compiler/com.sun.tools.javac.Main", "-encoding", "UTF-8",
                "-d", Files.createDirectories(Path.of("compile-check")).toAbsolutePath().toString()));
        int sourceCount = 0;
        for (String folder : Arrays.asList("model", "service", "repository", "exception", "project")) {
            Path directory = src.resolve(folder);
            if (!Files.isDirectory(directory)) throw new Blocked("Missing source directory: " + directory);
            try (Stream<Path> files = Files.walk(directory)) {
                List<Path> javaFiles = files.filter(f -> f.toString().endsWith(".java")).sorted()
                        .collect(java.util.stream.Collectors.toList());
                for (Path file : javaFiles) command.add(file.toAbsolutePath().toString());
                sourceCount += javaFiles.size();
            }
        }
        Path log = Path.of("compile-output.txt").toAbsolutePath();
        Process process = new ProcessBuilder(command).redirectErrorStream(true)
                .redirectOutput(log.toFile()).start();
        if (!process.waitFor(60, TimeUnit.SECONDS)) {
            process.destroyForcibly().waitFor();
            throw new Blocked("Compiler timeout after 60 seconds");
        }
        String output = Files.readString(log);
        System.out.println("Application source files compiled: " + sourceCount);
        System.out.println("Compiler exit code: " + process.exitValue());
        if (!output.isBlank()) System.out.println(output);
        if (output.contains("Module jdk.compiler not found")) {
            throw new Blocked("The selected Java runtime lacks the JDK compiler. Run with a full JDK.");
        }
        ok(process.exitValue() == 0, "Application compilation failed; see compiler output above");
        System.out.println("Separate classpath compilation; module-info.java and test sources are excluded.");
    }

    private static void testConsole() throws Exception {
        Path ui = Files.createTempDirectory(Path.of("").toAbsolutePath(), "ui-");
        boolean copies = true;
        for (String file : Arrays.asList("patients.txt", "doctors.txt", "appointments.txt", "schedules.txt")) {
            Path original = projectRoot.resolve(file);
            if (Files.isRegularFile(original)) Files.copy(original, ui.resolve(file));
            else copies = false;
        }
        if (!copies) {
            // Use a coherent synthetic dataset if any source data file is absent.
            Files.writeString(ui.resolve("patients.txt"), "P1|Test Patient|000|p@example.test|20|X|Test Address\n");
            Files.writeString(ui.resolve("doctors.txt"), "D1|Test Doctor|000|d@example.test|Dermatology\n");
            Files.writeString(ui.resolve("appointments.txt"), "");
            Files.writeString(ui.resolve("schedules.txt"), "");
        }
        System.out.println("UI fixture: " + (copies ? "copies of project data files" : "synthetic data (project data files incomplete)"));
        Path log = ui.resolve("ui-output.txt");
        Process process = new ProcessBuilder(JAVA, "-cp", CLASS_PATH, "project.Main")
                .directory(ui.toFile()).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try (OutputStream input = process.getOutputStream()) {
            input.write("abc\n99\n0\n".getBytes(StandardCharsets.UTF_8));
        }
        if (!process.waitFor(20, TimeUnit.SECONDS)) {
            process.destroyForcibly().waitFor();
            throw new AssertionError("Console did not exit within 20 seconds");
        }
        String output = Files.readString(log);
        System.out.println("Input supplied: abc, 99, 0");
        System.out.print(output);
        ok(process.exitValue() == 0, "Console exited with code " + process.exitValue());
        ok(output.contains("Please enter a valid number."), "Missing numeric retry message");
        ok(output.contains("Invalid choice."), "Missing invalid menu choice message");
        ok(output.contains("Thank you for using"), "Missing normal exit message");
        ok(output.contains("Patients saved successfully.")
            && output.contains("Doctors saved successfully.")
            && output.contains("Appointments saved successfully."), "Missing successful save messages");
    }

    private static void runTests() {
        System.out.println("========== BEGIN PHASE 5 TEST OUTPUT ==========");
        System.out.println("Executed: " + ZonedDateTime.now());
        System.out.println("Java: " + System.getProperty("java.version") + " / " + System.getProperty("java.vendor"));
        System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version")
                + " / " + System.getProperty("os.arch"));
        System.out.println("Suite: 21 baseline cases T01-T21; 5 robustness cases G01-G05; 8 scheduling cases S01-S08.");
        System.out.println("All file operations take place in isolated temporary folders.");
        // T01
        check("T01", "Compile all application sources successfully with the current JDK", () -> compileApplication());

        // T02
        check("T02","Patient CRUD and case-insensitive ID lookup succeed",()-> {
            PatientService s=new PatientService();
            s.addPatient(p());
            ok(s.findPatientById(" p1 ").getAge()==20);
            s.updatePatient("P1","Updated","0","x",21,"X","A");
            ok(s.findPatientById("P1").getAge()==21);
            s.deletePatient("P1",new ArrayList<>());
            ok(s.getAllPatients().isEmpty());
        });

        // T03
        check("T03","Doctor CRUD succeeds",()-> {
            DoctorService s=new DoctorService();
            s.addDoctor(d());
            s.updateDoctor("d1","Updated","0","x","Cardiology");
            ok(s.findDoctorById("D1").getSpecialization().equals("Cardiology"));
            s.deleteDoctor("D1",new ArrayList<>());
            ok(s.getAllDoctors().isEmpty());
        });

        // T04
        check("T04","Duplicate patient and doctor IDs rejected",()-> {
            PatientService p=new PatientService();
            p.addPatient(p());
            expect(IllegalArgumentException.class,()->p.addPatient(p()));
            DoctorService d=new DoctorService();
            d.addDoctor(d());
            expect(IllegalArgumentException.class,()->d.addDoctor(d()));
        });

        // T05
        check("T05","Missing patient and doctor raise custom exceptions",()-> {
            expect(PatientNotFoundException.class,()->new PatientService().findPatientById("missing"));
            expect(DoctorNotFoundException.class,()->new DoctorService().findDoctorById("missing"));
        });

        // T06
        check("T06","Booking stores CONFIRMED and normalizes 09:00 to 09:00 AM",()-> {
            Appointment t=a().findAppointmentById("a1");
            ok(t.getTime().equals("09:00 AM")&&t.getStatus().equals("CONFIRMED"));
        });

        // T07
        check("T07","Duplicate appointment ID rejected",()-> {
            AppointmentService s=a();
            expect(IllegalArgumentException.class,()->s.bookAppointment("a1",p(),d(),"2026-11-03","10:00","Visit"));
        });

        // T08
        check("T08","Same doctor/date/time active booking rejected",()-> {
            AppointmentService s=a();
            expect(AppointmentUnavailableException.class,()->s.bookAppointment("A2",p(),d(),"2026-11-02","9:00 AM","Visit"));
        });

        // T09
        check("T09","Different doctor can use same time",()-> {
            AppointmentService s=a();
            Doctor d=new Doctor("D2","Second","0","x","Cardiology");
            d.addSchedule(new Schedule("BASE2", "D2", "2026-11-02", "09:00", "12:00"));
            s.bookAppointment("A2",p(),d,"2026-11-02","09:00","Visit");
            ok(s.getAllAppointments().size()==2);
        });

        // T10
        check("T10","Invalid calendar date and invalid time rejected",()-> {
            AppointmentService s=new AppointmentService();
            expect(IllegalArgumentException.class,()->s.bookAppointment("A",p(),d(),"2026-02-30","09:00","Visit"));
            expect(IllegalArgumentException.class,()->s.bookAppointment("A",p(),d(),"2026-11-02","99:00","Visit"));
        });

        // T11
        check("T11","Blank reason and null patient rejected",()-> {
            expect(IllegalArgumentException.class,()->new AppointmentService().bookAppointment("A",p(),d(),"2026-11-02","09:00"," "));
            expect(IllegalArgumentException.class,()->new AppointmentService().bookAppointment("A",null,d(),"2026-11-02","09:00","Visit"));
        });

        // T12
        check("T12","Reschedule sets RESCHEDULED and permits unchanged own slot",()-> {
            AppointmentService s=a();
            s.rescheduleAppointment("A1","2026-11-02","09:00");
            ok(s.findAppointmentById("A1").getStatus().equals("RESCHEDULED"));
        });

        // T13
        check("T13","Conflicting reschedule rejected and original time preserved",()-> {
            AppointmentService s=a();
            s.bookAppointment("A2",p(),d(),"2026-11-02","10:00","Visit");
            expect(AppointmentUnavailableException.class,()->s.rescheduleAppointment("A1","2026-11-02","10:00"));
            ok(s.findAppointmentById("A1").getTime().equals("09:00 AM"));
        });

        // T14
        check("T14","Cancelled slot becomes reusable; cancelled record cannot reschedule",()-> {
            AppointmentService s=a();
            s.cancelAppointment("A1");
            expect(AppointmentUnavailableException.class,()->s.rescheduleAppointment("A1","2026-11-03","10:00"));
            s.bookAppointment("A2",p(),d(),"2026-11-02","09:00","Visit");
            ok(s.getAllAppointments().size()==2);
        });

        // T15
        check("T15","Completed record cannot cancel or reschedule",()-> {
            AppointmentService s=a();
            s.completeAppointment("A1");
            s.cancelAppointment("A1");
            expect(AppointmentUnavailableException.class,()->s.rescheduleAppointment("A1","2026-11-03","10:00"));
            ok(s.findAppointmentById("A1").getStatus().equals("COMPLETED"));
        });

        // T16
        check("T16","Referenced patient and doctor deletion blocked even after cancellation",()-> {
            PatientService ps=new PatientService();
            ps.addPatient(p());
            DoctorService ds=new DoctorService();
            ds.addDoctor(d());
            AppointmentService s=a();
            s.cancelAppointment("A1");
            expect(IllegalStateException.class,()->ps.deletePatient("P1",s.getAllAppointments()));
            expect(IllegalStateException.class,()->ds.deleteDoctor("D1",s.getAllAppointments()));
        });

        // T17
        check("T17","Save/load restores fields, relationships and status",()-> {
            AppointmentService s=a();
            s.rescheduleAppointment("A1","2026-11-03","14:30");
            FileManager.savePatients(Arrays.asList(p()));
            FileManager.saveDoctors(Arrays.asList(d()));
            FileManager.saveAppointments(s.getAllAppointments());
            List<Patient> ps=new ArrayList<>();
            List<Doctor> ds=new ArrayList<>();
            List<Appointment> as=new ArrayList<>();
            FileManager.loadPatients(ps);
            FileManager.loadDoctors(ds);
            FileManager.loadAppointments(as,ps,ds);
            ok(as.size()==1&&as.get(0).getPatient()==ps.get(0)&&as.get(0).getStatus().equals("RESCHEDULED")&&as.get(0).getTime().equals("02:30 PM"));
        });

        // T18
        check("T18","Orphan appointment row is skipped",()-> {
            Files.writeString(Path.of("appointments.txt"),"A1|unknown|D1|2026-11-02|09:00 AM|Visit|CONFIRMED\n");
            List<Appointment> as=new ArrayList<>();
            FileManager.loadAppointments(as,Arrays.asList(p()),Arrays.asList(d()));
            ok(as.isEmpty());
        });

        // T19
        check("T19", "Person roles and both notification implementations behave correctly", () -> {
            Person patient = p();
            Person doctor = d();
            ok(patient.getRole().equals("Patient"), "Patient role must be Patient");
            ok(doctor.getRole().equals("Doctor"), "Doctor role must be Doctor");
            java.io.ByteArrayOutputStream captured = new java.io.ByteArrayOutputStream();
            java.io.PrintStream original = System.out;
            try (java.io.PrintStream capture = new java.io.PrintStream(captured, true, java.nio.charset.StandardCharsets.UTF_8)) {
                System.setOut(capture);
                Notification notification = new EmailNotification();
                notification.send("test@example.test", "Test message");
                notification = new SMSNotification();
                notification.send("000", "Test message");
            } finally {
                System.setOut(original);
            }     String output = captured.toString(java.nio.charset.StandardCharsets.UTF_8);
            System.out.print(output);
            ok(output.contains("EMAIL NOTIFICATION"), "Email implementation must print its heading");
            ok(output.contains("SMS NOTIFICATION"), "SMS implementation must print its heading");
            ok(output.contains("Test message"), "Notification output must include the message");
        });

        // T20
        check("T20","Missing files are tolerated",()-> {
            Files.deleteIfExists(Path.of("patients.txt"));
            Files.deleteIfExists(Path.of("doctors.txt"));
            Files.deleteIfExists(Path.of("appointments.txt"));
            List<Patient> ps=new ArrayList<>();
            FileManager.loadPatients(ps);
            ok(ps.isEmpty());
        });

        // T21
        check("T21", "Console retries invalid numbers, rejects invalid choices, and saves on exit", () -> testConsole());

        // G01
        check("G01","Reject age zero at service creation boundary",()->expect(IllegalArgumentException.class,()->new PatientService().addPatient(new Patient("P","X","0","x",0,"X","A"))));

        // G02
        check("G02","Enforce doctor's declared schedule during booking",()-> {
            Doctor d=bareDoctor();
            d.addSchedule(new Schedule("S1","D1","2026-11-02","09:00 AM"));
            expect(AppointmentUnavailableException.class,()->new AppointmentService().bookAppointment("A",p(),d,"2026-11-02","22:00","Visit"));
        });

        // G03
        check("G03","Persist doctor schedules across save/load",()-> {
            Doctor d=bareDoctor();
            d.addSchedule(new Schedule("S1","D1","2026-11-02","09:00 AM"));
            FileManager.saveDoctors(Arrays.asList(d));
            List<Doctor> ds=new ArrayList<>();
            FileManager.loadDoctors(ds);
            ok(ds.get(0).getSchedules().size()==1, "Expected 1 reloaded schedule; actual=" + ds.get(0).getSchedules().size());
        });

        // G04
        check("G04","Collection getter prevents external mutation",()-> {
            PatientService s=new PatientService();
            s.addPatient(p());
            try {
                s.getAllPatients().clear();
            }catch(UnsupportedOperationException e) {
            }ok(s.getAllPatients().size()==1, "Expected service list to retain 1 patient after external clear(); actual=" + s.getAllPatients().size());
        });

        // G05
        check("G05","Malformed age does not prevent later valid rows from loading",()-> {
            Files.writeString(Path.of("patients.txt"),"BAD|X|0|x|oops|X|A\nP1|X|0|x|20|X|A\n");
            List<Patient> ps=new ArrayList<>();
            FileManager.loadPatients(ps);
            ok(ps.size()==1, "Expected 1 valid patient after malformed row; actual=" + ps.size());
        });
        check("S01", "Weekly shift accepts opening and rejects closing, earlier time, and wrong weekday", () -> {
            Schedule s = new Schedule("W", "D1", "MONDAY", "08:00", "12:00");
            ok(s.covers("2026-11-02", "08:00"));
            ok(s.covers("2026-11-09", "11:59 AM"));
            ok(!s.covers("2026-11-02", "12:00"));
            ok(!s.covers("2026-11-02", "07:59"));
            ok(!s.covers("2026-11-03", "09:00"));
        });
        check("S02", "No schedule or unavailable schedule rejects booking", () -> {
            Doctor doctor = bareDoctor();
            AppointmentService service = new AppointmentService();
            expect(AppointmentUnavailableException.class, () -> service.bookAppointment("A", p(), doctor, "2026-11-02", "09:00", "Visit"));
            Schedule s = new Schedule("W", "D1", "MONDAY", "08:00", "12:00");
            s.setAvailable(false); doctor.addSchedule(s);
            expect(AppointmentUnavailableException.class, () -> service.bookAppointment("A", p(), doctor, "2026-11-02", "09:00", "Visit"));
            ok(service.getAllAppointments().isEmpty());
        });
        check("S03", "Out-of-hours reschedule preserves date, time, and status", () -> {
            AppointmentService service = a();
            expect(AppointmentUnavailableException.class, () -> service.rescheduleAppointment("A1", "2026-11-02", "22:00"));
            Appointment appointment = service.findAppointmentById("A1");
            ok(appointment.getDate().equals("2026-11-02") && appointment.getTime().equals("09:00 AM") && appointment.getStatus().equals("CONFIRMED"));
        });
        check("S04", "Weekly and dated schedules retain ranges and unavailable flags on reload", () -> {
            Doctor doctor = bareDoctor();
            Schedule s = new Schedule("W", "D1", "MONDAY", "08:00", "12:00");
            s.setAvailable(false); doctor.addSchedule(s);
            doctor.addSchedule(new Schedule("D", "D1", "2026-11-03", "13:00", "17:00"));
            FileManager.saveDoctors(Arrays.asList(doctor));
            List<Doctor> loaded = new ArrayList<>(); FileManager.loadDoctors(loaded);
            ok(loaded.get(0).getSchedules().size() == 2);
            Schedule first = loaded.get(0).getSchedules().get(0);
            ok(first.getDate().equals("MONDAY") && first.getEndTime().equals("12:00 PM") && !first.isAvailable());
            ok(loaded.get(0).getSchedules().get(1).covers("2026-11-03", "16:59"));
        });
        check("S05", "Invalid and orphan schedule rows do not block later valid rows", () -> {
            Files.writeString(Path.of("schedules.txt"), "BAD|D1|MONDAY|oops|12:00|true\nORPHAN|UNKNOWN|MONDAY|08:00|12:00|true\nBOOL|D1|MONDAY|08:00|12:00|maybe\nGOOD|D1|MONDAY|08:00|12:00|true\n");
            Doctor doctor = bareDoctor(); FileManager.loadSchedules(Arrays.asList(doctor));
            ok(doctor.getSchedules().size() == 1 && doctor.getSchedules().get(0).getScheduleId().equals("GOOD"));
        });
        check("S06", "Doctor, appointment, and schedule lists resist external clear", () -> {
            DoctorService doctors = new DoctorService(); doctors.addDoctor(d());
            expect(UnsupportedOperationException.class, () -> doctors.getAllDoctors().clear());
            AppointmentService appointments = a();
            expect(UnsupportedOperationException.class, () -> appointments.getAllAppointments().clear());
            expect(UnsupportedOperationException.class, () -> doctors.getAllDoctors().get(0).getSchedules().clear());
            ok(doctors.getAllDoctors().size() == 1 && appointments.getAllAppointments().size() == 1);
        });
        check("S07", "Project schedule data matches all six screenshot rows and leaves two unspecified doctors unscheduled", () -> {
            List<String> rows = Files.readAllLines(projectRoot.resolve("schedules.txt"));
            Map<String, String> patterns = new HashMap<>();
            patterns.put("DOC-511", "08:00 AM|12:00 PM"); patterns.put("DOC-514", "09:00 AM|01:00 PM");
            patterns.put("DOC-513", "01:00 PM|05:00 PM"); patterns.put("DOC-516", "09:00 AM|12:00 PM");
            patterns.put("DOC-517", "08:00 AM|12:00 PM"); patterns.put("DOC-518", "01:00 PM|05:00 PM");
            Set<String> actual = new HashSet<>();
            for (String row : rows) {
                String[] fields = row.split("\\|", -1);
                ok(fields.length == 6 && patterns.containsKey(fields[1]));
                ok((fields[3] + "|" + fields[4]).equals(patterns.get(fields[1])) && fields[5].equals("true"));
                actual.add(fields[1] + "|" + fields[2]);
            }
            Set<String> expected = new HashSet<>();
            for (String id : patterns.keySet()) for (String day : (id.equals("DOC-514") || id.equals("DOC-517"))
                    ? Arrays.asList("TUESDAY", "THURSDAY", "SATURDAY") : Arrays.asList("MONDAY", "WEDNESDAY", "FRIDAY"))
                expected.add(id + "|" + day);
            ok(rows.size() == 18 && actual.equals(expected));
        });
        check("S08", "Historical appointment restores without schedule and normalizes legacy time", () -> {
            AppointmentService service = new AppointmentService();
            Appointment historical = new Appointment("OLD", p(), bareDoctor(), "2026-09-30", "8:00", "Historical");
            historical.setStatus("CANCELLED"); service.restoreAppointment(historical);
            ok(service.findAppointmentById("OLD").getStatus().equals("CANCELLED"));
            ok(service.findAppointmentById("OLD").getTime().equals("08:00 AM"));
            expect(IllegalArgumentException.class, () -> service.restoreAppointment(historical));
        });
        System.out.println("\n================ FINAL SUMMARY ================");
        for (String result : RESULTS) System.out.println(result);
        System.out.println("TOTAL=" + RESULTS.size() + " | PASS=" + passed
                + " | FAIL=" + failed + " | BLOCKED=" + blocked);
        System.out.println("Revised application: all 34 cases should pass. Valid-booking fixtures now declare schedules.");
        System.out.println("G01-G05 test desired safeguards; FAIL means the safeguard is missing, not a successful test.");
        System.out.println("T01 compiles current source. Other tests execute the classes built by your IDE/terminal.");
        System.out.println("Save all source files and rebuild before running so those classes match your source.");
        System.out.println("========== END PHASE 5 TEST OUTPUT ==========");
    }
}
