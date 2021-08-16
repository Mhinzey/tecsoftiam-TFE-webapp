package com.tecsoftiam.webapp;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Classe used to send the changes list ot the controller and display them in the web browser
 * Author: Deryck Olivier
 */
public class ChangeDetect {

    /**
     * empty constuctor
     * @throws IOException
     * @throws SQLException
     */
    public void Changedetect() throws IOException, SQLException {

    }

    /**
     * Obtain a list of all changes of the current ad
     * @return a list of all adChange
     * @throws SQLException
     * @throws IOException
     */
    public List<AdChanges> changesList() throws SQLException, IOException {
        Graph graph = new Graph();
        DbConnect db = new DbConnect();
        List<AdChanges> list = new ArrayList<AdChanges>();
        List<String> addedUsers = db.HasBeenAdded();
        List<String> removedUsers = db.Suppressed();
        Map<String, List<String>> addedRoles = db.addedRolesToUsers();
        Map<String, List<String>> removedRoles = db.removedRolesFromUsers();
        Map<String, List<String>> addedGroups = db.addedGroupsToUsers();
        Map<String, List<String>> removedGroups = db.removedGroupsFromUsers();
        for (int i = 0; i < addedUsers.size(); i++) { //added users
            AdChanges chang = new AdChanges();
            chang.setCible(addedUsers.get(i));
            chang.setType("Added user");
            list.add(chang);
        }

        for (int i = 0; i < removedUsers.size(); i++) { //removed users
            AdChanges chang = new AdChanges();
            chang.setCible(removedUsers.get(i));
            chang.setType("Removed user");
            list.add(chang);
        }
        addedRoles.forEach((key, value) -> { //added roles 
            for (int i = 0; i < value.size(); i++) {
                AdChanges chang = new AdChanges();
                chang.setCible(key);
                chang.setType("Added role");
                chang.setTypeCible(value.get(i));
                list.add(chang);
            }
        });
        removedRoles.forEach((key, value) -> { //removed roles
            for (int i = 0; i < value.size(); i++) {
                AdChanges chang = new AdChanges();
                chang.setCible(key);
                chang.setType("Removed role");
                chang.setTypeCible(value.get(i));
                list.add(chang);
            }
        });
        removedGroups.forEach((key, value) -> { //removed groups
            for (int i = 0; i < value.size(); i++) {
                AdChanges chang = new AdChanges();
                chang.setCible(key);
                chang.setType("Removed group");
                chang.setTypeCible(value.get(i));
                list.add(chang);
            }
        });
        addedGroups.forEach((key, value) -> { //added groups
            for (int i = 0; i < value.size(); i++) {
                AdChanges chang = new AdChanges();
                chang.setCible(key);
                chang.setType("Added group");
                chang.setTypeCible(value.get(i));
                list.add(chang);
            }
        });

        return list;

    }

    /**
     * Apply the changes to a list of changes
     * @param lst list of adChanges
     * @throws IOException
     * @throws SQLException
     */
    public void applyAllChanges(List<AdChanges> lst) throws IOException, SQLException {
        DbConnect db = new DbConnect();

        for (int i = 0; i < lst.size(); i++) {

            AdChanges current = lst.get(i);
            current.applyChange();

        }
        Date date = Date.valueOf(LocalDate.now());
        db.insertHistory(date, lst);
    }
}
