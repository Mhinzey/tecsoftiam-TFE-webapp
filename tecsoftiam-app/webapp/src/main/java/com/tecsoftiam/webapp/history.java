package com.tecsoftiam.webapp;

import java.sql.Date;

/**
 * Object class for history 
 * Author: Deryck Olivier
 */
public class history {
    int id;
    Date date;
    String description;

    /**
     * Empty constructor
     */
    public history() {

    }

    /**
     * Constructor
     * 
     * @param id   history id
     * @param date history creation date
     * @param desc history description
     */
    public history(int id, Date date, String desc) {
        this.id = id;
        this.date = date;
        this.description = desc;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
