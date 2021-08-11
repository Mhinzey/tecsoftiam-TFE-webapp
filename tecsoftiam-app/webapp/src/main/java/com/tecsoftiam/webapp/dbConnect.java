package com.tecsoftiam.webapp;

import java.io.IOException;
import java.sql.*;
import java.sql.Date;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import com.microsoft.graph.models.DirectoryAudit;
import com.microsoft.graph.models.DirectoryObject;
import com.microsoft.graph.models.DirectoryRole;
import com.microsoft.graph.models.Group;
import com.microsoft.graph.models.User;
import com.tecsoftiam.WebappApplication;

/** database class, where queries will be written */
public class dbConnect {
    final Properties properties = new Properties();
    //init app properties and db connection
    Connection connection;
    private Properties oAuthProperties = new Properties();
    String currentAD;
    String tenant;
    public dbConnect() throws IOException, SQLException {
        oAuthProperties.load(WebappApplication.class.getClassLoader().getResourceAsStream("oAuth.properties"));
        properties.load(WebappApplication.class.getClassLoader().getResourceAsStream("application.properties"));
        this.connection = DriverManager.getConnection(properties.getProperty("url"), properties);
        this.tenant = oAuthProperties.getProperty("app.tenant");
        this.currentAD = oAuthProperties.getProperty("app.scopename");
      
        
    }
    //return current ad Scope name
    private String getcurrentAd() throws SQLException{
        ResultSet set;
        PreparedStatement readStatement = connection.prepareStatement("SELECT scopeName FROM scopes where tenantId = ?");
        readStatement.setString(1, currentAD); 
        set=readStatement.executeQuery();               
        return set.getString("scopeName");
    }
  
    private static void insertData(AppUser user, Connection connection) throws SQLException {
      
        PreparedStatement insertStatement = connection
                .prepareStatement("INSERT INTO users (id, description, details, done) VALUES (?, ?, ?, ?) ;");

        insertStatement.setLong(1, user.getId());
        insertStatement.setString(2, user.getUsername());
        insertStatement.setString(3, user.getEmail());
        insertStatement.setBoolean(4, user.isEnabled());
        insertStatement.executeUpdate();
        
    }
    public void insertChange(String desc, String stat, int historyID) throws SQLException{
        PreparedStatement insertStatement = connection
        .prepareStatement("INSERT INTO adchange (description, status, historyID) VALUES (?, ?, ?) ;");

        insertStatement.setString(1, desc);
        insertStatement.setString(2, stat);
        insertStatement.setInt(3, historyID);
        System.out.println(insertStatement);
        insertStatement.executeUpdate();
    }
    public void insertHistory(Date date, List<adChanges> list) throws SQLException{
        PreparedStatement insertStatement = connection
                .prepareStatement("INSERT INTO history (date, scope) VALUES (?, ?) ;", Statement.RETURN_GENERATED_KEYS);
        insertStatement.setDate(1, date);
        insertStatement.setString(2, currentAD);
        System.out.println(insertStatement);
        insertStatement.executeUpdate();
        ResultSet rs = insertStatement.getGeneratedKeys();
        int id=0;
        String description;
        String state;
        adChanges current;
        if(rs.next())
        {
            id = rs.getInt(1);
        }
        for(int i=0; i<list.size();i++){
            current=list.get(i);
           if(current.typeCible!=null)
            description="object Name is :"+current.cible+" type of change is :"+current.type+" :"+current.typeCible;
            else description="object Name  is :"+current.cible+" type of change is: "+current.type;
            if(current.getRefused()==true) state="refused";
            else state="accepted";
            insertChange(description, state, id);
        }

    }

  
    //Read some data from db, was used for test purpose
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

