package model;

public class EmailNotification implements Notification {

    @Override
    public void send(String recipient, String message) {

        System.out.println("\n===== EMAIL NOTIFICATION =====");
        System.out.println("To: " + recipient);
        System.out.println("Message: " + message);
        System.out.println("==============================");
    }
}