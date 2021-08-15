package com.tecsoftiam.webapp;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import com.microsoft.graph.models.DirectoryRole;
import com.microsoft.graph.models.User;

/**
 * This class represents a adChange object. It contains a status, and details about the change
 * Author: Deryck Olivier
 */
public class adChanges {

    Boolean refused = false;
    String cible;
    String type;
    String typeCible;
    String description;
    String status;

    /**
     * Constructor used to insert in the db
     * @param desc full description of the change
     * @param status validated or not
     */
    public adChanges(String desc, String status) {
        this.description = desc;
        this.status = status;
    }

    /**
     * empty constructor
     */
    public adChanges() {

    }

    /**
     * Constructor used for data display 
     * @param cible target of the change 
     * @param type change type
     * @param refused refused or not
     */
    public adChanges(String cible, String type, Boolean refused) {
        this.cible = cible;
        this.type = type;
        this.refused = refused;
    }

    
    /** 
     * @return Boolean
     */
    public Boolean isRefused() {
        return this.refused;
    }

    
    /** 
     * @return Boolean
     */
    public Boolean getRefused() {
        return this.refused;
    }

    
    /** 
     * @param refused
     */
    public void setRefused(Boolean refused) {
        this.refused = refused;
    }

    
    /** 
     * @return String
     */
    public String getCible() {
        return this.cible;
    }

    
    /** 
     * @param cible
     */
    public void setCible(String cible) {
        this.cible = cible;
    }

    
    /** 
     * @return String
     */
    public String getType() {
        return this.type;
    }

    
    /** 
     * @param Type
     */
    public void setType(String Type) {
        this.type = Type;
    }

    
    /** 
     * @return String
     */
    public String getTypeCible() {
        return this.typeCible;
    }

    
    /** 
     * @param typeCible
     */
    public void setTypeCible(String typeCible) {
        this.typeCible = typeCible;
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

    
    /** 
     * @return String
     */
    public String getStatus() {
        return this.status;
    }

    
    /** 
     * @param status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Apply change to the AD depending on the type
     * @throws IOException
     * @throws SQLException
     */
    public void applyChange() throws IOException, SQLException {
        dbConnect db = new dbConnect();
        Graph graph = new Graph();
        User user;
        DirectoryRole role;
        String templateId;
        String groupId;

        if (refused == true) { // if change refused
            switch (type) {
                case "Added user": // if user was added -> delete user
                    user = graph.getAdUserByDP(cible);
                    graph.deleteUser(user.id);
                    break;
                case "Removed user": // if user was removed -> recover in the AD
                    String userId = db.getUserId(cible);
                    graph.restoreObject(userId);
                    List<String> rolesToGrant = db.rolesOfUser(cible);
                    for (int i = 0; i < rolesToGrant.size(); i++) {
                        graph.grantRole(rolesToGrant.get(i), userId);
                    }
                    break;
                case "Added role": // if role was added -> remove that role
                    user = graph.getAdUserByDP(cible);
                    templateId = db.getRoletemplateId(typeCible);
                    role = graph.getRoleDetails(templateId);
                    graph.deleteRoleFrom(role.roleTemplateId, user.id);
                    break;
                case "Removed role": //if role was removed -> re-add that role
                    user = graph.getAdUserByDP(cible);
                    templateId = db.getRoletemplateId(typeCible);
                    role = graph.getRoleDetails(templateId);
                    graph.grantRole(role.roleTemplateId, user.id);
                    break;
                case "Added group": //if group was added -> remove that group
                    user = graph.getAdUserByDP(cible);
                    groupId = db.getGroupidByName(typeCible);
                    graph.deleteFromGroup(user.id, groupId);
                    break;
                case "Removed group": //if group was removed -> re-add that group
                    user = graph.getAdUserByDP(cible);
                    groupId = db.getGroupidByName(typeCible);
                    graph.addToGroup(user.id, groupId);
                    break;
            }
        }
    }

}
