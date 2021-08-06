package com.tecsoftiam.webapp;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.microsoft.graph.models.User;

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

    public void applyChange() throws IOException, SQLException{
        dbConnect db=new dbConnect();
        Graph graph=new Graph();
        if(refused==true){ //if change refused
            switch(type){
                case "Added user": //if user was added -> delete user
                    User user= graph.getAdUserByDP(cible);
                    graph.deleteUser(user.id);
                    break;
                case "Removed user": //if user was removed -> recover in the AD 
                    String userId=db.getUserId(cible);
                    graph.restoreObject(userId);
                    List<String> rolesToGrant=db.rolesOfUser(cible);
                    for(int i=0; i<rolesToGrant.size();i++){
                        graph.grantRole(rolesToGrant.get(i), userId);
                    }
                    break;
            }
        }
    }
    
}