    //insert ad user of current ad in db
    public  void Insertaduser(User user )throws SQLException{
        PreparedStatement insertStatement = connection
                .prepareStatement("INSERT IGNORE INTO adusers ( mail ,givenName,surname ,employeeId ,displayName ,createdDateTime ,country ,id ,lastPasswordChangeDateTime, scopeName ) VALUES (?, ?, ?, ?,?, ?, ?, ?,?,?);");
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
                insertStatement.setString(10, currentAD);

                insertStatement.executeUpdate();
                
            }
    //insert a list of users in db
    public  void InsertMultipleUsers(List<User> list ) throws SQLException{
        
        for (int i = 0; i < list.size(); i++) {
            Insertaduser(list.get(i));
        }
    }
  
    //return a List of String of users that has been added to the AD
    public List<String> HasBeenAdded() throws SQLException, IOException{ 
        Graph graphtest = new Graph();
        List<User> currentList = graphtest.getAdUserList();
        List<String> newUsersId=new ArrayList<String>();
        ResultSet set;
        for (int i = 0; i < currentList.size(); i++) {
            String userId= currentList.get(i).id;
            PreparedStatement readStatement = connection.prepareStatement("SELECT displayName FROM adusers where id = ?");
            readStatement.setString(1, userId); 
            set=readStatement.executeQuery();   
                   
            if(!set.next()){
              
                newUsersId.add(currentList.get(i).displayName);
            }
        }
        
        return newUsersId;
    }
    //Return a list of (String) user suppressed from the AD
    public List<String> Suppressed() throws SQLException, IOException{
        Graph graphtest = new Graph();
	
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
    //Detect if a user has been added to a group
    public List<String> addedToGroup(String id) throws SQLException{
        ResultSet set;
        List<String> added=new ArrayList<String>();
        List<String> indb=new ArrayList<String>();
        PreparedStatement readStatement = connection.prepareStatement("SELECT adusers.displayName FROM adgroup JOIN usergroup ON adgroup.id=usergroup.groupid JOIN adusers ON usergroup.userid = adusers.id where adgroup.scopeName =?  ");
        readStatement.setString(1, currentAD); 
        set=readStatement.executeQuery();
        while(set.next()){            
            indb.add(set.getString("displayName"));
               
          }  
        return indb;
    }
    //Insert a role in the db
    public void InsertRoleInDb(DirectoryRole role) throws SQLException{
        PreparedStatement insertStatement = connection
                .prepareStatement("INSERT IGNORE INTO adrole ( displayName, description, adId, roleTemplateId, scopeName ) VALUES (?, ?, ?, ?, ?) ;");
                
                insertStatement.setString(1, role.displayName);
                insertStatement.setString(2, role.description);
                insertStatement.setString(3, role.id);
                insertStatement.setString(4, role.roleTemplateId);
                insertStatement.setString(5, currentAD);
                insertStatement.executeUpdate();
                
    }
    //insert a group of the current ad in the db
    public void InsertGroupInDb(Group group) throws SQLException{
        PreparedStatement insertStatement = connection
        .prepareStatement("INSERT IGNORE INTO adgroup ( displayName, description, adId, mail, scopeName ) VALUES (?, ?, ?, ?, ?) ;");
        
        insertStatement.setString(1, group.displayName);
        insertStatement.setString(2, group.description);
        insertStatement.setString(3, group.id);
        insertStatement.setString(4, group.mail);
        insertStatement.setString(5, currentAD);
        insertStatement.executeUpdate();
        
}
    //Insert all groups of the current ad in the db
    public void insertAllgroups(List<Group> lst) throws SQLException{
        for (int i = 0; i < lst.size(); i++) {
            
            InsertGroupInDb(lst.get(i));
            
        }
    }
    
    //insert all roles of the current ad in the db
    public void insertAllDirectoryRoles(List<DirectoryRole> lst) throws SQLException{
        for (int i = 0; i < lst.size(); i++) {
            InsertRoleInDb(lst.get(i));
        }
    }

    
    //insert user logs of current ad in the db
    public void insertUserLogs(DirectoryAudit audit) throws ParseException, SQLException{
        PreparedStatement insertStatement = connection
        .prepareStatement("INSERT INTO useraddlogs ( date, userId, targetId, scopeName ) VALUES (?, ?, ?,?) ");
        LocalDate localDate=audit.activityDateTime.toLocalDate();
        //System.out.println(java.sql.Date.valueOf(localDate).toString()+"/"+audit.initiatedBy.user.id+"/"+audit.targetResources.get(0).id  );
        insertStatement.setDate(1,  java.sql.Date.valueOf(localDate));
        insertStatement.setString(2, audit.initiatedBy.user.id);
        insertStatement.setString(3, audit.targetResources.get(0).id);
        insertStatement.setString(4, currentAD);
        insertStatement.executeUpdate();
        //connection.close();
        
    }
    //insert all logs of the current ad in the db
    public void insertAllLogs(List<DirectoryAudit> lst) throws ParseException, SQLException{
        for (int i = 0; i < lst.size(); i++) {
            insertUserLogs(lst.get(i));
        }
    }
    //insert all created date of current ad users in the db
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
        
    }
   

