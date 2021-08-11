package com.tecsoftiam.webapp;

import java.sql.Date;

public class history {
    int id;
    Date date;
    String description;

    public history(){
        
    }
    public history(int id, Date date, String desc){
        this.id=id;
        this.date=date;
        this.description=desc;
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
