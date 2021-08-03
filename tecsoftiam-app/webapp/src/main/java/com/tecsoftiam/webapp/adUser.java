package com.tecsoftiam.webapp;

import java.util.List;

import com.microsoft.graph.models.DirectoryRole;

public class adUser {
    private String displayName;
    private String mail;
    private String password;
    private String Name;
    private String nickName;


    List<String> roles;
    List<String> groups;


    public adUser(){

    }
    public adUser(String displayString, String mail, String passwString, String name, List<String> roles){
        this.displayName=displayString;
        this.nickName=displayString;
        this.mail=mail;
        this.password=passwString;
        this.Name=name;
        this.roles=roles;
    }
    public adUser(String displayString, String mail, String passwString, String name, List<String> roles, List<String> groups){
        this.displayName=displayString;
        this.nickName=displayString;
        this.mail=mail;
        this.password=passwString;
        this.Name=name;
        this.roles=roles;
        this.groups=groups;
    }
        public String getNickName() {
        return this.nickName;
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
