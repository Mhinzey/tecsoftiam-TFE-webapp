package com.tecsoftiam.webapp;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebappApplication  {

	
	public static void main(String[] args) throws IOException, SQLException {
		Properties properties = new Properties();
        properties.load(WebappApplication.class.getClassLoader().getResourceAsStream("application.properties"));      
		
        Connection connection = DriverManager.getConnection(properties.getProperty("url"), properties);

		dbConnect db= new dbConnect();
		User user = new User((long) 1,"oli","test");
		user = db.readData(connection);

		SpringApplication.run(WebappApplication.class, args);
		String url = "https://api.github.com/users/mhinzey/repos";
		
		
	}

}
