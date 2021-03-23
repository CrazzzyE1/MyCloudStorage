package serverApp;

import java.sql.*;

public class DbController {

    private static final String url = "jdbc:mysql://localhost:3306/clouddb";
    private static final String user = "root";
    private static final String password = "Viking07";
    private static Connection connection;
    private static Statement statement;
    private static ResultSet resultSet;
    private String host;
    private int port;

    private String sql = "";

    public DbController(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean auth(String login, String password) {
        sql = "SELECT login, password FROM clouddb.users " +
                "WHERE login='" + login + "' AND password='" + password + "';";
        ResultSet rs = sendQuery(sql);
        try {
            if (rs.next()) return true;
            return false;
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
            if (rs.next()) return true;
            return false;
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
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
