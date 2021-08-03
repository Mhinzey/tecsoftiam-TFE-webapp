package com.tecsoftiam.webapp;

import java.io.IOException;
import java.sql.*;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.logging.Logger;

import javax.servlet.jsp.jstl.sql.Result;

import com.microsoft.graph.models.DirectoryAudit;
import com.microsoft.graph.models.DirectoryObject;
import com.microsoft.graph.models.DirectoryRole;
import com.microsoft.graph.models.User;
import com.tecsoftiam.WebappApplication;

import org.h2.util.DateTimeUtils;
import org.simpleframework.xml.filter.SystemFilter;

/** database class, where queries will be written */
public class dbConnect {
    final Properties properties = new Properties();
    //init app properties and db connection
    Connection connection;
    public dbConnect() throws IOException, SQLException {
        properties.load(WebappApplication.class.getClassLoader().getResourceAsStream("application.properties"));
        this.connection = DriverManager.getConnection(properties.getProperty("url"), properties);
    }

    private static void insertData(AppUser user, Connection connection) throws SQLException {
      
        PreparedStatement insertStatement = connection
                .prepareStatement("INSERT INTO users (id, description, details, done) VALUES (?, ?, ?, ?);");

        insertStatement.setLong(1, user.getId());
        insertStatement.setString(2, user.getUsername());
        insertStatement.setString(3, user.getEmail());
        insertStatement.setBoolean(4, user.isEnabled());
        insertStatement.executeUpdate();
        connection.close();
    }

  

    AppUser readData( ) throws SQLException {
        System.out.println("Read data");
        PreparedStatement readStatement = connection.prepareStatement("SELECT * FROM users;");
        ResultSet resultSet = readStatement.executeQuery();
        if (!resultSet.next()) {
            System.out.println("There is no data in the database!");
            return null;
        }
        AppUser user = new AppUser();
        user.setId(resultSet.getLong("id_users"));
        user.setUsername(resultSet.getString("Username"));
        user.setEmail(resultSet.getString("Email"));

        System.out.println("Data read from the database: " + user.toString());
        return user;
    }

    public  void Insertaduser(User user )throws SQLException{
        PreparedStatement insertStatement = connection
                .prepareStatement("INSERT IGNORE INTO adusers ( mail ,givenName,surname ,employeeId ,displayName ,createdDateTime ,country ,id ,lastPasswordChangeDateTime) VALUES (?, ?, ?, ?,?, ?, ?, ?,?) ;");
                 Timestamp passChanged  =null;
                Timestamp createdAt=null;
                if(user.lastPasswordChangeDateTime != null){
                 passChanged = Timestamp.valueOf(user.lastPasswordChangeDateTime.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());}
                 if(user.createdDateTime != null){
                 createdAt=Timestamp.valueOf(user.createdDateTime.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());}
                insertStatement.setString(1, user.mail);
                insertStatement.setString(2, user.givenName);
                insertStatement.setString(3, user.surname);
                insertStatement.setString(4, user.employeeId);
                insertStatement.setString(5, user.displayName);
                insertStatement.setTimestamp(6, createdAt);
                insertStatement.setString(7, user.country);
                insertStatement.setString(8, user.id);
                insertStatement.setTimestamp(9, passChanged);

                insertStatement.executeUpdate();
                
            }
    
    public  void InsertMultipleUsers(List<User> list ) throws SQLException{
        
        for (int i = 0; i < list.size(); i++) {
            Insertaduser(list.get(i));
        }
    }

    //return a List of String of users that has been added to the AD
    public List<String> HasBeenAdded(List<User> Currentlist) throws SQLException{
        List<String> newUsersId=new ArrayList<String>();
        ResultSet set;
        for (int i = 0; i < Currentlist.size(); i++) {
            String userId= Currentlist.get(i).id;
            PreparedStatement readStatement = connection.prepareStatement("SELECT displayName FROM adusers where id = ?");
            readStatement.setString(1, userId); 
            set=readStatement.executeQuery();                  
            if(!set.next()){
                 
                newUsersId.add(Currentlist.get(i).displayName);
            }
        }
        connection.close();
        return newUsersId;
    }
    //Return a list of (String) user suppressed from the AD
    public List<String> Suppressed() throws SQLException, IOException{
        Graph graphtest = new Graph();
		graphtest.initializeGraphAuth();
        ResultSet set;
        List <User> usrlist=graphtest.getAdUserList();
        List <String> adUsers=new ArrayList<String>();
        List <String> dbUsers=new ArrayList<String>();
        for(int i = 0; i < usrlist.size(); i++){
            String userName= usrlist.get(i).displayName;
            adUsers.add(userName);
        }
        PreparedStatement readStatement = connection.prepareStatement("SELECT displayName FROM adusers");
        set=readStatement.executeQuery();
        while(set.next()){            
            String dpName = set.getString("displayName");
            dbUsers.add(dpName);          
          }  
        List<String> removed=new ArrayList<>();
        removed.addAll(dbUsers);
        removed.removeAll(adUsers);
        adUsers.removeAll(dbUsers);  
        connection.close();
        return removed;
    }

