package model;

public class Schedule {

    private String scheduleId;
    private String doctorId;
    private String date;
    private String time;
    private boolean available;

    public Schedule(
            String scheduleId,
            String doctorId,
            String date,
            String time) {

        this.scheduleId = scheduleId;
        this.doctorId = doctorId;
        this.date = date;
        this.time = time;
        this.available = true;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "Schedule ID: " + scheduleId +
                "\nDoctor ID: " + doctorId +
                "\nDate: " + date +
                "\nTime: " + time +
                "\nAvailable: " + available;
    }
}