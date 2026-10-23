package exception;

public class AppointmentUnavailableException extends Exception {

    public AppointmentUnavailableException(String message) {
        super(message);
    }
}