    //match roles with users
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
            List<DirectoryObject> lst= graph.getUserRoles(rolesTemplates.get(i));
            
            for(int j = 0; j < lst.size(); j++){
                PreparedStatement insertStatement = connection
                      .prepareStatement("INSERT INTO roleuser ( roleTemplateId, userId ,scopeName) VALUES (?,?,?) ");
                insertStatement.setString(1,rolesTemplates.get(i) );
                insertStatement.setString(2, lst.get(j).id);
                insertStatement.setString(3, currentAD);
                insertStatement.executeUpdate();
                
            } 
        }            
       
    }
    //match group with users
    public void matchGroups() throws IOException, SQLException{
        Graph graph=new Graph();
        List<Group> groups=graph.getGroupsList();
    
        for(int i = 0; i < groups.size(); i++){
            List<DirectoryObject> lst= graph.membersOf(groups.get(i).id);
            
            for(int j = 0; j < lst.size(); j++){
                PreparedStatement insertStatement = connection
                      .prepareStatement("INSERT INTO usergroup ( groupid, userid ,scopeName) VALUES (?,?,?) ");
                insertStatement.setString(1,groups.get(i).id );
                insertStatement.setString(2, lst.get(j).id);
                insertStatement.setString(3, currentAD);
                insertStatement.executeUpdate();
                
            } 
        }     

    }
    public void Flushadgroup() throws SQLException{
        PreparedStatement delete = connection.prepareStatement("DELETE from adgroup where scopeName=?");
        delete.setString(1, currentAD);
        delete.executeUpdate();   
    }
    public void Flushadrole() throws SQLException{
        PreparedStatement delete = connection.prepareStatement("DELETE from adrole where scopeName=?");
        delete.setString(1, currentAD);
        delete.executeUpdate();   
    }
    public void Flushadusers() throws SQLException{
        PreparedStatement delete = connection.prepareStatement("DELETE from adusers where scopeName=?");
        delete.setString(1, currentAD);
        delete.executeUpdate();   
    }
    public void Flushroleuser() throws SQLException{
        PreparedStatement delete = connection.prepareStatement("DELETE from roleuser where scopeName=?");
        delete.setString(1, currentAD);
        delete.executeUpdate();   
    }
    public void Flushgroupuser() throws SQLException{
        PreparedStatement delete = connection.prepareStatement("DELETE from usergroup where scopeName=?");
        delete.setString(1, currentAD);
        delete.executeUpdate();   
    }

    //empty the olds data from db and add the actualised ones (used after a admin validation)
    public void refreshDb() throws SQLException, IOException, ParseException{
        Flushroleuser();
        Flushgroupuser();
        Flushadgroup();
        Flushadrole();
        Flushadusers();
        
        Graph graph=new Graph();
        InsertMultipleUsers(graph.getAdUserList());
        insertAllDirectoryRoles(graph.getDirectoryRoles());
        insertAllgroups(graph.getGroupsList());
        matchGroups();
        matchRoles();
        insertAllLogs(graph.getDirectoryAudits());
    }
    //get a list of scopes
    public List<Scope> getScopeList() throws SQLException{
        ResultSet set;
        PreparedStatement readStatement = connection.prepareStatement("SELECT  * FROM scopes");
        set=readStatement.executeQuery();
        List<Scope> list=new ArrayList<Scope>();
        while(set.next()){            
            Scope scope= new Scope(set.getInt("id"),set.getString("tenantId"),set.getString("password"),set.getString("scopeName"));
            list.add(scope);
        } 
        return list;
    }
    //return a scope from scopeName
    public Scope getScope(String name) throws SQLException, IOException{
        ResultSet set;
        Scope scope=new Scope();
        PreparedStatement readStatement = connection.prepareStatement("SELECT  * FROM scopes where scopeName=?");
        readStatement.setString(1, name);
        set=readStatement.executeQuery();
        while(set.next())
        {
          scope.setId(set.getInt("id"));
          scope.setTenantId(set.getString("tenantId"));
          scope.setPassword(set.getString("password"));
          scope.setScopeName(set.getString("scopename"));
        }
         
        return scope;

    }
    //ad scope in db
    public void addScope(String tenantId, String password, String scopeName) throws SQLException{
        PreparedStatement insertStatement = connection
        .prepareStatement("INSERT INTO scopes (tenantId, password, scopeName) VALUES (?, ?, ?);");

            insertStatement.setString(1,tenantId);
            insertStatement.setString(2, password);
            insertStatement.setString(3, scopeName);
            insertStatement.executeUpdate();
          
    }
    //delete scope from db
    public void deleteScope(String scopeName) throws SQLException{
        PreparedStatement preparedStatement = connection
        .prepareStatement("DELETE FROM scopes WHERE scopeName = ?");
        preparedStatement.setString(1, scopeName);
        preparedStatement.executeUpdate();
    }
    //hget user id from user name
    public String getUserId(String name) throws SQLException{
        ResultSet set;
        String id=null;
        PreparedStatement readStatement = connection.prepareStatement("SELECT  * FROM adusers where displayName=? and scopeName=?");
        readStatement.setString(1, name);
        readStatement.setString(2, currentAD);
        set=readStatement.executeQuery();
        while(set.next())
        {
          id=set.getString("id");
      
        }
        if(id==null) return "erreur no id found";
        return id;
    }
    public String getRoletemplateId(String name) throws SQLException
    {
        ResultSet set;
        String id=null;
        PreparedStatement readStatement = connection.prepareStatement("SELECT  * FROM adrole where displayName=? and scopeName=?");
        readStatement.setString(1, name);
        readStatement.setString(2, currentAD);
        set=readStatement.executeQuery();
        while(set.next())
        {
          id=set.getString("roleTemplateId");
      
        }
        if(id==null) return "erreur no id found";
        return id;
    }   
    public String getGroupidByName(String name) throws SQLException{
        ResultSet set;
        String id=null;
        PreparedStatement readStatement = connection.prepareStatement("SELECT  * FROM adgroup where displayName=? and scopeName=?");
        readStatement.setString(1, name);
        readStatement.setString(2, currentAD);
        set=readStatement.executeQuery();
        while(set.next())
        {
          id=set.getString("adId");
      
        }
        if(id==null) return "erreur no id found";
        return id;
    }
    //delete an history from selected scope
    public void deleteHistory(int id) throws SQLException{
        PreparedStatement delete = connection.prepareStatement("DELETE from history where id=? and scope=?");
        delete.setInt(1, id);
        delete.setString(2, currentAD);
        delete.executeUpdate();   
    }
    //return roles list of a user
    public List<String> rolesOfUser(String name) throws SQLException{
        ResultSet set;
        List<String> indb=new ArrayList<String>();
        PreparedStatement readStatement = connection.prepareStatement("SELECT adrole.displayName FROM adrole JOIN roleuser ON adrole.roleTemplateId=roleuser.roletemplateId JOIN adusers ON roleuser.userId = adusers.id where adrole.scopeName =? AND adusers.displayName=?  ");
        readStatement.setString(1, currentAD); 
        readStatement.setString(2, name); 
        set=readStatement.executeQuery();
        while(set.next()){            
            indb.add(set.getString("displayName"));
           
               
          }  
        return indb;
    }
    //Return a list of adusers
    public List<adUser> getUserList() throws SQLException{
        ResultSet set;
        adUser usr=new adUser();
        List<adUser> usersList=new ArrayList<adUser>();
        PreparedStatement readStatement = connection.prepareStatement("SELECT  dislayName, id FROM adusers where scopeName=?");
        readStatement.setString(1, currentAD);
        set=readStatement.executeQuery();
        while(set.next())
        {
            usr.setDisplayName(set.getString("displayName"));
            usr.setId(set.getString("id"));
            usersList.add(usr);
      
        }
        return usersList;
    }
    //return a list of history
    public List<history> getHistoryList() throws SQLException{
        ResultSet set;
        com.tecsoftiam.webapp.history hist=new com.tecsoftiam.webapp.history();
        List<com.tecsoftiam.webapp.history> historyList=new ArrayList<com.tecsoftiam.webapp.history>();
        PreparedStatement readStatement = connection.prepareStatement("SELECT  * FROM history where scope=?");
        readStatement.setString(1, currentAD);
        set=readStatement.executeQuery();
        while(set.next())
        {
            hist.setId(set.getInt("id"));
            hist.setDate(set.getDate("date"));
            hist.setDescription(set.getString("description"));
            historyList.add(hist);
      
        }
        return historyList;
    }
    //return a list of changes related to a history
    public List<adChanges> getChangesList(int id) throws SQLException{
        ResultSet set;
        
        List<adChanges> changeList=new ArrayList<adChanges>();
        PreparedStatement readStatement = connection.prepareStatement("SELECT  * FROM adchange where historyID= ?");
        readStatement.setInt(1, id);
        set=readStatement.executeQuery();
        while(set.next())
        {   
            adChanges change=new adChanges();
            change.setDescription(set.getString("description"));
            change.setStatus(set.getString("status"));
            changeList.add(change);
      
        }
        return changeList;
    }

    //return a list of role names added to a user
    public List<String> addedRoleToUser(User user) throws SQLException, IOException{
        Graph graph=new Graph();
        String displayName= user.displayName;
        String id= user.id;
        List<String> addedRoles=new ArrayList<String>();
        List<DirectoryRole> adRoles= graph.GetAllRoleFrom(id);
        List<String> dbRolesNames= rolesOfUser(displayName);
        for(int i=0;i<adRoles.size();i++){
            Boolean state=true;
            String currentRole= adRoles.get(i).displayName;
            for(int j=0; j<dbRolesNames.size() ;j++){
                if(currentRole.equals(dbRolesNames.get(j))){
                    state=false;
                }
            }
            if(state==true){
                addedRoles.add(currentRole);
            }
        }
        return addedRoles;

    }

    //return a map of <UserName, List<AddedRoles>
    public Map<String, List<String>>addedRolesToUsers() throws SQLException, IOException{
        Graph graph=new Graph();
        Map<String, List<String>> map=new HashMap<String, List<String>>();
        List<User> users= graph.getAdUserList();
        for(int i=0;i<users.size();i++){
            map.put(users.get(i).displayName, addedRoleToUser(users.get(i)));

        }
        return map;
    }

    //return a list of removed roles from a user
    public List<String> removedRolesUser(User user) throws IOException, SQLException{
        Graph graph=new Graph();
        String displayName= user.displayName;
        String id= user.id;
        List<String> removedRoles=new ArrayList<String>();
        List<DirectoryRole> adRoles= graph.GetAllRoleFrom(id);
        List<String> dbRolesNames= rolesOfUser(displayName);
        for(int i=0;i<dbRolesNames.size();i++){
            Boolean state=true;
            String currentRole= dbRolesNames.get(i);
            for(int j=0; j<adRoles.size() ;j++){
                if(currentRole.equals(adRoles.get(j).displayName)){
                    state=false;
                }
            }
            if(state==true){
                removedRoles.add(currentRole);
            }
        }
        return removedRoles;
    }
    //return a map of <UserName, List<RemovedRoles>
    public Map<String, List<String>>removedRolesFromUsers() throws SQLException, IOException{
        Graph graph=new Graph();
        Map<String, List<String>> map=new HashMap<String, List<String>>();
        List<User> users= graph.getAdUserList();
        for(int i=0;i<users.size();i++){
            map.put(users.get(i).displayName, removedRolesUser(users.get(i)));

        }
        return map;
    }
    //return a list of groups of a user
    public List<String> GroupsOfUser(String name) throws SQLException{
        ResultSet set;
        List<String> indb=new ArrayList<String>();
        PreparedStatement readStatement = connection.prepareStatement("SELECT adgroup.displayName FROM adgroup JOIN usergroup ON adgroup.adId=usergroup.groupid JOIN adusers ON usergroup.userId = adusers.id where adgroup.scopeName =? AND adusers.displayName=?  ");
        readStatement.setString(1, currentAD); 
        readStatement.setString(2, name); 
        set=readStatement.executeQuery();
        while(set.next()){            
            indb.add(set.getString("displayName"));             
          }  
        return indb;
    }
      //return a list of groups names added to a user
      public List<String> addedgroupToUser(User user) throws SQLException, IOException{
        Graph graph=new Graph();
        String displayName= user.displayName;
        String id= user.id;
        List<String> addedGroups=new ArrayList<String>();
        List<Group> adGroups= graph.groupsOf(id);
        List<String> dbGroupsNames= GroupsOfUser(displayName);
        for(int i=0;i<adGroups.size();i++){
            Boolean state=true;
            String currentRole= adGroups.get(i).displayName;
            for(int j=0; j<dbGroupsNames.size() ;j++){
                if(currentRole.equals(dbGroupsNames.get(j))){
                    state=false;
                }
            }
            if(state==true){
                addedGroups.add(currentRole);
            }
        }
        return addedGroups;

    }
    //return a map of <UserName, List<AddedRoles>
    public Map<String, List<String>>addedGroupsToUsers() throws SQLException, IOException{
        Graph graph=new Graph();
        Map<String, List<String>> map=new HashMap<String, List<String>>();
        List<User> users= graph.getAdUserList();
        for(int i=0;i<users.size();i++){
            map.put(users.get(i).displayName, addedgroupToUser(users.get(i)));

        }
        return map;
    }

    //return a list of removed roles from a user
    public List<String> removedGoupsUser(User user) throws IOException, SQLException{
        Graph graph=new Graph();
        String displayName= user.displayName;
        String id= user.id;
        List<String> removedGroups=new ArrayList<String>();
        List<Group> adGroups= graph.groupsOf(id);
        List<String> dbGroupNames= GroupsOfUser(displayName);
        for(int i=0;i<dbGroupNames.size();i++){
            Boolean state=true;
            String currentRole= dbGroupNames.get(i);
            for(int j=0; j<adGroups.size() ;j++){
                if(currentRole.equals(adGroups.get(j).displayName)){
                    state=false;
                }
            }
            if(state==true){
                removedGroups.add(currentRole);
            }
        }
        return removedGroups;
    }
    //return a map of <UserName, List<RemovedRoles>
    public Map<String, List<String>>removedGroupsFromUsers() throws SQLException, IOException{
        Graph graph=new Graph();
        Map<String, List<String>> map=new HashMap<String, List<String>>();
        List<User> users= graph.getAdUserList();
        for(int i=0;i<users.size();i++){
            map.put(users.get(i).displayName, removedGoupsUser(users.get(i)));

        }
        return map;
    }
}
