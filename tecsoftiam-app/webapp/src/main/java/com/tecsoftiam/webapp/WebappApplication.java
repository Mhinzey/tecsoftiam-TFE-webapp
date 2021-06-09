package com.tecsoftiam.webapp;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
import com.microsoft.graph.models.Request;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.GroupCollectionPage;
import com.microsoft.graph.requests.UserCollectionPage;
import com.microsoft.graph.serializer.ISerializer;

import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import java.io.IOException;
import java.util.Properties;
@SpringBootApplication
public class WebappApplication  extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(WebappApplication.class);
	}
	
	public static void main(String[] args) throws IOException, SQLException {
	//load oAuth properties for graph api
		Properties properties = new Properties();
		final Properties oAuthProperties = new Properties();	
		oAuthProperties.load(WebappApplication.class.getClassLoader().getResourceAsStream("oAuth.properties"));	
		final List<String> scopes = Arrays
			.asList(oAuthProperties.getProperty("app.scopes").split(","));
		final String clientId = oAuthProperties.getProperty("app.id");
		final String clientSecret = oAuthProperties.getProperty("app.secret");
		final String tenant = oAuthProperties.getProperty("app.tenant");
		
		final ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
        .clientId(clientId)
        .clientSecret(clientSecret)
        .tenantId(tenant)
        .build();

final TokenCredentialAuthProvider tokenCredentialAuthProvider = new TokenCredentialAuthProvider(scopes, clientSecretCredential);

/*final GraphServiceClient graphClient =
  GraphServiceClient
    .builder()
    .authenticationProvider(tokenCredentialAuthProvider)
    .buildClient();*/
	
final GraphServiceClient graphClient = 
	GraphServiceClient
	  .builder()
	  .authenticationProvider(tokenCredentialAuthProvider)
	  .buildClient();
	  LinkedList<Option> requestOptions = new LinkedList<Option>();
	  requestOptions.add(new HeaderOption("ConsistencyLevel", "eventual"));
final	UserCollectionPage usersList = graphClient.users()
	.buildRequest(requestOptions )
	.select("displayName")
	.get();
	
final com.microsoft.graph.models.User usr = graphClient.users("{6345e4c6-c091-4c40-811c-34a6b952451b}")
	.buildRequest()
	.select("displayName")
	.get();
//final com.microsoft.graph.models.User me = graphClient.me().buildRequest().get();
List<com.microsoft.graph.models.User> usrList= usersList.getCurrentPage();
for(int i = 0; i < usrList.size(); i++) {
	System.out.println(usrList.get(i).displayName);
}
//System.out.println("users: " + usrList);

		
		

		    
	

		//load app properties	
		properties.load(WebappApplication.class.getClassLoader().getResourceAsStream("application.properties")); 
		 //connect to db
        Connection connection = DriverManager.getConnection(properties.getProperty("url"), properties);

		dbConnect db= new dbConnect();
		User user = new User((long) 1,"oli","test");
		user = db.readData(connection);

		SpringApplication.run(WebappApplication.class, args);
		String url = "https://api.github.com/users/mhinzey/repos";
		
		
	}

}
