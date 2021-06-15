package com.tecsoftiam.webapp;

import java.io.IOException;
import java.sql.*;
import java.sql.Date;
import java.time.ZoneOffset;
import java.util.*;
import java.util.logging.Logger;

import javax.servlet.jsp.jstl.sql.Result;

import com.microsoft.graph.models.DirectoryRole;
import com.microsoft.graph.models.User;

import org.h2.util.DateTimeUtils;

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
        System.out.println("Insert data");
        PreparedStatement insertStatement = connection
                .prepareStatement("INSERT INTO users (id, description, details, done) VALUES (?, ?, ?, ?);");

        insertStatement.setLong(1, user.getId());
        insertStatement.setString(2, user.getUsername());
        insertStatement.setString(3, user.getEmail());
        insertStatement.setBoolean(4, user.isDone());
        insertStatement.executeUpdate();
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
        return removed;
    }

    public void InsertRoleInDb(DirectoryRole role) throws SQLException{
        PreparedStatement insertStatement = connection
                .prepareStatement("INSERT IGNORE INTO adrole ( displayName, description, adId, roleTemplateId ) VALUES (?, ?, ?, ?) ;");
                
                insertStatement.setString(1, role.displayName);
                insertStatement.setString(2, role.description);
                insertStatement.setString(3, role.id);
                insertStatement.setString(4, role.roleTemplateId);
    }

    public void insertAllDirectoryRoles(List<DirectoryRole> lst) throws SQLException{
        for (int i = 0; i < lst.size(); i++) {
            InsertRoleInDb(lst.get(i));
        }
    }
    
   
}
