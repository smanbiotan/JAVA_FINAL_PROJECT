package testing;

import model.*;
import service.*;
import java.nio.file.*;
import java.util.*;

/** Focused regression tests. Runs inside a temporary folder to protect clinic data. */
public class TestAutoIds {
    private static int passed;
    interface Action { void run() throws Exception; }
    private static void check(String name, boolean result) {
        if (!result) throw new AssertionError(name);
        passed++; System.out.println("PASS | " + name);
    }
    private static void reject(String name, Action action) throws Exception {
        try { action.run(); } catch (IllegalArgumentException expected) { check(name, true); return; }
        throw new AssertionError(name + ": duplicate was accepted");
    }
    private static Doctor doctor(String id) { return new Doctor(id, "Test", "0", "x@example.test", "Dermatology"); }
    private static Patient patient(String id) { return new Patient(id, "Test", "0", "x@example.test", 20, "X", "Address"); }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            Path temporary = Files.createTempDirectory("clinic-auto-id-tests-");
            String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
            String classes = Path.of(TestAutoIds.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
            Process child = new ProcessBuilder(java, "-cp", classes, "testing.TestAutoIds", "worker")
                .directory(temporary.toFile()).inheritIO().start();
            if (child.waitFor() != 0) throw new AssertionError("Test subprocess failed");
            return;
        }
        // Empty datasets begin at 001, with separate namespaces for each entity.
        check("empty doctor series", IdGenerator.next("DOC-").equals("DOC-001"));
        DoctorService doctors = new DoctorService();
        doctors.addDoctor(doctor("DOC-519")); doctors.addDoctor(doctor("DOC-010"));
        check("highest numeric doctor, not row order", doctors.nextId().equals("DOC-520"));
        reject("case and whitespace doctor duplicate", () -> doctors.addDoctor(doctor(" doc-519 ")));
        doctors.deleteDoctor("DOC-519", Collections.emptyList());
        check("deleted high ID retained", doctors.nextId().equals("DOC-520"));
        check("preview does not consume ID", doctors.nextId().equals(doctors.nextId()));

        PatientService patients = new PatientService(); patients.addPatient(patient("PNT-018"));
        check("patient increments to 019", patients.nextId().equals("PNT-019"));
        reject("patient duplicate", () -> patients.addPatient(patient(" pnt-018 ")));
        Doctor d = doctors.findDoctorById("DOC-010");
        doctors.addSchedule(d.getId(), new Schedule("SCH-009", d.getId(), "MONDAY", "08:00", "12:00"));
        check("schedule increments to 010", doctors.nextScheduleId().equals("SCH-010"));
        Doctor second = doctor("DOC-020"); doctors.addDoctor(second);
        reject("schedule duplicate across doctors", () -> doctors.addSchedule(second.getId(), new Schedule(" sch-009 ", second.getId(), "MONDAY", "09:00")));
        doctors.addSchedule(second.getId(), new Schedule("SCH-511-MON", second.getId(), "MONDAY", "09:00"));
        check("legacy schedule does not corrupt numeric counter", doctors.nextScheduleId().equals("SCH-010"));

        AppointmentService appointments = new AppointmentService();
        appointments.bookAppointment("APT-012", patient("PNT-018"), d, "2026-11-02", "09:00", "Visit");
        check("appointment increments to 013", appointments.nextId().equals("APT-013"));
        reject("appointment duplicate", () -> appointments.bookAppointment(" apt-012 ", patient("PNT-018"), d, "2026-11-02", "10:00", "Visit"));
        IdGenerator.observe("APT-999");
        check("IDs grow beyond three digits", appointments.nextId().equals("APT-1000"));
        IdGenerator.save();
        Properties saved = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(Path.of("id_sequences.txt"))) { saved.load(reader); }
        check("all four counters persisted", saved.size() == 4 && saved.getProperty("DOC-").equals("519"));
        Files.writeString(Path.of("id_sequences.txt"), "DOC-=700\nPNT-=bad\nAPT-=999\nSCH-=9\n");
        IdGenerator.load();
        check("saved higher counter restored", doctors.nextId().equals("DOC-701"));
        check("invalid saved counter retains known patient maximum", patients.nextId().equals("PNT-019"));
        System.out.println("TOTAL=" + passed + " | PASS=" + passed + " | FAIL=0");
    }
}
