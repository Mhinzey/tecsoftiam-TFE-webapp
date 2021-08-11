package com.tecsoftiam.webapp;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.microsoft.graph.models.DirectoryRole;
import com.microsoft.graph.models.User;

public class adChanges {
    
    Boolean refused=false;
    String cible;
    String type;
    String typeCible;
    String description;
    String status;

  
    public adChanges(String desc, String status){
        this.description=desc;
        this.status=status;
    }
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
  public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public void applyChange() throws IOException, SQLException{
        dbConnect db=new dbConnect();
        Graph graph=new Graph();
        User user;
        DirectoryRole role;
        String templateId;
        String groupId;


        if(refused==true){ //if change refused
            switch(type){
                case "Added user": //if user was added -> delete user
                    user= graph.getAdUserByDP(cible);
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
                case "Added role":
                    user= graph.getAdUserByDP(cible);
                    templateId=db.getRoletemplateId(typeCible);
                    role=graph.getRoleDetails(templateId);
                    graph.deleteRoleFrom(role.roleTemplateId, user.id);
                    break;
                case "Removed role":
                    user= graph.getAdUserByDP(cible);
                    templateId=db.getRoletemplateId(typeCible);
                    role=graph.getRoleDetails(templateId);
                    graph.grantRole(role.roleTemplateId, user.id);
                    break;
                case "Added group":
                    user= graph.getAdUserByDP(cible);
                    groupId=db.getGroupidByName(typeCible);
                    graph.deleteFromGroup(user.id, groupId);
                break;
                case "Removed group":
                    user= graph.getAdUserByDP(cible);
                    groupId=db.getGroupidByName(typeCible);
                    graph.addToGroup(user.id, groupId);
                break;
            }
        }
    }
    
}
