package model;

public class SMSNotification implements Notification {

    @Override
    public void send(String recipient, String message) {

        System.out.println("\n===== SMS NOTIFICATION =====");
        System.out.println("To: " + recipient);
        System.out.println("Message: " + message);
        System.out.println("============================");
    }
}