    public void InsertRoleInDb(DirectoryRole role) throws SQLException{
        PreparedStatement insertStatement = connection
                .prepareStatement("INSERT IGNORE INTO adrole ( displayName, description, adId, roleTemplateId ) VALUES (?, ?, ?, ?) ;");
                
                insertStatement.setString(1, role.displayName);
                insertStatement.setString(2, role.description);
                insertStatement.setString(3, role.id);
                insertStatement.setString(4, role.roleTemplateId);
                insertStatement.executeUpdate();
                
    }

    public void insertAllDirectoryRoles(List<DirectoryRole> lst) throws SQLException{
        for (int i = 0; i < lst.size(); i++) {
            InsertRoleInDb(lst.get(i));
        }
    }

    
    
    public void insertUserLogs(DirectoryAudit audit) throws ParseException, SQLException{
        PreparedStatement insertStatement = connection
        .prepareStatement("INSERT INTO useraddlogs ( date, userId, targetId ) VALUES (?, ?, ?) ");
        LocalDate localDate=audit.activityDateTime.toLocalDate();
        System.out.println(java.sql.Date.valueOf(localDate).toString()+"/"+audit.initiatedBy.user.id+"/"+audit.targetResources.get(0).id  );
        insertStatement.setDate(1,  java.sql.Date.valueOf(localDate));
        insertStatement.setString(2, audit.initiatedBy.user.id);
        insertStatement.setString(3, audit.targetResources.get(0).id);
        insertStatement.executeUpdate();
        //connection.close();
        
    }
    public void insertAllLogs(List<DirectoryAudit> lst) throws ParseException, SQLException{
        for (int i = 0; i < lst.size(); i++) {
            insertUserLogs(lst.get(i));
        }
    }
    public void insertCreatedDate() throws SQLException{
        Map<String, Date> hmap = new HashMap<String, Date>();
        ResultSet set;
        PreparedStatement readStatement = connection.prepareStatement("SELECT userId, date FROM useraddlogs");
        set=readStatement.executeQuery();
        while(set.next()){            
            String id = set.getString("userId");
            Date date= set.getDate("date");
            hmap.put(id, date);     
             
          } 
           
          for(String key : hmap.keySet()) {
            Date value = hmap.get(key);
            PreparedStatement updateStatement = connection.prepareStatement("UPDATE adusers set createdDateTime = ? where id= ?");
            updateStatement.setDate(1, value);
            updateStatement.setString(2, key);
            updateStatement.executeUpdate();
            
        }
        connection.close();
    }
   

    public void matchRoles() throws SQLException, IOException{
        ResultSet set;
        List<String> rolesTemplates= new ArrayList<String>();
        Graph graph=new Graph();
        PreparedStatement readStatement = connection.prepareStatement("SELECT  roleTemplateId FROM adrole");
        set=readStatement.executeQuery();
        while(set.next()){            
            rolesTemplates.add(set.getString("roleTemplateId"));          
          } 
         for(int i = 0; i < rolesTemplates.size(); i++){
             System.out.println(rolesTemplates.get(i));
            List<DirectoryObject> lst= graph.getUserRoles(rolesTemplates.get(i));
            
            for(int j = 0; j < lst.size(); j++){
                PreparedStatement insertStatement = connection
                      .prepareStatement("INSERT INTO roleuser ( roleTemplateId, userId ) VALUES (?,?) ");
                insertStatement.setString(1,rolesTemplates.get(i) );
                insertStatement.setString(2, lst.get(j).id);
                insertStatement.executeUpdate();
                
            } 
        }            
        connection.close();
    }
    public List<scope> getScope() throws SQLException{
        ResultSet set;
        PreparedStatement readStatement = connection.prepareStatement("SELECT  * FROM scopes");
        set=readStatement.executeQuery();
        List<scope> list=new ArrayList<scope>();
        while(set.next()){            
            scope scope= new scope(set.getInt("id"),set.getString("tenantId"),set.getString("password"),set.getString("scopeName"));
            list.add(scope);
        } 
        return list;
    }
    public void addScope(String tenantId, String password, String scopeName) throws SQLException{
        PreparedStatement insertStatement = connection
        .prepareStatement("INSERT INTO scopes (tenantId, password, scopeName) VALUES (?, ?, ?);");

            insertStatement.setString(1,tenantId);
            insertStatement.setString(2, password);
            insertStatement.setString(3, scopeName);
            insertStatement.executeUpdate();
            connection.close();
    }

    public void deleteScope(String scopeName) throws SQLException{
        PreparedStatement preparedStatement = connection
        .prepareStatement("DELETE FROM scopes WHERE scopeName = ?");
        preparedStatement.setString(1, scopeName);
        preparedStatement.executeUpdate();
    }
}
