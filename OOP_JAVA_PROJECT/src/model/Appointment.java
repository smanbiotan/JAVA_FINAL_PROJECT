package model;

public class Appointment {

    private String appointmentId;
    private Patient patient;
    private Doctor doctor;
    private String date;
    private String time;
    private String reason;
    private String status;

    public Appointment(
            String appointmentId,
            Patient patient,
            Doctor doctor,
            String date,
            String time,
            String reason) {

        this.appointmentId = appointmentId;
        this.patient = patient;
        this.doctor = doctor;
        this.date = date;
        this.time = time;
        this.reason = reason;
        this.status = "CONFIRMED";
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public Patient getPatient() {
        return patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getReason() {
        return reason;
    }

    public String getStatus() {
        return status;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {

        return "\n================================" +
                "\nAppointment ID: " + appointmentId +
                "\nPatient: " + patient.getName() +
                "\nDoctor: " + doctor.getName() +
                "\nSpecialization: " +
                doctor.getSpecialization() +
                "\nDate: " + date +
                "\nTime: " + time +
                "\nReason: " + reason +
                "\nStatus: " + status +
                "\n================================";
    }
}