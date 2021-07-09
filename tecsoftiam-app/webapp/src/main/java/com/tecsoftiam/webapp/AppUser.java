package com.tecsoftiam.webapp;
import javax.persistence.*;
/**
 * User class (will probably change and get adapted)
 */
@Entity
@Table(name = "users")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private long id;
    private String username;
    private String email, password;

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String Password) {
        this.password = Password;
    }

    public boolean getDone() {
        return this.done;
    }

    private boolean done=true;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String Username) {
        this.username = Username;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String Email) {
        this.email = Email;
    }

    public boolean isDone() {
        return this.done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public AppUser(){

    }

    public AppUser(Long id, String Username,String Email){
        this.id=id;
        this.username=Username;
        this.email=Email;
    }
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", done=" + done +
                '}';
    }

}
