package com.tecsoftiam.webapp;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import okhttp3.Request;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.logger.DefaultLogger;
import com.microsoft.graph.logger.LoggerLevel;
import com.microsoft.graph.models.DirectoryAudit;
import com.microsoft.graph.models.DirectoryObject;
import com.microsoft.graph.models.DirectoryObjectGetMemberGroupsParameterSet;
import com.microsoft.graph.models.DirectoryRole;
import com.microsoft.graph.models.Domain;
import com.microsoft.graph.models.Group;
import com.microsoft.graph.models.PasswordProfile;
import com.microsoft.graph.models.User;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.GroupCollectionPage;
import com.microsoft.graph.requests.SubscriptionCollectionPage;
import com.microsoft.graph.requests.UserCollectionPage;
import com.tecsoftiam.WebappApplication;
import com.microsoft.graph.requests.DirectoryAuditCollectionPage;
import com.microsoft.graph.requests.DirectoryObjectCollectionWithReferencesPage;
import com.microsoft.graph.requests.DirectoryObjectGetMemberGroupsCollectionPage;
import com.microsoft.graph.requests.DirectoryRoleCollectionPage;
import com.microsoft.graph.requests.DomainCollectionPage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Class using MS graph api to get AD objects and data 
 * Author: Deryck Olivier
 */
public class Graph {

    private static GraphServiceClient<Request> graphClient = null;
    private static TokenCredentialAuthProvider authProvider = null;
    private Properties oAuthProperties = new Properties();
    private final List<String> scopes;
    private final String clientId;
    private final String clientSecret;
    private final String tenant;
    private LinkedList<Option> requestOptions = new LinkedList<Option>();
    private GraphServiceClient graphclient;
    private Scope scope;
    

    /**
     * Constructor, load properties from config files
     * 
     * @throws IOException
     */
    public Graph() throws IOException {
        scope = new Scope();
        oAuthProperties.load(WebappApplication.class.getClassLoader().getResourceAsStream("oAuth.properties"));
        this.scopes = Arrays.asList(oAuthProperties.getProperty("app.scopes").split(","));
        this.clientId = scope.getAppId();
        this.clientSecret = scope.getPassword();
        this.tenant = scope.getTenantId();

        initializeGraphAuth();
    }

    /**
     * Initialise authentication and token request
     */
    public void initializeGraphAuth() {

        // Create the auth provider
        final ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder().clientId(clientId)
                .clientSecret(clientSecret).tenantId(tenant).build();

        final TokenCredentialAuthProvider tokenCredentialAuthProvider = new TokenCredentialAuthProvider(scopes,
                clientSecretCredential);

        // Create default logger to only log errors
        DefaultLogger logger = new DefaultLogger();
        logger.setLoggingLevel(LoggerLevel.ERROR);

        this.graphClient = GraphServiceClient.builder().authenticationProvider(tokenCredentialAuthProvider)
                .buildClient();
    }

    /**
     * Get a user with a specific id
     * 
     * @param id user id
     * @return a graph User
     */
    public com.microsoft.graph.models.User getAdUser(String id) {

        return graphClient.users(id).buildRequest().get();

    }

