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

/**
 * database class, where queries will be written Also contains methods that
 * compare data to the DB 
 * Author: Deryck Olivier
 */
public class DbConnect {
    final Properties properties = new Properties();
    // init app properties and db connection
    Connection connection;
    private Properties oAuthProperties = new Properties();
    String currentAD;
    String tenant;

    /**
     * Constructor, load properties from files and make connection with the db
     * 
     * @throws IOException  if cannot read properties files
     * @throws SQLException if cannot connect to the db
     */
    public DbConnect() throws IOException, SQLException {
        oAuthProperties.load(WebappApplication.class.getClassLoader().getResourceAsStream("oAuth.properties"));
        properties.load(WebappApplication.class.getClassLoader().getResourceAsStream("application.properties"));
        this.connection = DriverManager.getConnection(properties.getProperty("url"), properties);
        this.tenant = oAuthProperties.getProperty("app.tenant");
        this.currentAD = oAuthProperties.getProperty("app.scopename");

    }

    /**
     * Insert a AppUser in the db
     * 
     * @param user       app user to insert
     * @param connection connection to the db
     * @throws SQLException if can't connect to the db
     */
    private static void insertData(AppUser user, Connection connection) throws SQLException {

        PreparedStatement insertStatement = connection
                .prepareStatement("INSERT INTO users (id, description, details, done) VALUES (?, ?, ?, ?) ;");

        insertStatement.setLong(1, user.getId());
        insertStatement.setString(2, user.getUsername());
        insertStatement.setString(3, user.getEmail());
        insertStatement.setBoolean(4, user.isEnabled());
        insertStatement.executeUpdate();

    }

    /**
     * insert a change to the db
     * 
     * @param desc      description of the change
     * @param stat      status of the change
     * @param historyID history ID the change is a part of
     * @throws SQLException if cannot connect to the db
     */
    public void insertChange(String desc, String stat, int historyID) throws SQLException {
        PreparedStatement insertStatement = connection
                .prepareStatement("INSERT INTO adchange (description, status, historyID) VALUES (?, ?, ?) ;");

        insertStatement.setString(1, desc);
        insertStatement.setString(2, stat);
        insertStatement.setInt(3, historyID);
        System.out.println(insertStatement);
        insertStatement.executeUpdate();
    }

    /**
     * insert a History in the db
     * 
     * @param date date
     * @param list list of ad Changes related to that history
     * @throws SQLException if sql error
     */
    public void insertHistory(Date date, List<AdChanges> list) throws SQLException {
        PreparedStatement insertStatement = connection
                .prepareStatement("INSERT INTO history (date, scope) VALUES (?, ?) ;", Statement.RETURN_GENERATED_KEYS);
        insertStatement.setDate(1, date);
        insertStatement.setString(2, currentAD);
        System.out.println(insertStatement);
        insertStatement.executeUpdate();
        ResultSet rs = insertStatement.getGeneratedKeys();
        int id = 0;
        String description;
        String state;
        AdChanges current;
        if (rs.next()) {
            id = rs.getInt(1);
        }
        for (int i = 0; i < list.size(); i++) {
            current = list.get(i);
            if (current.typeCible != null)
                description = "object Name is :" + current.cible + " type of change is :" + current.type + " :"
                        + current.typeCible;
            else
                description = "object Name  is :" + current.cible + " type of change is: " + current.type;
            if (current.getRefused() == true)
                state = "refused";
            else
                state = "accepted";
            insertChange(description, state, id);
        }

    }

