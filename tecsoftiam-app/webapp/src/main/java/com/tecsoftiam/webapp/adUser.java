package com.tecsoftiam.webapp;

import java.util.List;

/**
 * This class represented ad ad User. Mainly used for displaying data and
 * database interactions
 * Author: Deryck Olivier
 */
public class AdUser {
    private String displayName;
    private String mail;
    private String password;
    private String Name;
    private String nickName;
    private String id;
    private String domain;
    public List<String> roles;
    public List<String> groups;
    public String getDomain() {
        return this.domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    

    /**
     * empty constructor
     */
    public AdUser() {

    }


    /**
     * instantiate a adUser  with a list of roles
     * @param displayString user display name
     * @param mail user mail 
     * @param passwString user password
     * @param name user name
     * @param roles roles list
     */
    public AdUser(String displayString, String mail, String passwString, String name, List<String> roles) {
        this.displayName = displayString;
        this.nickName = displayString;
        this.mail = mail;
        this.password = passwString;
        this.Name = name;
        this.roles = roles;
    }

    /**
     * Instantiate a user with a list of role and a list of groups
     * @param displayString user display name
     * @param mail user mail 
     * @param passwString user password
     * @param name user name
     * @param roles roles list
     * @param groups groups list
     */
    public AdUser(String displayString, String mail, String passwString, String name, List<String> roles,
            List<String> groups) {
        this.displayName = displayString;
        this.nickName = displayString;
        this.mail = mail;
        this.password = passwString;
        this.Name = name;
        this.roles = roles;
        this.groups = groups;
    }

    
    /** 
     * @return String
     */
    public String getNickName() {
        return this.nickName;
    }

    
    /** 
     * @return String
     */
    public String getId() {
        return this.id;
    }

    
    /** 
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    
    /** 
     * @param nickName
     */
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    
    /** 
     * @return String
     */
    public String getDisplayName() {
        return this.displayName;
    }

    
    /** 
     * @param displayName
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    
    /** 
     * @return String
     */
    public String getMail() {
        return this.mail;
    }

    
    /** 
     * @param mail
     */
    public void setMail(String mail) {
        this.mail = mail;
    }

    
    /** 
     * @return String
     */
    public String getPassword() {
        return this.password;
    }

    
    /** 
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    
    /** 
     * @return String
     */
    public String getName() {
        return this.Name;
    }

    
    /** 
     * @param Name
     */
    public void setName(String Name) {
        this.Name = Name;
    }

    
    /** 
     * @return List<String>
     */
    public List<String> getRoles() {
        return this.roles;
    }

    
    /** 
     * @param roles
     */
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    
    /** 
     * @return List<String>
     */
    public List<String> getGroups() {
        return this.groups;
    }

    
    /** 
     * @param groups
     */
    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

}
