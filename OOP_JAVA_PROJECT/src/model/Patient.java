package model;

public class Patient extends Person {

    private int age;
    private String gender;
    private String address;

    public Patient(
            String id,
            String name,
            String phone,
            String email,
            int age,
            String gender,
            String address) {

        super(id, name, phone, email);

        setAge(age);
        this.gender = gender;
        this.address = address;
    }

    public int getAge() {
    	
        return age;
    }

    public String getGender() {
    	
        return gender;
    }

    public String getAddress() {
    	
        return address;
    }

    public void setAge(int age) {
    	
        if (age <= 0) throw new IllegalArgumentException("Age must be greater than zero.");
        this.age = age;
    }

    public void setGender(String gender) {
    	
        this.gender = gender;
    }

    public void setAddress(String address) {
    	
        this.address = address;
    }

    @Override
    public String getRole() {
    	
        return "Patient";
    }

    @Override
    public String toString() {
        return super.toString() + "\nAge: " + age + "\nGender: " + gender + "\nAddress: " + address;
    }
}