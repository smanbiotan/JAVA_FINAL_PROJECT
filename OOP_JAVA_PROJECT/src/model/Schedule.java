package model;

import java.time.*;
import java.time.format.*;
import java.util.Locale;

/** A dated slot, dated time range, or recurring weekday time range. */
public class Schedule {
    private final String scheduleId, doctorId, date, time, endTime;
    private boolean available = true;
    private static final DateTimeFormatter TWELVE = new DateTimeFormatterBuilder()
        .parseCaseInsensitive().appendPattern("h:mm a").toFormatter(Locale.US)
        .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter TWENTY_FOUR = DateTimeFormatter.ofPattern("H:mm")
        .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("hh:mm a", Locale.US);

    public Schedule(String id, String doctor, String date, String time) {
        this(id, doctor, date, time, time);
    }
    public Schedule(String id, String doctor, String dayOrDate, String start, String end) {
        if (id == null || id.trim().isEmpty() || doctor == null || doctor.trim().isEmpty())
            throw new IllegalArgumentException("Schedule and doctor IDs are required.");
        if (dayOrDate == null) throw new IllegalArgumentException("Day or date is required.");
        scheduleId = id.trim(); doctorId = doctor.trim();
        String value = dayOrDate.trim().toUpperCase(Locale.ROOT);
        if (value.matches("[A-Z]+")) date = DayOfWeek.valueOf(value).name();
        else date = LocalDate.parse(value).toString();
        LocalTime from = parseTime(start), to = parseTime(end);
        if (to.isBefore(from)) throw new IllegalArgumentException("End time must not precede start time.");
        time = from.format(DISPLAY); endTime = to.format(DISPLAY);
    }
    public static LocalTime parseTime(String value) {
        if (value == null) throw new IllegalArgumentException("Time is required.");
        try { return LocalTime.parse(value.trim(), TWELVE); }
        catch (DateTimeParseException ex) { return LocalTime.parse(value.trim(), TWENTY_FOUR); }
    }
    public boolean covers(String requestedDate, String requestedTime) {
        LocalDate requested = LocalDate.parse(requestedDate);
        boolean dayMatches = date.equals(requested.toString()) || date.equals(requested.getDayOfWeek().name());
        LocalTime slot = parseTime(requestedTime), start = parseTime(time), end = parseTime(endTime);
        return available && dayMatches && (start.equals(end) ? slot.equals(start)
            : !slot.isBefore(start) && slot.isBefore(end));
    }
    public String getScheduleId() { return scheduleId; }
    public String getDoctorId() { return doctorId; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getEndTime() { return endTime; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    @Override public String toString() {
        return scheduleId + " | " + date + " | " + time
            + (time.equals(endTime) ? "" : " - " + endTime)
            + " | " + (available ? "Available" : "Unavailable");
    }
}
