
package service;

import model.Appointment;
import model.Doctor;
import model.Patient;
import exception.AppointmentUnavailableException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AppointmentService {

    private final List<Appointment> appointments;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd")
                    .withResolverStyle(ResolverStyle.STRICT);

    private static final DateTimeFormatter TIME_12_HOUR =
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("h:mm a")
                    .toFormatter(Locale.US);

    private static final DateTimeFormatter TIME_24_HOUR =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final DateTimeFormatter DISPLAY_TIME =
            DateTimeFormatter.ofPattern("hh:mm a", Locale.US);

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

        if (isBlank(appointmentId)) {
            throw new IllegalArgumentException(
                    "Appointment ID is required.");
        }

        if (patient == null || doctor == null) {
            throw new IllegalArgumentException(
                    "Patient and doctor are required.");
        }

        if (isBlank(reason)) {
            throw new IllegalArgumentException(
                    "Appointment reason is required.");
        }

        if (findAppointmentById(appointmentId) != null) {
            throw new IllegalArgumentException(
                    "Appointment ID already exists.");
        }

        String validDate = normalizeDate(date);
        String validTime = normalizeTime(time);

        checkDoctorConflict(
                doctor.getId(), validDate, validTime, null);

        Appointment appointment = new Appointment(
                appointmentId.trim(),
                patient,
                doctor,
                validDate,
                validTime,
                reason.trim());

        appointments.add(appointment);

        System.out.println("Appointment booked successfully.");
    }

    // READ
    public List<Appointment> getAllAppointments() {
        return appointments;
    }

    public Appointment findAppointmentById(String id) {
        if (isBlank(id)) {
            return null;
        }

        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentId()
                    .equalsIgnoreCase(id.trim())) {
                return appointment;
            }
        }

        return null;
    }

    // UPDATE: RESCHEDULE
    public void rescheduleAppointment(
            String id,
            String newDate,
            String newTime)
            throws AppointmentUnavailableException {

        Appointment appointment = findAppointmentById(id);

        if (appointment == null) {
            throw new AppointmentUnavailableException(
                    "Appointment not found: " + id);
        }

        String status = appointment.getStatus();

        if ("CANCELLED".equalsIgnoreCase(status)) {
            throw new AppointmentUnavailableException(
                    "Cancelled appointment cannot be rescheduled.");
        }

        if ("COMPLETED".equalsIgnoreCase(status)) {
            throw new AppointmentUnavailableException(
                    "Completed appointment cannot be rescheduled.");
        }

        String validDate;
        String validTime;

        try {
            validDate = normalizeDate(newDate);
            validTime = normalizeTime(newTime);
        } catch (IllegalArgumentException e) {
            throw new AppointmentUnavailableException(
                    e.getMessage());
        }

        checkDoctorConflict(
                appointment.getDoctor().getId(),
                validDate,
                validTime,
                appointment.getAppointmentId());

        // Update the appointment
        appointment.setDate(validDate);
        appointment.setTime(validTime);

        // IMPORTANT: preserve this status for n8n
        appointment.setStatus("RESCHEDULED");

        System.out.println(
                "Appointment rescheduled successfully.");
    }

    // CANCEL
    public void cancelAppointment(String id) {
        Appointment appointment = findAppointmentById(id);

        if (appointment == null) {
            System.out.println("Appointment not found.");
            return;
        }

        if ("COMPLETED".equalsIgnoreCase(
                appointment.getStatus())) {
            System.out.println(
                    "Completed appointments cannot be cancelled.");
            return;
        }

        if ("CANCELLED".equalsIgnoreCase(
                appointment.getStatus())) {
            System.out.println(
                    "Appointment is already cancelled.");
            return;
        }

        appointment.setStatus("CANCELLED");

        System.out.println(
                "Appointment cancelled successfully.");
    }

    // COMPLETE
    public void completeAppointment(String id) {
        Appointment appointment = findAppointmentById(id);

        if (appointment == null) {
            System.out.println("Appointment not found.");
            return;
        }

        if ("CANCELLED".equalsIgnoreCase(
                appointment.getStatus())) {
            System.out.println(
                    "Cancelled appointments cannot be completed.");
            return;
        }

        if ("COMPLETED".equalsIgnoreCase(
                appointment.getStatus())) {
            System.out.println(
                    "Appointment is already completed.");
            return;
        }

        appointment.setStatus("COMPLETED");

        System.out.println(
                "Appointment completed successfully.");
    }

    // CHECK DOCTOR AVAILABILITY
    private void checkDoctorConflict(
            String doctorId,
            String date,
            String time,
            String excludedAppointmentId)
            throws AppointmentUnavailableException {

        for (Appointment existing : appointments) {

            // Ignore the appointment being rescheduled
            if (excludedAppointmentId != null
                    && existing.getAppointmentId()
                    .equalsIgnoreCase(excludedAppointmentId)) {
                continue;
            }

            String status = existing.getStatus();

            boolean active =
                    "CONFIRMED".equalsIgnoreCase(status)
                    || "RESCHEDULED".equalsIgnoreCase(status);

            if (!active) {
                continue;
            }

            boolean sameDoctor =
                    existing.getDoctor().getId()
                            .equalsIgnoreCase(doctorId);

            boolean sameDate =
                    existing.getDate().equals(date);

            boolean sameTime =
                    existing.getTime().equals(time);

            if (sameDoctor && sameDate && sameTime) {
                throw new AppointmentUnavailableException(
                        "Doctor is already booked on "
                                + date + " at " + time + ".");
            }
        }
    }

    // VALIDATE DATE
    private String normalizeDate(String date) {
        if (isBlank(date)) {
            throw new IllegalArgumentException(
                    "Date is required.");
        }

        try {
            return LocalDate.parse(
                    date.trim(), DATE_FORMAT).toString();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid date. Use YYYY-MM-DD, "
                            + "for example 2026-10-28.");
        }
    }

    // VALIDATE TIME
    // Accepts 11:30 AM or 11:30 in 24-hour format
    private String normalizeTime(String time) {
        if (isBlank(time)) {
            throw new IllegalArgumentException(
                    "Time is required.");
        }

        LocalTime parsedTime;

        try {
            parsedTime = LocalTime.parse(
                    time.trim(), TIME_12_HOUR);
        } catch (DateTimeParseException e) {
            try {
                parsedTime = LocalTime.parse(
                        time.trim(), TIME_24_HOUR);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException(
                        "Invalid time. Use HH:MM AM/PM "
                                + "or 24-hour HH:MM.");
            }
        }

        return parsedTime.format(DISPLAY_TIME);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
