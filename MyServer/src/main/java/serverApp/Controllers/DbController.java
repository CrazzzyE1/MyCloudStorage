package serverApp.Controllers;

import java.sql.*;

public class DbController {

    //    private final String url = "jdbc:mysql://localhost:3306/clouddb";
    private final String url;
    private final String user;
    private final String password;
//    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private String sql = "";

    public DbController(String host, int port, String user, String password) {
        this.user = user;
        this.password = password;
        this.url = "jdbc:mysql://" + host + ":" + port + "/clouddb";
    }

    public boolean auth(String login, String password) {
        sql = "SELECT login, password FROM clouddb.users " +
                "WHERE login='" + login + "' AND password='" + password + "';";
        ResultSet rs = sendQuery(sql);
        try {
            return rs.next();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return true;
    }

    public boolean reg(String login, String password, String nick) {
        if (checkLogin(login)) return false;
        sql = "INSERT INTO clouddb.users (login, password, nickname, folderpath) " +
                "VALUES ('" + login + "', " + "'" + password + "', " + "'" + nick + "', " + "'" + login + "');";
        return sendExecute(sql);
    }

    public boolean checkLogin(String login) {
        sql = "SELECT login FROM clouddb.users " +
                "WHERE login='" + login + "';";
        ResultSet rs = sendQuery(sql);
        try {
            return rs.next();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return true;
    }

    private ResultSet sendQuery(String sql) {
        openConnection();
        try {
            resultSet = statement.executeQuery(sql);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return resultSet;
    }

    private boolean sendExecute(String sql) {
        openConnection();
        boolean flag = false;
        try {
            statement.execute(sql);
            flag = true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return flag;
    }


    private void openConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }
}
