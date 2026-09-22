package project;

import model.*;
import service.*;
import exception.*;
import repository.FileManager;

import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    private static final PatientService patientService = new PatientService();

    private static final DoctorService doctorService = new DoctorService();

    private static final AppointmentService appointmentService = new AppointmentService();

    public static void main(String[] args) {

        loadData();

        int choice;

        do {

            displayMainMenu();

            choice = readInt("Enter choice: ");

            switch (choice) {

                case 1:
                    patientMenu();
                    break;

                case 2:
                    doctorMenu();
                    break;

                case 3:
                    appointmentMenu();
                    break;

                case 4:
                    searchMenu();
                    break;

                case 5:
                    saveData();
                    break;

                case 6:
                    notificationDemo();
                    break;

                case 0:

                    saveData();

                    System.out.println(
                            "\nThank you for using " + "Clinic Appointment " + "Management System.");
                    break;

                default:

                    System.out.println("Invalid choice. " + "Please try again.");
            }

        } while (choice != 0);

        scanner.close();
    }

    // MAIN MENU

    private static void displayMainMenu() {

        System.out.println("\n======================================");
        System.out.println("   CLINIC APPOINTMENT MANAGEMENT");
        System.out.println("======================================");

        System.out.println("1. Patient Management");
        System.out.println("2. Doctor Management");
        System.out.println("3. Appointment Management");
        System.out.println("4. Search");
        System.out.println("5. Save Data");
        System.out.println("6. Notification / Polymorphism Demo");
        System.out.println("0. Exit");
    }

    // PATIENT MENU

    private static void patientMenu() {

        int choice;

        do {

            System.out.println(
                    "\n========== PATIENT MANAGEMENT ==========");

            System.out.println("1. Add Patient");
            System.out.println("2. View All Patients");
            System.out.println("3. Update Patient");
            System.out.println("4. Delete Patient");
            System.out.println("0. Back");

            choice = readInt("Enter choice: ");

            try {

                switch (choice) {

                    case 1:
                        addPatient();
                        break;

                    case 2:
                        viewPatients();
                        break;

                    case 3:
                        updatePatient();
                        break;

                    case 4:
                        deletePatient();
                        break;

                    case 0:
                        break;

                    default:
                        System.out.println(
                                "Invalid choice.");
                }

            } catch (Exception e) {

                System.out.println("Error: " + e.getMessage());
            }

        } while (choice != 0);
    }

    private static void addPatient() {

        System.out.println("\n===== ADD PATIENT =====");

        String id = readRequiredString("Patient ID: ");

        String name = readRequiredString("Name: ");

        String phone = readRequiredString("Phone: ");

        String email = readRequiredString("Email: ");

        int age = readInt("Age: ");

        if (age <= 0) {

            System.out.println("Age must be greater than zero.");

            return;
        }

        String gender = readRequiredString("Gender: ");

        String address = readRequiredString("Address: ");

        Patient patient = new Patient(
                id,
                name,
                phone,
                email,
                age,
                gender,
                address);

        patientService.addPatient(patient);
    }

    private static void viewPatients() {

        System.out.println("\n===== ALL PATIENTS =====");

        if (patientService.getAllPatients().isEmpty()) {

            System.out.println("No patients found.");

            return;
        }

        for (Patient patient : patientService.getAllPatients()) {

            System.out.println("\n" + patient);
        }
    }

    private static void updatePatient() throws PatientNotFoundException {

        System.out.println("\n===== UPDATE PATIENT =====");

        String id = readRequiredString("Patient ID: ");

        String name = readRequiredString("New name: ");

        String phone = readRequiredString("New phone: ");

        String email = readRequiredString("New email: ");

        int age = readInt("New age: ");

        if (age <= 0) {

            System.out.println("Age must be greater than zero.");

            return;
        }

        String gender = readRequiredString("New gender: ");

        String address = readRequiredString("New address: ");

        patientService.updatePatient(
                id,
                name,
                phone,
                email,
                age,
                gender,
                address);
    }

    private static void deletePatient() throws PatientNotFoundException {

        System.out.println("\n===== DELETE PATIENT =====");

        String id = readRequiredString("Patient ID: ");

        patientService.deletePatient(id);
    }

    // DOCTOR MENU

    private static void doctorMenu() {

        int choice;

        do {

            System.out.println("\n========== DOCTOR MANAGEMENT ==========");

            System.out.println("1. Add Doctor");
            System.out.println("2. View All Doctors");
            System.out.println("3. Update Doctor");
            System.out.println("4. Delete Doctor");
            System.out.println("0. Back");

            choice = readInt("Enter choice: ");

            try {

                switch (choice) {

                    case 1:
                        addDoctor();
                        break;

                    case 2:
                        viewDoctors();
                        break;

                    case 3:
                        updateDoctor();
                        break;

                    case 4:
                        deleteDoctor();
                        break;

                    case 0:
                        break;

                    default:
                        System.out.println("Invalid choice.");
                }

            } catch (Exception e) {

                System.out.println("Error: " + e.getMessage());
            }

        } while (choice != 0);
    }

    private static void addDoctor() {

        System.out.println("\n===== ADD DOCTOR =====");

        String id = readRequiredString("Doctor ID: ");

        String name = readRequiredString("Name: ");

        String phone = readRequiredString("Phone: ");

        String email = readRequiredString("Email: ");

        String specialization = readRequiredString("Specialization: ");

        Doctor doctor = new Doctor(
                id,
                name,
                phone,
                email,
                specialization);

        doctorService.addDoctor(doctor);
    }

    private static void viewDoctors() {

        System.out.println("\n===== ALL DOCTORS =====");

        if (doctorService.getAllDoctors().isEmpty()) {

            System.out.println("No doctors found.");

            return;
        }

        for (Doctor doctor : doctorService.getAllDoctors()) {

            System.out.println("\n" + doctor);
        }
    }

    private static void updateDoctor()
            throws DoctorNotFoundException {

        System.out.println("\n===== UPDATE DOCTOR =====");

        String id = readRequiredString("Doctor ID: ");

        String name = readRequiredString("New name: ");

        String phone = readRequiredString("New phone: ");

        String email = readRequiredString("New email: ");

        String specialization = readRequiredString("New specialization: ");

        doctorService.updateDoctor(
                id,
                name,
                phone,
                email,
                specialization);
    }

    private static void deleteDoctor()
            throws DoctorNotFoundException {

        System.out.println("\n===== DELETE DOCTOR =====");

        String id = readRequiredString("Doctor ID: ");

        doctorService.deleteDoctor(id);
    }

    // APPOINTMENT MENU

    private static void appointmentMenu() {

        int choice;

        do {

            System.out.println("\n======= APPOINTMENT MANAGEMENT =======");

            System.out.println("1. Book Appointment");
            System.out.println("2. View Appointments");
            System.out.println("3. Reschedule Appointment");
            System.out.println("4. Cancel Appointment");
            System.out.println("5. Complete Appointment");
            System.out.println("0. Back");

            choice = readInt("Enter choice: ");

            try {

                switch (choice) {

                    case 1:
                        bookAppointment();
                        break;

                    case 2:
                        viewAppointments();
                        break;

                    case 3:
                        rescheduleAppointment();
                        break;

                    case 4:
                        cancelAppointment();
                        break;

                    case 5:
                        completeAppointment();
                        break;

                    case 0:
                        break;

                    default:
                        System.out.println("Invalid choice.");
                }

            } catch (Exception e) {

                System.out.println("Error: " + e.getMessage());
            }

        } while (choice != 0);
    }

    private static void bookAppointment()
            throws PatientNotFoundException,
            DoctorNotFoundException,
            AppointmentUnavailableException {

        System.out.println("\n===== BOOK APPOINTMENT =====");

        String appointmentId = readRequiredString("Appointment ID: ");

        String patientId = readRequiredString("Patient ID: ");

        String doctorId = readRequiredString("Doctor ID: ");

        String date = readRequiredString("Date (YYYY-MM-DD): ");

        String time = readRequiredString("Time (HH:MM AM/PM): ");

        String reason = readRequiredString("Reason: ");

        Patient patient = patientService.findPatientById(patientId);

        Doctor doctor = doctorService.findDoctorById(doctorId);

        appointmentService.bookAppointment(
                appointmentId,
                patient,
                doctor,
                date,
                time,
                reason);
    }

    private static void viewAppointments() {

        System.out.println("\n===== ALL APPOINTMENTS =====");

        if (appointmentService
                .getAllAppointments()
                .isEmpty()) {

            System.out.println(
                    "No appointments found.");

            return;
        }

        for (Appointment appointment : appointmentService
                .getAllAppointments()) {

            System.out.println(
                    appointment);
        }
    }

    private static void rescheduleAppointment()
            throws AppointmentUnavailableException {

        System.out.println("\n===== RESCHEDULE =====");

        String id = readRequiredString(
                "Appointment ID: ");

        String date = readRequiredString(
                "New date: ");

        String time = readRequiredString(
                "New time: ");

        appointmentService.rescheduleAppointment(
                id,
                date,
                time);
    }

    private static void cancelAppointment() {

        System.out.println("\n===== CANCEL APPOINTMENT =====");

        String id = readRequiredString("Appointment ID: ");

        appointmentService.cancelAppointment(id);
    }

    private static void completeAppointment() {

        System.out.println("\n===== COMPLETE APPOINTMENT =====");

        String id = readRequiredString("Appointment ID: ");

        appointmentService.completeAppointment(id);
    }

    // SEARCH

    private static void searchMenu() {

        System.out.println("\n========== SEARCH ==========");

        System.out.println("1. Search Patient");
        System.out.println("2. Search Doctor");
        System.out.println("3. Search Appointment");
        System.out.println("0. Back");

        int choice = readInt("Enter choice: ");

        try {

            switch (choice) {

                case 1:

                    String patientId = readRequiredString("Patient ID: ");

                    Patient patient = patientService.findPatientById(patientId);

                    System.out.println("\n" + patient);

                    break;

                case 2:

                    String doctorId = readRequiredString("Doctor ID: ");

                    Doctor doctor = doctorService.findDoctorById(doctorId);

                    System.out.println("\n" + doctor);

                    break;

                case 3:

                    String appointmentId = readRequiredString("Appointment ID: ");

                    Appointment appointment = appointmentService.findAppointmentById(appointmentId);

                    if (appointment == null) {

                        System.out.println("Appointment not found.");

                    } else {

                        System.out.println(appointment);
                    }

                    break;

                case 0:
                    break;

                default:

                    System.out.println("Invalid choice.");
            }

        } catch (Exception e) {

            System.out.println("Error: " + e.getMessage());
        }
    }

    // SAVE DATA

    private static void saveData() {

        FileManager.savePatients(patientService.getAllPatients());

        FileManager.saveDoctors(doctorService.getAllDoctors());

        FileManager.saveAppointments(appointmentService.getAllAppointments());
    }

    // LOAD DATA

    private static void loadData() {

        FileManager.loadPatients(patientService.getAllPatients());

        FileManager.loadDoctors(doctorService.getAllDoctors());

        FileManager.loadAppointments(appointmentService.getAllAppointments(),patientService.getAllPatients(),doctorService.getAllDoctors());
    }

    // POLYMORPHISM DEMO

    private static void notificationDemo() {

        System.out.println("\n===== POLYMORPHISM DEMO =====");

        Notification notification;

        // Parent/interface reference
        // pointing to EmailNotification object

        notification = new EmailNotification();

        notification.send(
                "patient@gmail.com",
                "Your clinic appointment "
                        + "has been confirmed.");

        // Same reference
        // pointing to SMSNotification object

        notification = new SMSNotification();

        notification.send(
                "09123456789",
                "Your clinic appointment "
                        + "has been confirmed.");
    }

    // INPUT VALIDATION

    private static String readRequiredString(
            String message) {

        while (true) {

            System.out.print(message);

            String input = scanner.nextLine().trim();

            if (!input.isEmpty()) {
                return input;
            }

            System.out.println("Input cannot be empty.");
        }
    }

    private static int readInt(String message) {

        while (true) {

            try {

                System.out.print(message);

                return Integer.parseInt(scanner.nextLine().trim());

            } catch (NumberFormatException e) {

                System.out.println("Please enter a valid number.");
            }
        }
    }
}
