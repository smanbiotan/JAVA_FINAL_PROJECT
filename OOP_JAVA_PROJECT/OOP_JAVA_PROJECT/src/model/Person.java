package model;

public abstract class Person {

    private String id;
    private String name;
    private String phone;
    private String email;

    public Person(String id, String name, String phone, String email) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Abstraction
    public abstract String getRole();

    @Override
    public String toString() {
        return "ID: " + id +
                "\nName: " + name +
                "\nPhone: " + phone +
                "\nEmail: " + email;
    }
}