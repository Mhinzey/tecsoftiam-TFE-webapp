package com.tecsoftiam.webapp;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class changeDetect {



    public void changedetect() throws IOException, SQLException{
       
    }

    public List<adChanges> changesList() throws SQLException, IOException{
        Graph graph= new Graph();
        dbConnect db= new dbConnect();
        List<adChanges> list=new ArrayList<adChanges>();
        List<String> addedUsers= db.HasBeenAdded();
        List<String> removedUsers=db.Suppressed();
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

        return list;

    }

    public void applyChanges(List<adChanges> lst){
        for(int i=0;i<lst.size();i++){
            System.out.print(lst.get(i).getRefused());
        }
    }
}

  
