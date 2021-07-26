package com.tecsoftiam;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.azure.identity.AuthorizationCodeCredential;
import com.azure.identity.AuthorizationCodeCredentialBuilder;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.google.gson.JsonObject;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.DirectoryAudit;
import com.microsoft.graph.models.DirectoryRole;
import com.microsoft.graph.models.Request;
import com.microsoft.graph.models.Subscription;
import com.microsoft.graph.models.User;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.GroupCollectionPage;
import com.microsoft.graph.requests.SubscriptionCollectionPage;
import com.microsoft.graph.requests.SubscriptionCollectionResponse;
import com.microsoft.graph.requests.UserCollectionPage;
import com.microsoft.graph.serializer.ISerializer;
import com.tecsoftiam.webapp.AppUser;
import com.tecsoftiam.webapp.Graph;
import com.tecsoftiam.webapp.dbConnect;

import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.bind.annotation.InitBinder;

import java.io.IOException;
import java.util.Properties;

@SpringBootApplication
public class WebappApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(WebappApplication.class);
	}

	public static void main(String[] args) throws IOException, SQLException, ParseException {
		final Properties properties = new Properties();
		Graph graphtest = new Graph();
		graphtest.initializeGraphAuth();
		
		// graphtest.getSubscriptionsList();

	
		//test to read into the db
		dbConnect db = new dbConnect();
		//AppUser user = new AppUser((long) 1, "oli", "test");
		//db.insertCreatedDate();
		//graphtest.AllAudit();
		//List<User> lst= graphtest.getAdUserList();
		//db.InsertMultipleUsers(lst);
		//List<DirectoryAudit> liste= graphtest.getDirectoryAudits();
		//db.insertAllLogs(liste);
		//List<DirectoryRole> lst =graphtest.getDirectoryRoles();
		//db.insertAllDirectoryRoles(lst);
		//db.matchRoles();
		graphtest.grantRole("88d8e3e3-8f55-4a1e-953a-9b9898b8876b", "ccfdd40e-23be-450e-bd6c-01aa7ce689a8");
		SpringApplication.run(WebappApplication.class, args);

	}

}
