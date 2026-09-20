package model;

import java.util.ArrayList;
import java.util.List;

public class Doctor extends Person {

    private String specialization;

    // Aggregation relationship
    private List<Schedule> schedules;

    public Doctor(
            String id,
            String name,
            String phone,
            String email,
            String specialization) {

        super(id, name, phone, email);

        this.specialization = specialization;
        this.schedules = new ArrayList<>();
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void addSchedule(Schedule schedule) {
        schedules.add(schedule);
    }

    @Override
    public String getRole() {
        return "Doctor";
    }

    @Override
    public String toString() {
        return super.toString() +
                "\nSpecialization: " + specialization;
    }
}