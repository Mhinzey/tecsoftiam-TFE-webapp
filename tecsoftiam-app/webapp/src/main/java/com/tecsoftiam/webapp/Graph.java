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
import com.microsoft.graph.models.DirectoryRole;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.Event;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.RoleAssignment;
import com.microsoft.graph.models.User;
import com.microsoft.graph.models.AttendeeType;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.SubscriptionCollectionPage;
import com.microsoft.graph.requests.UserCollectionPage;
import com.microsoft.graph.requests.DirectoryAuditCollectionPage;
import com.microsoft.graph.requests.DirectoryObjectCollectionWithReferencesPage;
import com.microsoft.graph.requests.DirectoryRoleCollectionPage;
import com.microsoft.graph.requests.EventCollectionPage;
import com.microsoft.graph.requests.EventCollectionRequestBuilder;
import java.util.Arrays;

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

    //initalize oAuth properties
    public Graph() throws IOException {
        oAuthProperties.load(WebappApplication.class.getClassLoader().getResourceAsStream("oAuth.properties"));
        this.scopes = Arrays.asList(oAuthProperties.getProperty("app.scopes").split(","));
        this.clientId = oAuthProperties.getProperty("app.id");
        this.clientSecret = oAuthProperties.getProperty("app.secret");
        this.tenant = oAuthProperties.getProperty("app.tenant");
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
        return graphClient.users(id).buildRequest().select("displayName").get();
    }

    //return a list of Users using graph model
    public List<com.microsoft.graph.models.User> getAdUserList() {

        final UserCollectionPage usersList = graphClient.users().buildRequest().get();
        List<com.microsoft.graph.models.User> usrList = usersList.getCurrentPage();
        // testing purposes
        for (int i = 0; i < usrList.size(); i++) {
            System.out.println("User : " + usrList.get(i).displayName);
        }
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

    public List<DirectoryObject> getUserRoles(String id){
        DirectoryObjectCollectionWithReferencesPage members = graphClient.directoryRoles("roleTemplateId="+id).members()
            .buildRequest()
            .get();

        List<DirectoryObject> users = members.getCurrentPage();
        while(members.getNextPage() != null){
            members = members.getNextPage() .buildRequest().get();
            users.addAll(members.getCurrentPage());
        }
        System.out.println(users.size());
        User usr;
        for(int i=0; i<users.size();i++){
            usr= (User)users.get(i);
            System.out.println("Role:" + usr.displayName);
        }
        return users;
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
        return type + time + by;
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
}