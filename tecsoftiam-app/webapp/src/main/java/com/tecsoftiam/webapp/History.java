package com.tecsoftiam.webapp;

import java.sql.Date;

/**
 * Object class for history 
 * Author: Deryck Olivier
 */
public class History {
    int id;
    Date date;
    String description;

    /**
     * Empty constructor
     */
    public History() {

    }

    /**
     * Constructor
     * 
     * @param id   history id
     * @param date history creation date
     * @param desc history description
     */
    public History(int id, Date date, String desc) {
        this.id = id;
        this.date = date;
        this.description = desc;
    }

    
    /** 
     * @return int
     */
    public int getId() {
        return this.id;
    }

    
    /** 
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    
    /** 
     * @return Date
     */
    public Date getDate() {
        return this.date;
    }

    
    /** 
     * @param date
     */
    public void setDate(Date date) {
        this.date = date;
    }

    
    /** 
     * @return String
     */
    public String getDescription() {
        return this.description;
    }

    
    /** 
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

}