    /**
     * Get a user from displayname
     * 
     * @param displayName user displayname
     * @return Graph User
     */
    public com.microsoft.graph.models.User getAdUserByDP(String displayName) {
        List<User> list = getAdUserList();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).displayName.equals(displayName)) {
                return list.get(i);
            }
        }

        return null;

    }

    /**
     * Get a list of all users
     * 
     * @return a list of Graph user in the Active directory
     */
    public List<com.microsoft.graph.models.User> getAdUserList() {

        final UserCollectionPage usersList = graphClient.users().buildRequest().get();
        List<com.microsoft.graph.models.User> usrList = usersList.getCurrentPage();

        return usrList;
    }

    /**
     * Should return a list of subscription, not working currently
     * 
     * @return a list of subscription
     */
    public List<com.microsoft.graph.models.Subscription> getSubscriptionsList() {

        final SubscriptionCollectionPage subscriptions = graphClient.subscriptions().buildRequest().get();
        List<com.microsoft.graph.models.Subscription> subsList = subscriptions.getCurrentPage();
        // testing purpose
        for (int i = 0; i < subsList.size(); i++) {
            System.out.println(("Subscriptions:" + subsList.get(i)));
        }
        return subsList;
    }

    /**
     * get all directory roles from the active directory
     * 
     * @return list of DirectoryRole
     */
    public List<DirectoryRole> getDirectoryRoles() {
        DirectoryRoleCollectionPage directoryRoles = graphClient.directoryRoles().buildRequest().get();
        List<DirectoryRole> roleList = directoryRoles.getCurrentPage();
        while (directoryRoles.getNextPage() != null) {
            directoryRoles = directoryRoles.getNextPage().buildRequest().get();
            roleList.addAll(directoryRoles.getCurrentPage());
        }
        return roleList;
    }

    /**
     * get directory role details from a role
     * 
     * @param templateId roletemplateId
     * @return the DirectoryRole requested
     */
    public DirectoryRole getRoleDetails(String templateId) {
        return graphClient.directoryRoles("roleTemplateId=" + templateId).buildRequest().get();

    }

    /**
     * get role detail by equesting his name
     * 
     * @param name role name
     * @return DirectoryRole
     */
    public DirectoryRole getRoleByRoleName(String name) {
        return graphClient.directoryRoles("displayName=" + name).buildRequest().get();
    }

    /**
     * get all user having the role in param
     * 
     * @param id role id
     * @return a list of directoryObject (users or users+ groups)
     */
    public List<DirectoryObject> getUserRoles(String id) {
        DirectoryObjectCollectionWithReferencesPage members = graphClient.directoryRoles("roleTemplateId=" + id)
                .members().buildRequest().get();

        List<DirectoryObject> users = members.getCurrentPage();
        while (members.getNextPage() != null) {
            members = members.getNextPage().buildRequest().get();
            users.addAll(members.getCurrentPage());
        }
        User usr;
        for (int i = 0; i < users.size(); i++) {
            usr = (User) users.get(i);

        }
        return users;
    }

    /**
     * get all roles from a user
     * 
     * @param id user id
     * @return List of DirectoryRole
     */
    public List<DirectoryRole> GetAllRoleFrom(String id) {
        List<DirectoryRole> allRoles = getDirectoryRoles();
        List<DirectoryRole> hasRole = new ArrayList<DirectoryRole>();
        List<DirectoryObject> usersWRole = new ArrayList<DirectoryObject>();
        User currentUser;
        DirectoryRole role;
        User usr = getAdUser(id);

        for (int i = 0; i < allRoles.size(); i++) {
            role = allRoles.get(i);
            usersWRole = getUserRoles(role.roleTemplateId);
            for (int j = 0; j < usersWRole.size(); j++) {
                currentUser = (User) usersWRole.get(j);
                if (currentUser.displayName.equals(usr.displayName)) {

                    hasRole.add(role);
                }
            }
        }
        return hasRole;
    }

    /**
     * get all roles from a group
     * 
     * @param id group id
     * @return list of DirectoryRole
     */
    public List<DirectoryRole> GetAllRoleFromGroup(String id) {
        List<DirectoryRole> allRoles = getDirectoryRoles();
        List<DirectoryRole> hasRole = new ArrayList<DirectoryRole>();
        List<DirectoryObject> usersWRole = new ArrayList<DirectoryObject>();
        Group currentGroup;
        DirectoryRole role;
        Group usr = groupDetail(id);

        for (int i = 0; i < allRoles.size(); i++) {
            role = allRoles.get(i);
            usersWRole = getUserRoles(role.roleTemplateId);
            for (int j = 0; j < usersWRole.size(); j++) {
                currentGroup = (Group) usersWRole.get(j);
                if (currentGroup.displayName.equals(usr.displayName)) {

                    hasRole.add(role);
                }
            }
        }
        return hasRole;
    }

    /**
     * Get a list of role the object doesn't have
     * 
     * @param id object id (user or group)
     * @return list of directory Role
     */
    public Set<DirectoryRole> NotHaveRoleList(String id) {

        List<DirectoryRole> hasRole, allRole = new ArrayList<DirectoryRole>();
        hasRole = GetAllRoleFrom(id);
        allRole = getDirectoryRoles();
        List<DirectoryRole> notHave = new ArrayList<DirectoryRole>();

        DirectoryRole roleA, roleB;
        boolean state;
        Set<DirectoryRole> set = new HashSet<DirectoryRole>();
        if (hasRole.size() == 0)
            notHave = allRole;
        else {
            for (int i = 0; i < allRole.size(); i++) {
                roleA = allRole.get(i);
                state = true;
                for (int j = 0; j < hasRole.size(); j++) {
                    roleB = hasRole.get(j);

                    if (roleA.displayName.equals(roleB.displayName)) {

                        state = false;
                    }

                }
                if (state == true) {

                    notHave.add(roleA);
                }
            }
        }
        for (DirectoryRole r : notHave)
            set.add(r);

        return set;
    }

    /**
     * Get directory audits (logs) of action added user
     * 
     * @return list of Directory Audit
     */
    public List<DirectoryAudit> getDirectoryAudits() {
        requestOptions.add(new QueryOption("$filter", "activityDisplayName eq 'Add user'"));
        DirectoryAuditCollectionPage directoryAudits = graphClient.auditLogs().directoryAudits()
                .buildRequest(requestOptions).get();
        List<DirectoryAudit> audits = directoryAudits.getCurrentPage();
        while (directoryAudits.getNextPage() != null) {
            directoryAudits = directoryAudits.getNextPage().buildRequest().get();
            audits.addAll(directoryAudits.getCurrentPage());
        }
        return audits;
    }

    /**
     * test puprose, print log info
     * 
     * @param audit directoryAudit
     * @return details string
     */
    public String AuditBy(DirectoryAudit audit) {
        String type = audit.activityDisplayName;
        String time = audit.activityDateTime.toString();
        String by = audit.initiatedBy.user.id;
        String cible = audit.toString();
        for (int i = 0; i < audit.targetResources.size(); i++) {
            System.out.println(audit.targetResources.get(i).id);
        }
        return type + time + by + cible;
    }

    /**
     * Get all audits
     */
    public void AllAudit() {
        List<DirectoryAudit> lst;
        lst = getDirectoryAudits();
        DirectoryAudit audit;
        for (int i = 0; i < lst.size(); i++) {
            audit = (DirectoryAudit) lst.get(i);
            System.out.println(AuditBy(audit));
        }
    }

    /**
     * Create a user in active directory
     * 
     * @param display  displayname
     * @param mailNick mailNickName
     * @param mail     mail
     * @param name     Name
     * @param password Password
     */
    public void CreateUser(String display, String mailNick, String mail, String name, String password) {

        User user = new User();
        user.accountEnabled = true;
        user.displayName = display;
        user.userPrincipalName = mail;
        user.mail = mail;
        user.mailNickname = mailNick;
        PasswordProfile passwordProfile = new PasswordProfile();
        passwordProfile.forceChangePasswordNextSignIn = true;
        passwordProfile.password = password;
        user.passwordProfile = passwordProfile;

        graphClient.users().buildRequest().post(user);

    }

    /**
     * Delete a user from AD
     * 
     * @param id user id
     */
    public void deleteUser(String id) {
        deleteAllRolesFrom(id);
        graphClient.users(id).buildRequest().delete();
    }

    /**
     * Change accountenabled state of a user
     * 
     * @param user  user
     * @param state state- true/false
     */
    public void changeActivate(User user, Boolean state) {
        if (state == true)
            user.accountEnabled = false;
        else
            user.accountEnabled = true;
    }

    /**
     * Grant a role to a selected objetc
     * 
     * @param templateId roletemplateid
     * @param toId       object id to give role to
     */
    public void grantRole(String templateId, String toId) {
        DirectoryObject directoryObject = new DirectoryObject();
        directoryObject.id = toId;

        graphClient.directoryRoles("roleTemplateId=" + templateId).members().references().buildRequest()
                .post(directoryObject);
    }

    /**
     * delete role from object
     * 
     * @param template roletemplateId
     * @param idOf     object id
     */
    public void deleteRoleFrom(String template, String idOf) {
        graphClient.directoryRoles("roleTemplateId=" + template).members(idOf).reference().buildRequest().delete();

    }

    /**
     * Delete all role from a user (used when a user is deleted, else you can't
     * delete it)
     * 
     * @param id user id
     */
    public void deleteAllRolesFrom(String id) {
        List<DirectoryRole> list = GetAllRoleFrom(id);
        DirectoryRole role;
        for (int i = 0; i < list.size(); i++) {
            role = list.get(i);
            deleteRoleFrom(role.roleTemplateId, id);
        }
    }

    /**
     * get group list
     * 
     * @return list of Group
     */
    public List<Group> getGroupsList() {
        GroupCollectionPage groups = graphClient.groups().buildRequest().get();
        List<Group> lst = groups.getCurrentPage();
        while (groups.getNextPage() != null) {
            groups = groups.getNextPage().buildRequest().get();
            lst.addAll(groups.getCurrentPage());
        }
        return lst;
    }

    /**
     * get Users in a group
     * 
     * @param id group id
     * @return List of users
     */
    public List<DirectoryObject> usersInGroup(String id) {
        DirectoryObjectCollectionWithReferencesPage members = graphClient.groups(id).members().buildRequest().get();
        List<DirectoryObject> users = members.getCurrentPage();

        while (members.getNextPage() != null) {
            members = members.getNextPage().buildRequest().get();
            users.addAll(members.getCurrentPage());
        }

        return users;

    }

    /**
     * Get group detail from id
     * 
     * @param id group id
     * @return Group object
     */
    public Group groupDetail(String id) {

        return graphClient.groups(id).buildRequest().get();

    }

    /**
     * Get a list of user inside a group
     * 
     * @param id group id
     * @return list of users
     */
    public List<DirectoryObject> membersOf(String id) {
        DirectoryObjectCollectionWithReferencesPage members = graphClient.groups(id).members().buildRequest().get();
        List<DirectoryObject> users = members.getCurrentPage();
        while (members.getNextPage() != null) {
            members = members.getNextPage().buildRequest().get();
            users.addAll(members.getCurrentPage());
        }
        return users;

    }

    /**
     * Add a user to a group
     * 
     * @param userId  user id
     * @param groupid group id
     */
    public void addToGroup(String userId, String groupid) {

        DirectoryObject directoryObject = new DirectoryObject();
        directoryObject.id = userId;

        graphClient.groups(groupid).members().references().buildRequest().post(directoryObject);

    }

    /**
     * delete a user from a group
     * 
     * @param userId  user id
     * @param groupId group id
     */
    public void deleteFromGroup(String userId, String groupId) {
        graphClient.groups(groupId).members(userId).reference().buildRequest().delete();
    }

    /**
     * Get group list of a user
     * 
     * @param id user id
     * @return list of Group
     */
    public List<Group> groupsOf(String id) {
        Boolean securityEnabledOnly = true;
        List<Group> groupList = new ArrayList<Group>();
        DirectoryObjectGetMemberGroupsCollectionPage groups = graphClient.directoryObjects(id)
                .getMemberGroups(DirectoryObjectGetMemberGroupsParameterSet.newBuilder()
                        .withSecurityEnabledOnly(securityEnabledOnly).build())
                .buildRequest().post();
        List<String> grp = groups.getCurrentPage();
        while (groups.getNextPage() != null) {
            groups = groups.getNextPage().buildRequest().post();
            grp.addAll(groups.getCurrentPage());
        }
        for (int i = 0; i < grp.size(); i++) {
            Group group = graphClient.groups(grp.get(i)).buildRequest().get();
            groupList.add(group);

        }
        return groupList;
    }

    /**
     * Get a list of group the user iesn't a part of
     * 
     * @param id user id
     * @return Set of group
     */
    public Set<Group> NotHaveGroupList(String id) {

        List<Group> hasGroup, allGroups = new ArrayList<Group>();
        hasGroup = groupsOf(id);
        allGroups = getGroupsList();
        List<Group> notHave = new ArrayList<Group>();

        Group roleA, roleB;
        boolean state;
        Set<Group> set = new HashSet<Group>();
        if (hasGroup.size() == 0)
            notHave = allGroups;

        else {
            for (int i = 0; i < allGroups.size(); i++) {
                roleA = allGroups.get(i);
                state = true;
                for (int j = 0; j < hasGroup.size(); j++) {
                    state = true;

                    roleB = hasGroup.get(j);
                    if ((roleA.displayName.equals(roleB.displayName))) {

                        state = false;
                    }

                }
                if (state == true) {
                    notHave.add(roleA);
                }
            }
        }
        for (Group r : notHave)
            set.add(r);

        return set;
    }

    /**
     * Restore object in the active directory, available 30days max after a delete
     * 
     * @param id object id
     */
    public void restoreObject(String id) {
        graphClient.directory().deletedItems(id).restore().buildRequest().post();
    }

    /**
     * get all domains authorized in the ad
     * @return
     */
    public List<Domain> domainList(){
       
        DomainCollectionPage domains = graphClient.domains()
	.buildRequest()
	.get(); 
    List<Domain> list=domains.getCurrentPage();

    while (domains.getNextPage() != null) {
        domains = domains.getNextPage().buildRequest().get();
        list.addAll(domains.getCurrentPage());
    }
        return list;
    }
}