package com.tecsoftiam.webapp;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import okhttp3.Request;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DeviceCodeCredential;
import com.azure.identity.DeviceCodeCredentialBuilder;

import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.logger.DefaultLogger;
import com.microsoft.graph.logger.LoggerLevel;
import com.microsoft.graph.models.Attendee;
import com.microsoft.graph.models.DateTimeTimeZone;
import com.microsoft.graph.models.DirectoryAudit;
import com.microsoft.graph.models.DirectoryObject;
import com.microsoft.graph.models.DirectoryObjectGetMemberGroupsParameterSet;
import com.microsoft.graph.models.DirectoryRole;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.Event;
import com.microsoft.graph.models.Group;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.PasswordProfile;
import com.microsoft.graph.models.RoleAssignment;
import com.microsoft.graph.models.TargetResource;
import com.microsoft.graph.models.User;
import com.microsoft.graph.models.AttendeeType;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.GroupCollectionPage;
import com.microsoft.graph.requests.SubscriptionCollectionPage;
import com.microsoft.graph.requests.UserCollectionPage;
import com.tecsoftiam.WebappApplication;

import org.h2.engine.SysProperties;

import javassist.expr.NewArray;

import com.microsoft.graph.requests.DirectoryAuditCollectionPage;
import com.microsoft.graph.requests.DirectoryObjectCollectionWithReferencesPage;
import com.microsoft.graph.requests.DirectoryObjectGetMemberGroupsCollectionPage;
import com.microsoft.graph.requests.DirectoryRoleCollectionPage;
import com.microsoft.graph.requests.EventCollectionPage;
import com.microsoft.graph.requests.EventCollectionRequestBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Class using MS graph api to get AD objects
 * 
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

    //initalize oAuth properties
    public Graph() throws IOException {
        scope= new Scope();
        oAuthProperties.load(WebappApplication.class.getClassLoader().getResourceAsStream("oAuth.properties"));
        this.scopes = Arrays.asList(oAuthProperties.getProperty("app.scopes").split(","));
        this.clientId = oAuthProperties.getProperty("app.id");
        this.clientSecret = scope.getPassword();
        this.tenant = scope.getTenantId();
   
        initializeGraphAuth();
    }
    //initalisiz Authenthification and token provider
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
    //return user with specific ID
    public com.microsoft.graph.models.User getAdUser(String id) {
        
        return graphClient.users(id).buildRequest().get();

    }
 
    public com.microsoft.graph.models.User getAdUserByDP(String displayName) {
        List<User> list= getAdUserList();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).displayName.equals(displayName)){
                return list.get(i);
            } 
        }

        return null;

    }

    //return a list of Users using graph model
    public List<com.microsoft.graph.models.User> getAdUserList() {

        final UserCollectionPage usersList = graphClient.users().buildRequest().get();
        List<com.microsoft.graph.models.User> usrList = usersList.getCurrentPage();
     
        return usrList;
    }

    //should return a list of subscriptions (empty atm ?)
    public List<com.microsoft.graph.models.Subscription> getSubscriptionsList() {

        final SubscriptionCollectionPage subscriptions = graphClient.subscriptions().buildRequest().get();
        List<com.microsoft.graph.models.Subscription> subsList = subscriptions.getCurrentPage();
        // testing purpose
        for (int i = 0; i < subsList.size(); i++) {
            System.out.println(("Subscriptions:" + subsList.get(i)));
        }
        return subsList;
    }

    //get all roles active in AD
    public List<DirectoryRole> getDirectoryRoles(){
        DirectoryRoleCollectionPage directoryRoles = graphClient.directoryRoles()
            .buildRequest()
            .get();
        List<DirectoryRole> roleList = directoryRoles.getCurrentPage();
        while(directoryRoles.getNextPage() != null){
            directoryRoles = directoryRoles.getNextPage().buildRequest().get();
            roleList.addAll(directoryRoles.getCurrentPage());
        }
        return roleList;
    }

    public DirectoryRole getRoleDetails(String templateId){
        return graphClient.directoryRoles("roleTemplateId="+templateId)
        .buildRequest()
        .get();

    }

    //get all users having a role with templateId 
    public List<DirectoryObject> getUserRoles(String id){
        DirectoryObjectCollectionWithReferencesPage members = graphClient.directoryRoles("roleTemplateId="+id).members()
            .buildRequest()
            .get();

        List<DirectoryObject> users = members.getCurrentPage();
        while(members.getNextPage() != null){
            members = members.getNextPage() .buildRequest().get();
            users.addAll(members.getCurrentPage());
        }
        User usr;
        for(int i=0; i<users.size();i++){
            usr= (User)users.get(i);
          
        }
        return users;
    }
    //return a list of all roles from a user/group
    public List<DirectoryRole> GetAllRoleFrom(String id){
        List<DirectoryRole> allRoles= getDirectoryRoles();
        List<DirectoryRole> hasRole=new ArrayList<DirectoryRole>();
        List<DirectoryObject> usersWRole=new ArrayList<DirectoryObject>();
        User currentUser;
        DirectoryRole role;
        User usr= getAdUser(id);
        System.out.println(usr.displayName);
        for(int i=0; i<allRoles.size();i++){
            role= allRoles.get(i);
            usersWRole= getUserRoles(role.roleTemplateId);
            for(int j=0; j<usersWRole.size();j++ ){
                currentUser= (User)usersWRole.get(j);
                if(currentUser.displayName.equals(usr.displayName)){
                    
                    hasRole.add(role);
                }
            }
        }
        return hasRole;
    }
    //return a list of roles the selected used is NOT a part of
    public Set<DirectoryRole> NotHaveRoleList(String id){
     
        List<DirectoryRole> hasRole,allRole= new ArrayList<DirectoryRole>(); 
        hasRole=GetAllRoleFrom(id);
        allRole= getDirectoryRoles();
        List<DirectoryRole> notHave= new ArrayList<DirectoryRole>();

        DirectoryRole roleA, roleB;
        boolean state;
        Set<DirectoryRole> set= new HashSet<DirectoryRole>();
        if(hasRole.size()==0) notHave=allRole;
        else{
        for(int i=0; i<allRole.size();i++){
            roleA= allRole.get(i);
            state=true;
            for(int j=0; j<hasRole.size();j++ ){
            roleB=hasRole.get(j);
           
                if(roleA.displayName.equals(roleB.displayName)){                
                   
                   state=false;
                }
                
            }
            if(state==true){
                    
                notHave.add(roleA);
            }
        }
    }
    for (DirectoryRole r : notHave)
        set.add(r);
        
    
        return set;
    }

   

    public List<DirectoryAudit> getDirectoryAudits (){
        requestOptions.add(new QueryOption("$filter", "activityDisplayName eq 'Add user'"));
        DirectoryAuditCollectionPage directoryAudits = graphClient.auditLogs().directoryAudits()
        .buildRequest(requestOptions)
        .get();
        List<DirectoryAudit> audits = directoryAudits.getCurrentPage();
        while(directoryAudits.getNextPage() != null){
            directoryAudits = directoryAudits.getNextPage().buildRequest().get();
            audits.addAll(directoryAudits.getCurrentPage());
        }
        return audits;
    }
    //test purpose; print logs info
    public String AuditBy(DirectoryAudit audit){
        String type=audit.activityDisplayName;
        String time= audit.activityDateTime.toString();
        String by= audit.initiatedBy.user.id;
        String cible= audit.toString();
        for (int i=0; i<audit.targetResources.size();i++) {
            System.out.println(audit.targetResources.get(i).id);
        }
        return type + time + by + cible;
    }
    // get all audit logs for user add
    public void AllAudit(){
        List<DirectoryAudit> lst;
        lst= getDirectoryAudits();
        DirectoryAudit audit;
        for(int i=0; i<lst.size();i++){
            audit= (DirectoryAudit)lst.get(i);
            System.out.println(AuditBy(audit));
        }
    }
    //create user in the active directory
    public void CreateUser(String display, String mailNick, String mail, String name, String password){
        
        User user = new User();
        user.accountEnabled = true;
        user.displayName = display;
        user.userPrincipalName=mail;
        user.mail = mail;
        user.mailNickname=mailNick;
        PasswordProfile passwordProfile = new PasswordProfile();
        passwordProfile.forceChangePasswordNextSignIn = true;
        passwordProfile.password = password;
        user.passwordProfile = passwordProfile;

        graphClient.users()
            .buildRequest()
            .post(user);

            }
    //delete user in AD
    public void deleteUser(String id){
        deleteAllRolesFrom(id);
        graphClient.users(id)
	.buildRequest()
	.delete();
    }

            //change accountEnabled state of a user
    public void changeActivate(User user, Boolean state){
        if(state == true)
        user.accountEnabled=false;
        else user.accountEnabled=true;
    }
    //grant a ad role to a user / group
    public void grantRole(String templateId, String toId){
        DirectoryObject directoryObject = new DirectoryObject();
        directoryObject.id = toId;

        graphClient.directoryRoles("roleTemplateId="+templateId).members().references()
            .buildRequest()
            .post(directoryObject);
    }
    //delete role from user/group
    public void deleteRoleFrom(String template, String idOf){
        graphClient.directoryRoles("roleTemplateId="+template).members(idOf).reference()
	.buildRequest()
	.delete();
    

    }
    //delete all roles from a user (used for user delete)
    public void deleteAllRolesFrom(String id){
        List<DirectoryRole> list= GetAllRoleFrom(id);
        DirectoryRole role;
        for(int i=0 ; i<list.size() ;i++){
            role=list.get(i);
            deleteRoleFrom(role.roleTemplateId, id);
        }
    }
    //get a list of all groups
    public List<Group> getGroupsList(){
        GroupCollectionPage groups = graphClient.groups()
            .buildRequest()
            .get();
        List<Group> lst= groups.getCurrentPage();
          while(groups.getNextPage() != null){
            groups = groups.getNextPage() .buildRequest().get();
            lst.addAll(groups.getCurrentPage());
          }
        return lst;
    }
    public List<DirectoryObject> usersInGroup(String id){
        DirectoryObjectCollectionWithReferencesPage members = graphClient.groups(id).members()
            .buildRequest()
            .get();
        List<DirectoryObject> users = members.getCurrentPage();

        while(members.getNextPage() != null){
            members = members.getNextPage() .buildRequest().get();
            users.addAll(members.getCurrentPage());
        }
      
        return users;

    }
    //return group details from ID
    public Group groupDetail(String id){
                
        return graphClient.groups(id)
        .buildRequest()
        .get();

    }

    //get a list of users from a group 
    public List<DirectoryObject> membersOf(String id){
            DirectoryObjectCollectionWithReferencesPage members = graphClient.groups(id).members()
        .buildRequest()
        .get();
        List<DirectoryObject> users = members.getCurrentPage();
        while(members.getNextPage() != null){
            members = members.getNextPage() .buildRequest().get();
            users.addAll(members.getCurrentPage());
        }
        return users;

    }
    //add user to a group
    public void addToGroup(String userId, String groupid){

        DirectoryObject directoryObject = new DirectoryObject();
        directoryObject.id = userId;

        graphClient.groups(groupid).members().references()
            .buildRequest()
	.post(directoryObject);

    }

    //delete user from a group 
    public void deleteFromGroup(String userId, String groupId){
        graphClient.groups(groupId).members(userId).reference()
	.buildRequest()
	.delete();
    }

    //gte group list of a user/role
    public List<Group> groupsOf(String id){
        Boolean securityEnabledOnly = true;
        List<Group> groupList=new ArrayList<Group>();
        DirectoryObjectGetMemberGroupsCollectionPage groups= graphClient.directoryObjects(id)
            .getMemberGroups(DirectoryObjectGetMemberGroupsParameterSet
                .newBuilder()
                .withSecurityEnabledOnly(securityEnabledOnly)
                .build())
            .buildRequest()
            .post();
            List<String> grp = groups.getCurrentPage();
            while(groups.getNextPage() != null){
                groups = groups.getNextPage().buildRequest().post();
                grp.addAll(groups.getCurrentPage());
            }
           for(int i=0; i<grp.size();i++){
            Group group = graphClient.groups(grp.get(i))
            .buildRequest()
            .get();
            groupList.add(group);
        
           }
        return groupList;
    }

    //return a list of groups a member is not a part of
    public Set<Group> NotHaveGroupList(String id){
     
        List<Group> hasGroup,allGroups= new ArrayList<Group>(); 
        hasGroup= groupsOf(id);
        allGroups= getGroupsList();
        List<Group> notHave= new ArrayList<Group>();

        Group roleA, roleB;
        boolean state;
        Set<Group> set= new HashSet<Group>();
        if(hasGroup.size()==0) notHave=allGroups;
       
        else{
        for(int i=0; i<allGroups.size();i++){
            roleA= allGroups.get(i);
            state=true;
            for(int j=0; j<hasGroup.size();j++ ){
                state=true;
                
            roleB=hasGroup.get(j);
                if((roleA.displayName.equals(roleB.displayName))){                
               
                   state=false;
                }
                
            }
            if(state==true){
                    notHave.add(roleA);
                }
        }
    }
    for (Group r : notHave)
        set.add(r);
        
    
        return set;
    }
    //restore object deleted from ad
    public void restoreObject(String id){
        graphClient.directory().deletedItems(id)
        .restore()
        .buildRequest()
        .post();
    }
   
}