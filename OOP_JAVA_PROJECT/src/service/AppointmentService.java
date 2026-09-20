package service;

import model.Appointment;
import model.Doctor;
import model.Patient;
import exception.AppointmentUnavailableException;

import java.util.ArrayList;
import java.util.List;

public class AppointmentService {

    private List<Appointment> appointments;

    public AppointmentService() {
        appointments = new ArrayList<>();
    }

    // CREATE
    public void bookAppointment(
            String appointmentId,
            Patient patient,
            Doctor doctor,
            String date,
            String time,
            String reason)
            throws AppointmentUnavailableException {

        if (appointmentId == null ||
                appointmentId.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Appointment ID is required."
            );
        }

        if (patient == null) {

            throw new IllegalArgumentException(
                    "Patient is required."
            );
        }

        if (doctor == null) {

            throw new IllegalArgumentException(
                    "Doctor is required."
            );
        }

        if (date == null ||
                date.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Date is required."
            );
        }

        if (time == null ||
                time.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Time is required."
            );
        }

        if (reason == null ||
                reason.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Reason is required."
            );
        }

        if (findAppointmentById(appointmentId) != null) {

            throw new IllegalArgumentException(
                    "Appointment ID already exists."
            );
        }

        // Check doctor availability
        for (Appointment appointment : appointments) {

            if (appointment.getDoctor()
                    .getId()
                    .equalsIgnoreCase(
                            doctor.getId())
                    && appointment.getDate()
                    .equals(date)
                    && appointment.getTime()
                    .equals(time)
                    && appointment.getStatus()
                    .equals("CONFIRMED")) {

                throw new AppointmentUnavailableException("Doctor is already booked at " + date + " " + time + "."
                );
            }
        }

        Appointment appointment =
                new Appointment(
                        appointmentId,
                        patient,
                        doctor,
                        date,
                        time,
                        reason
                );

        appointments.add(appointment);

        System.out.println(
                "Appointment booked successfully."
        );
    }

    // READ
    public List<Appointment> getAllAppointments() {
        return appointments;
    }

    public Appointment findAppointmentById(String id) {

        for (Appointment appointment : appointments) {

            if (appointment.getAppointmentId()
                    .equalsIgnoreCase(id)) {

                return appointment;
            }
        }

        return null;
    }

    // UPDATE
    public void rescheduleAppointment(
            String id,
            String newDate,
            String newTime)
            throws AppointmentUnavailableException {

        Appointment appointment =
                findAppointmentById(id);

        if (appointment == null) {

            throw new AppointmentUnavailableException(
                    "Appointment not found."
            );
        }

        if (appointment.getStatus()
                .equals("CANCELLED")) {

            throw new AppointmentUnavailableException(
                    "Cancelled appointment cannot be rescheduled."
            );
        }

        for (Appointment existing : appointments) {

            if (!existing.getAppointmentId()
                    .equalsIgnoreCase(id)
                    && existing.getDoctor()
                    .getId()
                    .equalsIgnoreCase(
                            appointment.getDoctor().getId())
                    && existing.getDate()
                    .equals(newDate)
                    && existing.getTime()
                    .equals(newTime)
                    && existing.getStatus()
                    .equals("CONFIRMED")) {

                throw new AppointmentUnavailableException(
                        "New date and time is already booked."
                );
            }
        }

        appointment.setDate(newDate);
        appointment.setTime(newTime);

        System.out.println(
                "Appointment rescheduled successfully."
        );
    }

    // CANCEL
    public void cancelAppointment(String id) {

        Appointment appointment =
                findAppointmentById(id);

        if (appointment == null) {

            System.out.println(
                    "Appointment not found."
            );

            return;
        }

        appointment.setStatus("CANCELLED");

        System.out.println(
                "Appointment cancelled successfully."
        );
    }

    // COMPLETE
    public void completeAppointment(String id) {

        Appointment appointment =
                findAppointmentById(id);

        if (appointment == null) {

            System.out.println(
                    "Appointment not found."
            );

            return;
        }

        appointment.setStatus("COMPLETED");

        System.out.println(
                "Appointment completed successfully."
        );
    }
}