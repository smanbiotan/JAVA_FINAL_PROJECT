package repository;

import model.Appointment;
import model.Doctor;
import model.Patient;

import java.io.*;
import java.util.List;

public class FileManager {

    private static final String PATIENT_FILE =
            "patients.txt";

    private static final String DOCTOR_FILE =
            "doctors.txt";

    private static final String APPOINTMENT_FILE =
            "appointments.txt";

    // ==========================
    // SAVE PATIENTS
    // ==========================

    public static void savePatients(
            List<Patient> patients) {

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new FileWriter(PATIENT_FILE))) {

            for (Patient patient : patients) {

                writer.write(
                        patient.getId() + "|" +
                        patient.getName() + "|" +
                        patient.getPhone() + "|" +
                        patient.getEmail() + "|" +
                        patient.getAge() + "|" +
                        patient.getGender() + "|" +
                        patient.getAddress()
                );

                writer.newLine();
            }

            System.out.println(
                    "Patients saved successfully."
            );

        } catch (IOException e) {

            System.out.println(
                    "Error saving patients: "
                            + e.getMessage()
            );
        }
    }

    // ==========================
    // SAVE DOCTORS
    // ==========================

    public static void saveDoctors(
            List<Doctor> doctors) {

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new FileWriter(DOCTOR_FILE))) {

            for (Doctor doctor : doctors) {

                writer.write(
                        doctor.getId() + "|" +
                        doctor.getName() + "|" +
                        doctor.getPhone() + "|" +
                        doctor.getEmail() + "|" +
                        doctor.getSpecialization()
                );

                writer.newLine();
            }

            System.out.println(
                    "Doctors saved successfully."
            );

        } catch (IOException e) {

            System.out.println(
                    "Error saving doctors: "
                            + e.getMessage()
            );
        }
    }

    // ==========================
    // SAVE APPOINTMENTS
    // ==========================

    public static void saveAppointments(
            List<Appointment> appointments) {

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new FileWriter(APPOINTMENT_FILE))) {

            for (Appointment appointment :
                    appointments) {

                writer.write(
                        appointment.getAppointmentId()
                                + "|" +
                        appointment.getPatient().getId()
                                + "|" +
                        appointment.getDoctor().getId()
                                + "|" +
                        appointment.getDate()
                                + "|" +
                        appointment.getTime()
                                + "|" +
                        appointment.getReason()
                                + "|" +
                        appointment.getStatus()
                );

                writer.newLine();
            }

            System.out.println(
                    "Appointments saved successfully."
            );

        } catch (IOException e) {

            System.out.println(
                    "Error saving appointments: "
                            + e.getMessage()
            );
        }
    }

    // ==========================
    // LOAD PATIENTS
    // ==========================

    public static void loadPatients(
            List<Patient> patients) {

        File file = new File(PATIENT_FILE);

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] data = line.split("\\|");

                if (data.length != 7) {
                    continue;
                }

                Patient patient = new Patient(
                        data[0],
                        data[1],
                        data[2],
                        data[3],
                        Integer.parseInt(data[4]),
                        data[5],
                        data[6]
                );

                patients.add(patient);
            }

            System.out.println(
                    "Patients loaded successfully."
            );

        } catch (IOException |
                 NumberFormatException e) {

            System.out.println(
                    "Error loading patients: "
                            + e.getMessage()
            );
        }
    }

    // ==========================
    // LOAD DOCTORS
    // ==========================

    public static void loadDoctors(
            List<Doctor> doctors) {

        File file = new File(DOCTOR_FILE);

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] data = line.split("\\|");

                if (data.length != 5) {
                    continue;
                }

                Doctor doctor = new Doctor(
                        data[0],
                        data[1],
                        data[2],
                        data[3],
                        data[4]
                );

                doctors.add(doctor);
            }

            System.out.println(
                    "Doctors loaded successfully."
            );

        } catch (IOException e) {

            System.out.println(
                    "Error loading doctors: "
                            + e.getMessage()
            );
        }
    }

    // ==========================
    // LOAD APPOINTMENTS
    // ==========================

    public static void loadAppointments(
            List<Appointment> appointments,
            List<Patient> patients,
            List<Doctor> doctors) {

        File file = new File(APPOINTMENT_FILE);

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] data = line.split("\\|");

                if (data.length != 7) {
                    continue;
                }

                Patient patient =
                        findPatient(
                                patients,
                                data[1]
                        );

                Doctor doctor =
                        findDoctor(
                                doctors,
                                data[2]
                        );

                if (patient == null ||
                        doctor == null) {

                    continue;
                }

                Appointment appointment =
                        new Appointment(
                                data[0],
                                patient,
                                doctor,
                                data[3],
                                data[4],
                                data[5]
                        );

                appointment.setStatus(data[6]);

                appointments.add(appointment);
            }

            System.out.println(
                    "Appointments loaded successfully."
            );

        } catch (IOException e) {

            System.out.println(
                    "Error loading appointments: "
                            + e.getMessage()
            );
        }
    }

    private static Patient findPatient(
            List<Patient> patients,
            String id) {

        for (Patient patient : patients) {

            if (patient.getId()
                    .equalsIgnoreCase(id)) {

                return patient;
            }
        }

        return null;
    }

    private static Doctor findDoctor(
            List<Doctor> doctors,
            String id) {

        for (Doctor doctor : doctors) {

            if (doctor.getId()
                    .equalsIgnoreCase(id)) {

                return doctor;
            }
        }

        return null;
    }
}