package service;

import model.Patient;
import exception.PatientNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class PatientService {

    private List<Patient> patients;

    public PatientService() {
        patients = new ArrayList<>();
    }

    // CREATE
    public void addPatient(Patient patient) {

        if (patient == null) {
            throw new IllegalArgumentException(
                    "Patient cannot be null."
            );
        }

        if (findPatientWithoutException(patient.getId()) != null) {
            throw new IllegalArgumentException(
                    "Patient ID already exists."
            );
        }

        patients.add(patient);

        System.out.println(
                "Patient added successfully."
        );
    }

    // READ
    public List<Patient> getAllPatients() {
        return patients;
    }

    public Patient findPatientById(String id)
            throws PatientNotFoundException {

        Patient patient = findPatientWithoutException(id);

        if (patient == null) {
            throw new PatientNotFoundException(
                    "Patient with ID " + id + " not found."
            );
        }

        return patient;
    }

    private Patient findPatientWithoutException(String id) {

        for (Patient patient : patients) {

            if (patient.getId().equalsIgnoreCase(id)) {
                return patient;
            }
        }

        return null;
    }

    // UPDATE
    public void updatePatient(
            String id,
            String name,
            String phone,
            String email,
            int age,
            String gender,
            String address)
            throws PatientNotFoundException {

        Patient patient = findPatientById(id);

        patient.setName(name);
        patient.setPhone(phone);
        patient.setEmail(email);
        patient.setAge(age);
        patient.setGender(gender);
        patient.setAddress(address);

        System.out.println(
                "Patient updated successfully."
        );
    }

    // DELETE
    public void deletePatient(String id)
            throws PatientNotFoundException {

        Patient patient = findPatientById(id);

        patients.remove(patient);

        System.out.println(
                "Patient deleted successfully."
        );
    }
}