    /**
     * Read data from the database, was mainly used for test purpose
     */
    AppUser readData() throws SQLException {
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

    /**
     * insert a ad user in the db
     * 
     * @param user graph User
     * @throws SQLException
     */
    public void Insertaduser(User user) throws SQLException {
        PreparedStatement insertStatement = connection.prepareStatement(
                "INSERT IGNORE INTO adusers ( mail ,givenName,surname ,employeeId ,displayName ,createdDateTime ,country ,id ,lastPasswordChangeDateTime, scopeName ) VALUES (?, ?, ?, ?,?, ?, ?, ?,?,?);");
        Timestamp passChanged = null;
        Timestamp createdAt = null;
        if (user.lastPasswordChangeDateTime != null) {
            passChanged = Timestamp
                    .valueOf(user.lastPasswordChangeDateTime.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
        }
        if (user.createdDateTime != null) {
            createdAt = Timestamp.valueOf(user.createdDateTime.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
        }
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

    /**
     * insert multiple users in the db
     * 
     * @param list a list of Graph User
     * @throws SQLException
     */
    public void InsertMultipleUsers(List<User> list) throws SQLException {

        for (int i = 0; i < list.size(); i++) {
            Insertaduser(list.get(i));
        }
    }

    /**
     * Detect which users have been added in the AD compared to the db
     * 
     * @return a list of users name
     * @throws SQLException if cannot connect to the db
     * @throws IOException  if cannot read properties file
     */
    public List<String> HasBeenAdded() throws SQLException, IOException {
        Graph graphtest = new Graph();
        List<User> currentList = graphtest.getAdUserList();
        List<String> newUsersId = new ArrayList<String>();
        ResultSet set;
        for (int i = 0; i < currentList.size(); i++) {
            String userId = currentList.get(i).id;
            PreparedStatement readStatement = connection
                    .prepareStatement("SELECT displayName FROM adusers where id = ? and scopeName=?");
            readStatement.setString(1, userId);
            readStatement.setString(2, currentAD);
            set = readStatement.executeQuery();

            if (!set.next()) {

                newUsersId.add(currentList.get(i).displayName);
            }
        }

        return newUsersId;
    }

    /**
     * Detect which users have been suppressed from the ad
     * 
     * @return a list of user names
     * @throws SQLException if cannot connect to the db
     * @throws IOException  if cannot read properties file
     */
    public List<String> Suppressed() throws SQLException, IOException {
        Graph graphtest = new Graph();

        ResultSet set;
        List<User> usrlist = graphtest.getAdUserList();
        List<String> adUsers = new ArrayList<String>();
        List<String> dbUsers = new ArrayList<String>();
        for (int i = 0; i < usrlist.size(); i++) {
            String userName = usrlist.get(i).displayName;
            adUsers.add(userName);
        }
        PreparedStatement readStatement = connection.prepareStatement("SELECT displayName FROM adusers where scopeName=?");
        readStatement.setString(1, currentAD);
        set = readStatement.executeQuery();
        while (set.next()) {
            String dpName = set.getString("displayName");
            dbUsers.add(dpName);
        }
        List<String> removed = new ArrayList<>();
        removed.addAll(dbUsers);
        removed.removeAll(adUsers);
        adUsers.removeAll(dbUsers);

        return removed;
    }

    /**
     * Detect if a user has been added to a group by comparing the DB to the AD
     * 
     * @param id user id
     * @return list of
     * @throws SQLException
     */
    public List<String> addedToGroup(String id) throws SQLException {
        ResultSet set;
        List<String> added = new ArrayList<String>();
        List<String> indb = new ArrayList<String>();
        PreparedStatement readStatement = connection.prepareStatement(
                "SELECT adusers.displayName FROM adgroup JOIN usergroup ON adgroup.id=usergroup.groupid JOIN adusers ON usergroup.userid = adusers.id where adgroup.scopeName =?  ");
        readStatement.setString(1, currentAD);
        set = readStatement.executeQuery();
        while (set.next()) {
            indb.add(set.getString("displayName"));

        }
        return indb;
    }

    /**
     * Insert a role in the databse
     * 
     * @param role Graph Directory Role
     * @throws SQLException
     */
    public void InsertRoleInDb(DirectoryRole role) throws SQLException {
        PreparedStatement insertStatement = connection.prepareStatement(
                "INSERT IGNORE INTO adrole ( displayName, description, adId, roleTemplateId, scopeName ) VALUES (?, ?, ?, ?, ?) ;");

        insertStatement.setString(1, role.displayName);
        insertStatement.setString(2, role.description);
        insertStatement.setString(3, role.id);
        insertStatement.setString(4, role.roleTemplateId);
        insertStatement.setString(5, currentAD);
        insertStatement.executeUpdate();

    }

    /**
     * Insert a group in the db
     * 
     * @param group Graph Group
     * @throws SQLException
     */
    public void InsertGroupInDb(Group group) throws SQLException {
        PreparedStatement insertStatement = connection.prepareStatement(
                "INSERT IGNORE INTO adgroup ( displayName, description, adId, mail, scopeName ) VALUES (?, ?, ?, ?, ?) ;");

        insertStatement.setString(1, group.displayName);
        insertStatement.setString(2, group.description);
        insertStatement.setString(3, group.id);
        insertStatement.setString(4, group.mail);
        insertStatement.setString(5, currentAD);
        insertStatement.executeUpdate();

    }

    /**
     * Insert all groups in the db
     * 
     * @param lst a List of groups to add
     * @throws SQLException
     */
    public void insertAllgroups(List<Group> lst) throws SQLException {
        for (int i = 0; i < lst.size(); i++) {

            InsertGroupInDb(lst.get(i));

        }
    }

    /**
     * Insert all DirectoryRoles in the db
     * 
     * @param lst a List of DirectoryRole
     * @throws SQLException
     */
    public void insertAllDirectoryRoles(List<DirectoryRole> lst) throws SQLException {
        for (int i = 0; i < lst.size(); i++) {
            InsertRoleInDb(lst.get(i));
        }
    }

    /**
     * Insert Directory audit in the db. A Directory audit is used to get the
     * created date of a user
     * 
     * @param audit Directory Audit (log from azure)
     * @throws ParseException
     * @throws SQLException
     */
    public void insertUserLogs(DirectoryAudit audit) throws ParseException, SQLException {
        PreparedStatement insertStatement = connection
                .prepareStatement("INSERT INTO useraddlogs ( date, userId, targetId, scopeName ) VALUES (?, ?, ?,?) ");
        LocalDate localDate = audit.activityDateTime.toLocalDate();
        insertStatement.setDate(1, java.sql.Date.valueOf(localDate));
        insertStatement.setString(2, audit.initiatedBy.user.id);
        insertStatement.setString(3, audit.targetResources.get(0).id);
        insertStatement.setString(4, currentAD);
        insertStatement.executeUpdate();

    }

    /**
     * Insert all logs in the db
     * 
     * @param lst a list of DirectoryAudit
     * @throws ParseException
     * @throws SQLException
     */
    public void insertAllLogs(List<DirectoryAudit> lst) throws ParseException, SQLException {
        for (int i = 0; i < lst.size(); i++) {
            insertUserLogs(lst.get(i));
        }
    }

    /**
     * Insert the created date from userlogs to users
     * 
     * @throws SQLException
     */
    public void insertCreatedDate() throws SQLException {
        Map<String, Date> hmap = new HashMap<String, Date>();
        ResultSet set;
        PreparedStatement readStatement = connection.prepareStatement("SELECT userId, date FROM useraddlogs");
        set = readStatement.executeQuery();
        while (set.next()) {
            String id = set.getString("userId");
            Date date = set.getDate("date");
            hmap.put(id, date);

        }

        for (String key : hmap.keySet()) {
            Date value = hmap.get(key);
            PreparedStatement updateStatement = connection
                    .prepareStatement("UPDATE adusers set createdDateTime = ? where id= ?");
            updateStatement.setDate(1, value);
            updateStatement.setString(2, key);
            updateStatement.executeUpdate();

        }

    }

    /**
     * match role with users, fill the table between users and role, because the
     * relationship is n:n
     * 
     * @throws SQLException
     * @throws IOException
     */
    public void matchRoles() throws SQLException, IOException {
        ResultSet set;
        List<String> rolesTemplates = new ArrayList<String>();
        Graph graph = new Graph();
        PreparedStatement readStatement = connection.prepareStatement("SELECT  roleTemplateId FROM adrole where scopeName=?");
        readStatement.setString(1, currentAD);
        set = readStatement.executeQuery();
        while (set.next()) {
            rolesTemplates.add(set.getString("roleTemplateId"));
        }
        for (int i = 0; i < rolesTemplates.size(); i++) {
            List<DirectoryObject> lst = graph.getUserRoles(rolesTemplates.get(i));

            for (int j = 0; j < lst.size(); j++) {
                PreparedStatement readStatement2 = connection.prepareStatement("SELECT  id FROM adrole where roleTemplateId=? and scopeName=?");
                readStatement2.setString(1, rolesTemplates.get(i));
                readStatement2.setString(2, currentAD);
                set = readStatement2.executeQuery();
                int id=0;
                while (set.next()) {
                    id=set.getInt("id");
                }
                PreparedStatement insertStatement = connection
                        .prepareStatement("INSERT INTO roleuser ( roleId, userId ,scopeName) VALUES (?,?,?) ");
                insertStatement.setInt(1, id);
                insertStatement.setString(2, lst.get(j).id);
                insertStatement.setString(3, currentAD);
                insertStatement.executeUpdate();

            }
        }

    }

    /**
     * match groups with users, fill the table between groups and users because the
     * relationship is n:n
     * 
     * @throws IOException
     * @throws SQLException
     */
    public void matchGroups() throws IOException, SQLException {
        Graph graph = new Graph();
        List<Group> groups = graph.getGroupsList();

        for (int i = 0; i < groups.size(); i++) {
            List<DirectoryObject> lst = graph.membersOf(groups.get(i).id);

            for (int j = 0; j < lst.size(); j++) {
                PreparedStatement insertStatement = connection
                        .prepareStatement("INSERT INTO usergroup ( groupid, userid ,scopeName) VALUES (?,?,?) ");
                insertStatement.setString(1, groups.get(i).id);
                insertStatement.setString(2, lst.get(j).id);
                insertStatement.setString(3, currentAD);
                insertStatement.executeUpdate();

            }
        }

    }

    /**
     * empty the adgroup table for the current scope
     */
    public void Flushadgroup() throws SQLException {
        PreparedStatement delete = connection.prepareStatement("DELETE from adgroup where scopeName=?");
        delete.setString(1, currentAD);
        delete.executeUpdate();
    }

    /**
     * empty the adrole table for the current scope
     * 
     * @throws SQLException
     */
    public void Flushadrole() throws SQLException {
        PreparedStatement delete = connection.prepareStatement("DELETE from adrole where scopeName=?");
        delete.setString(1, currentAD);
        delete.executeUpdate();
    }

    /**
     * empty the adusers table for the current scope
     * 
     * @throws SQLException
     */
    public void Flushadusers() throws SQLException {
        PreparedStatement delete = connection.prepareStatement("DELETE from adusers where scopeName=?");
        delete.setString(1, currentAD);
        delete.executeUpdate();
    }

    /**
     * empty the roleuser table for the current scope
     * 
     * @throws SQLException
     */
    public void Flushroleuser() throws SQLException {
        PreparedStatement delete = connection.prepareStatement("DELETE from roleuser where scopeName=?");
        delete.setString(1, currentAD);
        delete.executeUpdate();
    }

    /**
     * empty the groupuser table for the current scope
     * 
     * @throws SQLException
     */
    public void Flushgroupuser() throws SQLException {
        PreparedStatement delete = connection.prepareStatement("DELETE from usergroup where scopeName=?");
        delete.setString(1, currentAD);
        delete.executeUpdate();
    }

    /**
     * refresh the datable by deleted the old elements for the selected AD and
     * insert the new ones
     * 
     * @throws SQLException
     * @throws IOException
     * @throws ParseException
     */
    public void refreshDb() throws SQLException, IOException, ParseException {
        Flushroleuser();
        Flushgroupuser();
        Flushadgroup();
        Flushadrole();
        Flushadusers();

        Graph graph = new Graph();
        InsertMultipleUsers(graph.getAdUserList());
        insertAllDirectoryRoles(graph.getDirectoryRoles());
        insertAllgroups(graph.getGroupsList());
        matchGroups();
        matchRoles();
        //insertAllLogs(graph.getDirectoryAudits());
    }

    /**
     * get a list of scope
     * 
     * @return List of scope
     * @throws SQLException
     */
    public List<Scope> getScopeList() throws SQLException {
        ResultSet set;
        PreparedStatement readStatement = connection.prepareStatement("SELECT  * FROM scopes");
        set = readStatement.executeQuery();
        List<Scope> list = new ArrayList<Scope>();
        while (set.next()) {
            Scope scope = new Scope(set.getInt("id"), set.getString("tenantId"), set.getString("password"),
                    set.getString("scopeName"), set.getString("appId"));
            list.add(scope);
        }
        return list;
    }

    /**
     * get scope details for specified scope
     * 
     * @param name name of the scope
     * @return a scope object
     * @throws SQLException
     * @throws IOException
     */
    public Scope getScope(String name) throws SQLException, IOException {
        ResultSet set;
        Scope scope = new Scope();
        PreparedStatement readStatement = connection.prepareStatement("SELECT  * FROM scopes where scopeName=?");
        readStatement.setString(1, name);
        set = readStatement.executeQuery();
        while (set.next()) {
            scope.setId(set.getInt("id"));
            scope.setTenantId(set.getString("tenantId"));
            scope.setPassword(set.getString("password"));
            scope.setScopeName(set.getString("scopename"));
            scope.setAppId(set.getString("appId"));
        }

        return scope;

    }

    /**
     * add a scope in the db
     * 
     * @param tenantId  tenant id, needed to access an Active directory
     * @param password  tenant password, needed to acces an active directory
     * @param scopeName name of the scope
     * @throws SQLException
     */
    public void addScope(String tenantId, String password, String scopeName, String appid) throws SQLException {
        PreparedStatement insertStatement = connection
                .prepareStatement("INSERT INTO scopes (tenantId, password, scopeName, appId) VALUES (?, ?, ?, ?);");

        insertStatement.setString(1, tenantId);
        insertStatement.setString(2, password);
        insertStatement.setString(3, scopeName);
        insertStatement.setString(4, appid);
        insertStatement.executeUpdate();

    }

    /**
     * delete a scope from the db
     * 
     * @param scopeName name of the scope
     * @throws SQLException
     */
    public void deleteScope(String scopeName) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM scopes WHERE scopeName = ?");
        preparedStatement.setString(1, scopeName);
        preparedStatement.executeUpdate();
    }

    /**
     * get a user Id
     * 
     * @param name user name
     * @return the user id
     * @throws SQLException
     */
    public String getUserId(String name) throws SQLException {
        ResultSet set;
        String id = null;
        PreparedStatement readStatement = connection
                .prepareStatement("SELECT  * FROM adusers where displayName=? and scopeName=?");
        readStatement.setString(1, name);
        readStatement.setString(2, currentAD);
        set = readStatement.executeQuery();
        while (set.next()) {
            id = set.getString("id");

        }
        if (id == null)
            return "erreur no id found";
        return id;
    }

    /**
     * get role template id
     * 
     * @param name the role name
     * @return string of role template id
     * @throws SQLException
     */
    public String getRoletemplateId(String name) throws SQLException {
        ResultSet set;
        String id = null;
        PreparedStatement readStatement = connection
                .prepareStatement("SELECT  * FROM adrole where displayName=?");
        readStatement.setString(1, name);
        
        set = readStatement.executeQuery();
        while (set.next()) {
            id = set.getString("roleTemplateId");

        }
        if (id == null)
            return "erreur no id found";
        return id;
    }

    /**
     * get group id
     * 
     * @param name name of the group
     * @return id of the group
     * @throws SQLException
     */
    public String getGroupidByName(String name) throws SQLException {
        ResultSet set;
        String id = null;
        PreparedStatement readStatement = connection
                .prepareStatement("SELECT  * FROM adgroup where displayName=? and scopeName=?");
        readStatement.setString(1, name);
        readStatement.setString(2, currentAD);
        set = readStatement.executeQuery();
        while (set.next()) {
            id = set.getString("adId");

        }
        if (id == null)
            return "erreur no id found";
        return id;
    }

    /**
     * delete history from the db
     * 
     * @param id id of the history
     * @throws SQLException
     */
    public void deleteHistory(int id) throws SQLException {
        PreparedStatement delete = connection.prepareStatement("DELETE from history where id=? and scope=?");
        delete.setInt(1, id);
        delete.setString(2, currentAD);
        delete.executeUpdate();
    }

    /**
     * get roles of a user
     * 
     * @param name name of user
     * @return list of roles assigned to the user
     * @throws SQLException
     */
    public List<String> rolesOfUser(String name) throws SQLException {
        ResultSet set;
        List<String> indb = new ArrayList<String>();
        PreparedStatement readStatement = connection.prepareStatement(
                "SELECT adrole.displayName FROM adrole JOIN roleuser ON adrole.id=roleuser.roleId JOIN adusers ON roleuser.userId = adusers.id where adrole.scopeName =? AND adusers.displayName=?  ");
        readStatement.setString(1, currentAD);
        readStatement.setString(2, name);
        set = readStatement.executeQuery();
        while (set.next()) {
            indb.add(set.getString("displayName"));

        }
        return indb;
    }

    /**
     * get a list of all users for selected scope
     * 
     * @return a list of users
     * @throws SQLException
     */
    public List<AdUser> getUserList() throws SQLException {
        ResultSet set;
        AdUser usr = new AdUser();
        List<AdUser> usersList = new ArrayList<AdUser>();
        PreparedStatement readStatement = connection
                .prepareStatement("SELECT  dislayName, id FROM adusers where scopeName=?");
        readStatement.setString(1, currentAD);
        set = readStatement.executeQuery();
        while (set.next()) {
            usr.setDisplayName(set.getString("displayName"));
            usr.setId(set.getString("id"));
            usersList.add(usr);

        }
        return usersList;
    }

    /**
     * get history liste
     * 
     * @return a list of history
     * @throws SQLException
     */
    public List<History> getHistoryList() throws SQLException {
        ResultSet set;
        
        List<com.tecsoftiam.webapp.History> historyList = new ArrayList<com.tecsoftiam.webapp.History>();
        PreparedStatement readStatement = connection.prepareStatement("SELECT  * FROM history where scope=?");
        readStatement.setString(1, currentAD);
        set = readStatement.executeQuery();
        while (set.next()) {
            com.tecsoftiam.webapp.History hist = new com.tecsoftiam.webapp.History();
            hist.setId(set.getInt("id"));
            hist.setDate(set.getDate("date"));
            hist.setDescription(set.getString("description"));
            historyList.add(hist);

        }
        return historyList;
    }

    /**
     * get a list of changes
     * 
     * @param id historyID to get related changes
     * @return a list of adChange
     * @throws SQLException
     */
    public List<AdChanges> getChangesList(int id) throws SQLException {
        ResultSet set;

        List<AdChanges> changeList = new ArrayList<AdChanges>();
        PreparedStatement readStatement = connection.prepareStatement("SELECT  * FROM adchange where historyID= ?");
        readStatement.setInt(1, id);
        set = readStatement.executeQuery();
        while (set.next()) {
            AdChanges change = new AdChanges();
            change.setDescription(set.getString("description"));
            change.setStatus(set.getString("status"));
            changeList.add(change);

        }
        return changeList;
    }

    /**
     * check what roles have been added to a user
     * 
     * @param user a graph user
     * @return a list ofadded roles names
     * @throws SQLException
     * @throws IOException
     */
    public List<String> addedRoleToUser(User user) throws SQLException, IOException {
        Graph graph = new Graph();
        String displayName = user.displayName;
        String id = user.id;
        List<String> addedRoles = new ArrayList<String>();
        List<DirectoryRole> adRoles = graph.GetAllRoleFrom(id);
        List<String> dbRolesNames = rolesOfUser(displayName);
        for (int i = 0; i < adRoles.size(); i++) {
            Boolean state = true;
            String currentRole = adRoles.get(i).displayName;
            for (int j = 0; j < dbRolesNames.size(); j++) {
                if (currentRole.equals(dbRolesNames.get(j))) {
                    state = false;
                }
            }
            if (state == true) {
                addedRoles.add(currentRole);
            }
        }
        return addedRoles;

    }

    /**
     * Get all the added roles for all users of current ad
     * 
     * @return a map containing all roles added for every users
     * @throws SQLException
     * @throws IOException
     */
    public Map<String, List<String>> addedRolesToUsers() throws SQLException, IOException {
        Graph graph = new Graph();
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<User> users = graph.getAdUserList();
        for (int i = 0; i < users.size(); i++) {
            map.put(users.get(i).displayName, addedRoleToUser(users.get(i)));

        }
        return map;
    }

    /**
     * Check the roles suppressed from a user
     * 
     * @param user Graph user
     * @return a list of roles names removes from the user
     * @throws IOException
     * @throws SQLException
     */
    public List<String> removedRolesUser(User user) throws IOException, SQLException {
        Graph graph = new Graph();
        String displayName = user.displayName;
        String id = user.id;
        List<String> removedRoles = new ArrayList<String>();
        List<DirectoryRole> adRoles = graph.GetAllRoleFrom(id);
        List<String> dbRolesNames = rolesOfUser(displayName);
        for (int i = 0; i < dbRolesNames.size(); i++) {
            Boolean state = true;
            String currentRole = dbRolesNames.get(i);
            for (int j = 0; j < adRoles.size(); j++) {
                if (currentRole.equals(adRoles.get(j).displayName)) {
                    state = false;
                }
            }
            if (state == true) {
                removedRoles.add(currentRole);
            }
        }
        return removedRoles;
    }

    /**
     * get all removed roles for all users
     * 
     * @return a map containing a list of deleted roles for each users
     * @throws SQLException
     * @throws IOException
     */
    public Map<String, List<String>> removedRolesFromUsers() throws SQLException, IOException {
        Graph graph = new Graph();
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<User> users = graph.getAdUserList();
        for (int i = 0; i < users.size(); i++) {
            map.put(users.get(i).displayName, removedRolesUser(users.get(i)));

        }
        return map;
    }

    /**
     * return the groups of a user
     * 
     * @param name user name
     * @return list of groups names
     * @throws SQLException
     */
    public List<String> GroupsOfUser(String name) throws SQLException {
        ResultSet set;
        List<String> indb = new ArrayList<String>();
        PreparedStatement readStatement = connection.prepareStatement(
                "SELECT adgroup.displayName FROM adgroup JOIN usergroup ON adgroup.adId=usergroup.groupid JOIN adusers ON usergroup.userId = adusers.id where adgroup.scopeName =? AND adusers.displayName=?  ");
        readStatement.setString(1, currentAD);
        readStatement.setString(2, name);
        set = readStatement.executeQuery();
        while (set.next()) {
            indb.add(set.getString("displayName"));
        }
        return indb;
    }

    /**
     * get the groups the user has been added to
     * 
     * @param user graph user
     * @return a list of group names
     * @throws SQLException
     * @throws IOException
     */
    public List<String> addedgroupToUser(User user) throws SQLException, IOException {
        Graph graph = new Graph();
        String displayName = user.displayName;
        String id = user.id;
        List<String> addedGroups = new ArrayList<String>();
        List<Group> adGroups = graph.groupsOf(id);
        List<String> dbGroupsNames = GroupsOfUser(displayName);
        for (int i = 0; i < adGroups.size(); i++) {
            Boolean state = true;
            String currentRole = adGroups.get(i).displayName;
            for (int j = 0; j < dbGroupsNames.size(); j++) {
                if (currentRole.equals(dbGroupsNames.get(j))) {
                    state = false;
                }
            }
            if (state == true) {
                addedGroups.add(currentRole);
            }
        }
        return addedGroups;

    }

    /**
     * get added goups for each user of the current AD
     * 
     * @return a map having a list of added groups for each users
     * @throws SQLException
     * @throws IOException
     */
    public Map<String, List<String>> addedGroupsToUsers() throws SQLException, IOException {
        Graph graph = new Graph();
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<User> users = graph.getAdUserList();
        for (int i = 0; i < users.size(); i++) {
            map.put(users.get(i).displayName, addedgroupToUser(users.get(i)));

        }
        return map;
    }

    /**
     * get removed groups from a user
     * 
     * @param user graph user
     * @return a list of group name removed from user
     * @throws IOException
     * @throws SQLException
     */
    public List<String> removedGoupsUser(User user) throws IOException, SQLException {
        Graph graph = new Graph();
        String displayName = user.displayName;
        String id = user.id;
        List<String> removedGroups = new ArrayList<String>();
        List<Group> adGroups = graph.groupsOf(id);
        List<String> dbGroupNames = GroupsOfUser(displayName);
        for (int i = 0; i < dbGroupNames.size(); i++) {
            Boolean state = true;
            String currentRole = dbGroupNames.get(i);
            for (int j = 0; j < adGroups.size(); j++) {
                if (currentRole.equals(adGroups.get(j).displayName)) {
                    state = false;
                }
            }
            if (state == true) {
                removedGroups.add(currentRole);
            }
        }
        return removedGroups;
    }

    /**
     * get all removed groups for all users
     * 
     * @return a map containing all removed groups from each users
     * @throws SQLException
     * @throws IOException
     */
    public Map<String, List<String>> removedGroupsFromUsers() throws SQLException, IOException {
        Graph graph = new Graph();
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<User> users = graph.getAdUserList();
        for (int i = 0; i < users.size(); i++) {
            map.put(users.get(i).displayName, removedGoupsUser(users.get(i)));

        }
        return map;
    }
}
