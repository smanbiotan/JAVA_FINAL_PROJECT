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

    // AUTO ID: inspect existing records as well as the saved high-water mark.
    public String nextId() {
        for (Doctor item : doctors) IdGenerator.observe(item.getId());
        return IdGenerator.next("DOC-");
    }

    // CREATE: reject duplicates even when called outside the console.

    public void addDoctor(Doctor doctor) {
        if (doctor == null) {
            throw new IllegalArgumentException("Doctor cannot be null.");
        }

        if (findDoctorWithoutException(doctor.getId()) != null) {
            throw new IllegalArgumentException("Doctor ID already exists.");
        }

        // Validate attached schedule IDs before inserting a doctor loaded or created elsewhere.
        for (model.Schedule schedule : doctor.getSchedules()) {
            for (Doctor existingDoctor : doctors) for (model.Schedule existing : existingDoctor.getSchedules())
                if (existing.getScheduleId().equalsIgnoreCase(schedule.getScheduleId()))
                    throw new IllegalArgumentException("Schedule ID already exists.");
        }
        doctors.add(doctor);
        for (model.Schedule schedule : doctor.getSchedules()) IdGenerator.observe(schedule.getScheduleId());
        IdGenerator.observe(doctor.getId());
        System.out.println("Doctor added successfully.");
    }

    // Schedule IDs are unique across all doctors, not just one doctor's list.
    public String nextScheduleId() {
        for (Doctor doctor : doctors) for (model.Schedule schedule : doctor.getSchedules())
            IdGenerator.observe(schedule.getScheduleId());
        return IdGenerator.next("SCH-");
    }

    public void addSchedule(String doctorId, model.Schedule schedule) throws DoctorNotFoundException {
        if (schedule == null) throw new IllegalArgumentException("Schedule is required.");
        for (Doctor doctor : doctors) for (model.Schedule existing : doctor.getSchedules()) {
            if (existing.getScheduleId().equalsIgnoreCase(schedule.getScheduleId().trim()))
                throw new IllegalArgumentException("Schedule ID already exists.");
        }
        findDoctorById(doctorId).addSchedule(schedule);
        IdGenerator.observe(schedule.getScheduleId());
    }

    // READ
    public List<Doctor> getAllDoctors() {
        return java.util.Collections.unmodifiableList(doctors);
    }

    public Doctor findDoctorById(String id)
            throws DoctorNotFoundException {

        Doctor doctor = findDoctorWithoutException(id);

        if (doctor == null) {
            throw new DoctorNotFoundException("Doctor with ID " + id + " not found.");
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

                    throw new IllegalStateException("Cannot delete doctor because appointment "
                                    + appointment.getAppointmentId()
                                    + " references this doctor.");
                }
            }
        }

        doctors.remove(doctor);
        System.out.println("Doctor deleted successfully.");
    }
}
