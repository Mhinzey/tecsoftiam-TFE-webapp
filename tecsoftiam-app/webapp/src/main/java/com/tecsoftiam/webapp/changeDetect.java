package com.tecsoftiam.webapp;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.microsoft.graph.models.User;

public class changeDetect {



    public void changedetect() throws IOException, SQLException{
       
    }

    public List<adChanges> changesList() throws SQLException, IOException{
        Graph graph= new Graph();
        dbConnect db= new dbConnect();
        List<adChanges> list=new ArrayList<adChanges>();
        List<String> addedUsers= db.HasBeenAdded();
        List<String> removedUsers=db.Suppressed();
        Map<String, List<String>> addedRoles=db.addedRolesToUsers();
        Map<String, List<String>> removedRoles=db.removedRolesFromUsers();
        Map<String, List<String>> addedGroups=db.addedGroupsToUsers();
        Map<String, List<String>> removedGroups=db.removedGroupsFromUsers();
        for(int i=0; i<addedUsers.size();i++){
            adChanges chang=new adChanges();
            chang.setCible(addedUsers.get(i));
            chang.setType("Added user");
            list.add(chang);
        }

        for(int i=0;i<removedUsers.size();i++){
            adChanges chang=new adChanges();
            chang.setCible(removedUsers.get(i));
            chang.setType("Removed user");
            list.add(chang);
        }
        addedRoles.forEach((key,value)->{
            for(int i=0;i<value.size();i++){
                adChanges chang=new adChanges();
                chang.setCible(key);
                chang.setType("Added role");
                chang.setTypeCible(value.get(i));
                list.add(chang);
            }
		});
        removedRoles.forEach((key,value)->{
            for(int i=0;i<value.size();i++){
                adChanges chang=new adChanges();
                chang.setCible(key);
                chang.setType("Removed role");
                chang.setTypeCible(value.get(i));
                list.add(chang);
            }
		});
        removedGroups.forEach((key,value)->{
            for(int i=0;i<value.size();i++){
                adChanges chang=new adChanges();
                chang.setCible(key);
                chang.setType("Removed group");
                chang.setTypeCible(value.get(i));
                list.add(chang);
            }
		});
        addedGroups.forEach((key,value)->{
            for(int i=0;i<value.size();i++){
                adChanges chang=new adChanges();
                chang.setCible(key);
                chang.setType("Added group");
                chang.setTypeCible(value.get(i));
                list.add(chang);
            }
		});

        return list;

    }
    //apply all changes 
    public void applyAllChanges(List<adChanges> lst) throws IOException, SQLException{
        dbConnect db=new dbConnect();
       
        for(int i=0;i<lst.size();i++){
            
            adChanges current=lst.get(i);
            current.applyChange();
            
        }
        Date date= Date.valueOf(LocalDate.now());
        db.insertHistory(date, lst);
    }
}



  
