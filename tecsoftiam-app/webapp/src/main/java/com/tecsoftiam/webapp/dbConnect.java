package com.tecsoftiam.webapp;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class dbConnect {
        
        

    private static void insertData(User user, Connection connection) throws SQLException {
        System.out.println("Insert data");
        PreparedStatement insertStatement = connection
                .prepareStatement("INSERT INTO users (id, description, details, done) VALUES (?, ?, ?, ?);");
    
        insertStatement.setLong(1, user.getId());
        insertStatement.setString(2, user.getUsername());
        insertStatement.setString(3, user.getEmail());
        insertStatement.setBoolean(4, user.isDone());
        insertStatement.executeUpdate();
    }
    User readData(Connection connection) throws SQLException {
        System.out.println("Read data");
        PreparedStatement readStatement = connection.prepareStatement("SELECT * FROM users;");
        ResultSet resultSet = readStatement.executeQuery();
        if (!resultSet.next()) {
            System.out.println("There is no data in the database!");
            return null;
        }
        User user = new User();
        user.setId(resultSet.getLong("id_users"));
        user.setUsername(resultSet.getString("Username"));
        user.setEmail(resultSet.getString("Email"));
        //user.setDone(resultSet.getBoolean("done"));
        System.out.println("Data read from the database: " + user.toString());
        return user;
    }
}
