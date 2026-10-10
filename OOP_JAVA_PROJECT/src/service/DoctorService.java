package service;

import model.Appointment;
import model.Doctor;
import exception.DoctorNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class DoctorService {

    private final List<Doctor> doctors;

    public DoctorService() {
        doctors = new ArrayList<>();
    }

    // CREATE
    public void addDoctor(Doctor doctor) {
        if (doctor == null) {
            throw new IllegalArgumentException(
                    "Doctor cannot be null.");
        }

        if (findDoctorWithoutException(doctor.getId()) != null) {
            throw new IllegalArgumentException(
                    "Doctor ID already exists.");
        }

        doctors.add(doctor);
        System.out.println("Doctor added successfully.");
    }

    // READ
    public List<Doctor> getAllDoctors() {
        return doctors;
    }

    public Doctor findDoctorById(String id)
            throws DoctorNotFoundException {

        Doctor doctor = findDoctorWithoutException(id);

        if (doctor == null) {
            throw new DoctorNotFoundException(
                    "Doctor with ID " + id + " not found.");
        }

        return doctor;
    }

    private Doctor findDoctorWithoutException(String id) {
        if (id == null) {
            return null;
        }

        for (Doctor doctor : doctors) {
            if (doctor.getId().equalsIgnoreCase(id.trim())) {
                return doctor;
            }
        }

        return null;
    }

    // UPDATE
    public void updateDoctor(
            String id,
            String name,
            String phone,
            String email,
            String specialization)
            throws DoctorNotFoundException {

        Doctor doctor = findDoctorById(id);

        doctor.setName(name);
        doctor.setPhone(phone);
        doctor.setEmail(email);
        doctor.setSpecialization(specialization);

        System.out.println("Doctor updated successfully.");
    }

    // DELETE
    public void deleteDoctor(
            String id,
            List<Appointment> appointments)
            throws DoctorNotFoundException {

        Doctor doctor = findDoctorById(id);

        if (appointments != null) {
            for (Appointment appointment : appointments) {
                if (appointment.getDoctor().getId()
                        .equalsIgnoreCase(doctor.getId())) {

                    throw new IllegalStateException(
                            "Cannot delete doctor because appointment "
                                    + appointment.getAppointmentId()
                                    + " references this doctor.");
                }
            }
        }

        doctors.remove(doctor);
        System.out.println("Doctor deleted successfully.");
    }
}
