package serverApp.Controllers;

import java.sql.*;

public class DbController {

    private final String url;
    private final String user;
    private final String password;
    private Statement statement;
    private ResultSet resultSet;
    private String sql = "";

    public DbController(String host, int port, String user, String password) {
        this.user = user;
        this.password = password;
        this.url = "jdbc:mysql://" + host + ":" + port + "/clouddb";
    }

    // Авторизация
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


    // Регистрация
    public boolean reg(String login, String password, String nick) {
        if (checkLogin(login)) return false;
        sql = "INSERT INTO clouddb.users (login, password, nickname, folderpath, space) " +
                "VALUES ('" + login + "', " + "'" + password + "', " + "'" + nick + "', " + "'" + login + "', '15');";
        return sendExecute(sql);
    }

    // Проверка на существование логина
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

    // Запрос в БД
    private ResultSet sendQuery(String sql) {
        openConnection();
        try {
            resultSet = statement.executeQuery(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return resultSet;
    }

    // Запрос в БД
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

    // Получение доступного места на диске для аккаунта
    public int getSpace(String login) {
        sql = "SELECT space FROM clouddb.users " +
                "WHERE login='" + login + "';";
        ResultSet rs = sendQuery(sql);
        int space = 0;
        try {
            if (rs.next())
                space = rs.getInt("space");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return space;
    }

    // Соединение с БД
    private void openConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }

    // Смена пароля
    public boolean changePass(String login, String oldPassword, String newPassword) {
        if(!auth(login,oldPassword)) return false;
        sql = "UPDATE clouddb.users SET password = '"
                + newPassword + "' WHERE (login = '"
                + login + "' AND password = '" + oldPassword + "');";
        return sendExecute(sql);
    }

    // Удаление аккаунта
    public boolean removeAccount(String login, String pass) {
        if(!auth(login, pass)) return false;
        sql = "UPDATE clouddb.users SET login = '" + "removed_".concat(login) + "' WHERE (login = '" + login + "' AND password = '" + pass + "');";
        return sendExecute(sql);
    }
}
