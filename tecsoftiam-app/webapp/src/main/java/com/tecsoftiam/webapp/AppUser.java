package com.tecsoftiam.webapp;
import javax.persistence.*;
/**
 * User class used for login and data persistence
 * Author: Deryck Olivier
 */
@Entity
@Table(name = "users")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private long id;
    private String username;
    private String email, password;
    private boolean enabled;
    private String role;

    
    /** 
     * @return boolean
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    
    /** 
     * @return boolean
     */
    public boolean getEnabled() {
        return this.enabled;
    }

    
    /** 
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    
    /** 
     * @return String
     */
    public String getRole() {
        return this.role;
    }

    
    /** 
     * @param role
     */
    public void setRole(String role) {
        this.role = role;
    }

    
    /** 
     * @return String
     */
    public String getPassword() {
        return this.password;
    }

    
    /** 
     * @param Password
     */
    public void setPassword(String Password) {
        this.password = Password;
    }

   

    
    /** 
     * @return long
     */
    public long getId() {
        return this.id;
    }

    
    /** 
     * @param id
     */
    public void setId(long id) {
        this.id = id;
    }

    
    /** 
     * @return String
     */
    public String getUsername() {
        return this.username;
    }

    
    /** 
     * @param Username
     */
    public void setUsername(String Username) {
        this.username = Username;
    }

    
    /** 
     * @return String
     */
    public String getEmail() {
        return this.email;
    }

    
    /** 
     * @param Email
     */
    public void setEmail(String Email) {
        this.email = Email;
    }

  
    public AppUser(){

    }

    public AppUser(Long id, String Username,String Email){
        this.id=id;
        this.username=Username;
        this.email=Email;
    }
    
    /** 
     * @return String
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                
                '}';
    }

}
