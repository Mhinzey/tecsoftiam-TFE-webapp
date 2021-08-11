package com.tecsoftiam.webapp;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import com.microsoft.graph.models.User;
import com.tecsoftiam.WebappApplication;
/**
 * User service class used for login
 */
public class UserService {
    Connection connection;
    final Properties properties = new Properties();


    //constructor, initialize database link
    public UserService() throws IOException, SQLException{
        properties.load(WebappApplication.class.getClassLoader().getResourceAsStream("application.properties"));
        connection = DriverManager.getConnection(properties.getProperty("url"), properties);
    }
    //login service
    public AppUser login(String username, String password)   {
        AppUser dbUser = new AppUser();
        try
       {

        String query = "SELECT * FROM users WHERE username=? AND password=?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, username);
        stmt.setString(2, password);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        dbUser.setId(rs.getInt("id"));
        dbUser.setEmail(rs.getString("email"));
        dbUser.setUsername(rs.getString("username"));
        dbUser.setPassword(rs.getString("password"));
        if(username.equals(dbUser.getUsername()) && 
                password.equals(dbUser.getPassword()))
        {
            System.out.println("Successfully Logged-In");
        }
        else
        {
            System.out.println("Failed To Log-In");
            dbUser = null;
        }
    }
    catch(Exception e)
    {
        e.printStackTrace();
    }
    return dbUser;
}
}
