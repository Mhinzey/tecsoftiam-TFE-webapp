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

public class UserService {
    Connection connection;
    final Properties properties = new Properties();
    public UserService() throws IOException, SQLException{
        properties.load(WebappApplication.class.getClassLoader().getResourceAsStream("application.properties"));
        connection = DriverManager.getConnection(properties.getProperty("url"), properties);
    }
    public AppUser login(String email, String password)
    {AppUser dbUser = new AppUser();
    try
    {

        String query = "SELECT * FROM users WHERE email=? AND password=?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, email);
        stmt.setString(2, password);

        ResultSet rs = stmt.executeQuery();
        rs.next();

        dbUser.setId(rs.getInt("id"));
        dbUser.setEmail(rs.getString("email"));
        dbUser.setUsername(rs.getString("username"));
        dbUser.setPassword(rs.getString("password"));
        

        if(email.equals(dbUser.getEmail()) && 
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
