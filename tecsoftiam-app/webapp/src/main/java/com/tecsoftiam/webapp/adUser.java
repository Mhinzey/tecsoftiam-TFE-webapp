package com.tecsoftiam.webapp;

import java.util.List;

/**
 * This class represented ad ad User. Mainly used for displaying data and
 * database interactions
 * Author: Deryck Olivier
 */
public class adUser {
    private String displayName;
    private String mail;
    private String password;
    private String Name;
    private String nickName;
    private String id;

    List<String> roles;
    List<String> groups;

    /**
     * empty constructor
     */
    public adUser() {

    }


    /**
     * instantiate a adUser  with a list of roles
     * @param displayString user display name
     * @param mail user mail 
     * @param passwString user password
     * @param name user name
     * @param roles roles list
     */
    public adUser(String displayString, String mail, String passwString, String name, List<String> roles) {
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
    public adUser(String displayString, String mail, String passwString, String name, List<String> roles,
            List<String> groups) {
        this.displayName = displayString;
        this.nickName = displayString;
        this.mail = mail;
        this.password = passwString;
        this.Name = name;
        this.roles = roles;
        this.groups = groups;
    }

    public String getNickName() {
        return this.nickName;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getMail() {
        return this.mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return this.Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public List<String> getRoles() {
        return this.roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getGroups() {
        return this.groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

}
