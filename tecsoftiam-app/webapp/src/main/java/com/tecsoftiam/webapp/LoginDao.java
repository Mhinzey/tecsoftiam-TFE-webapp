package com.tecsoftiam.webapp;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class LoginDao {
    public boolean validate(LoginBean loginBean) throws ClassNotFoundException, IOException {
        boolean status = false;

        Class.forName("com.mysql.jdbc.Driver");
        Properties properties = new Properties();
        properties.load(WebappApplication.class.getClassLoader().getResourceAsStream("application.properties"));
        try (Connection connection = DriverManager.getConnection(properties.getProperty("url"), properties);

                // Step 2:Create a statement using connection object
                PreparedStatement preparedStatement = connection
                        .prepareStatement("select * from login where username = ? and password = ? ")) {
            preparedStatement.setString(1, loginBean.getUsername());
            preparedStatement.setString(2, loginBean.getPassword());

            System.out.println(preparedStatement);
            ResultSet rs = preparedStatement.executeQuery();
            status = rs.next();

        } catch (SQLException e) {
            // process sql exception
            printSQLException(e);
        }
        return status;
    }

    private void printSQLException(SQLException ex) {
        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                e.printStackTrace(System.err);
                System.err.println("SQLState: " + ((SQLException) e).getSQLState());
                System.err.println("Error Code: " + ((SQLException) e).getErrorCode());
                System.err.println("Message: " + e.getMessage());
                Throwable t = ex.getCause();
                while (t != null) {
                    System.out.println("Cause: " + t);
                    t = t.getCause();
                }
            }
        }
    }
}
