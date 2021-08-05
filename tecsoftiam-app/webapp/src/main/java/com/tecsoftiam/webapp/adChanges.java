package com.tecsoftiam.webapp;

import java.util.List;

public class adChanges {
    //List<String> addedUsers;
    //List<String> suppressedUsers;
    Boolean refused=false;
    String cible;
    String type;
    String typeCible;

    public adChanges(){

    }
    public adChanges(String cible, String type, Boolean refused){
        this.cible=cible;
        this.type=type;
        this.refused=refused;
    }
    public Boolean isRefused() {
        return this.refused;
    }

    public Boolean getRefused() {
        return this.refused;
    }

    public void setRefused(Boolean refused) {
        this.refused = refused;
    }

    public String getCible() {
        return this.cible;
    }

    public void setCible(String cible) {
        this.cible = cible;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String Type) {
        this.type = Type;
    }

    public String getTypeCible() {
        return this.typeCible;
    }

    public void setTypeCible(String typeCible) {
        this.typeCible = typeCible;
    }

    
}
