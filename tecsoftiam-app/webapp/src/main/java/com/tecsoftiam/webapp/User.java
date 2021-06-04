package com.tecsoftiam.webapp;

public class User {
    private long id;
    private String Username;
    private String Email;
    private boolean done=true;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return this.Username;
    }

    public void setUsername(String Username) {
        this.Username = Username;
    }

    public String getEmail() {
        return this.Email;
    }

    public void setEmail(String Email) {
        this.Email = Email;
    }

    public boolean isDone() {
        return this.done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public User(){

    }

    public User(Long id, String Username,String Email){
        this.id=id;
        this.Username=Username;
        this.Email=Email;
    }
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + Username + '\'' +
                ", email='" + Email + '\'' +
                ", done=" + done +
                '}';
    }

}
