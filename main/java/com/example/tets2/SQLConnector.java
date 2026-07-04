package com.example.tets2;

import com.example.tets2.controller.MainController;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLConnector {
    private static Connection connection;
    public static int errorCount = 0;
    private static int errorId=0;
    private static Logger logger=Logger.getLogger(Tets2Application.class.getName());

    public static int getErrorId(){
        errorId++;
        return errorId;
    }

    public static class SendData {
        private String email;
        private String id;
        private String name;
        private LocalDate expirationDate;
        private int type;

        public SendData(String email, String id, String name, LocalDate expirationDate, int type) {
            this.email = email;
            this.id = id;
            this.name = name;
            this.expirationDate = expirationDate;

            this.type = type;
        }

        public String getEmail() {
            return email;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public LocalDate getExpirationDate() {
            return expirationDate;
        }

        public int getType() {
            return type;
        }
    }

    public static void CheckDates() {
        try {
            logger.logp(Level.INFO, "SQLConnector", "CheckDates", "Начало проверки сроков");
            ArrayList<SendData> dataToSendList = new ArrayList<>();
            logger.logp(Level.INFO, "SQLConnector", "CheckDates", "Установка соединения");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3308/postdb", "root", "root");
            Statement statement = connection.createStatement();
            LocalDate reminderDate = LocalDate.now().plusDays(30);
            logger.logp(Level.INFO, "SQLConnector", "CheckDates", "Выполнение запроса");
            statement.executeQuery(String.format("select * from paper where expiration_date <= '%s-%s-%s'", reminderDate.getYear(), reminderDate.getMonthValue(), reminderDate.getDayOfMonth()));
            logger.logp(Level.INFO, "SQLConnector", "CheckDates", "Получение ответа");
            ResultSet resultSet = statement.getResultSet();
            while (resultSet.next()) {
                if (!resultSet.getBoolean("reminder")) {
                    dataToSendList.add(new SendData(resultSet.getString("client_email"), resultSet.getString("paper_id"), resultSet.getString("client_name"), reminderDate, resultSet.getInt("paper_type")));
                }
            }
            logger.logp(Level.INFO, "SQLConnector", "CheckDates", "Создание списка на отправку");
            if (dataToSendList.size() != 0) {
                StringBuilder urlUpdateReminders = new StringBuilder("update paper set reminder='1' where");
                StringBuilder urlInsertSend = new StringBuilder("insert into send_list (paper_id, client_name, email, reminder_date, type, attempt) values ");
                for (int i = 0; i < dataToSendList.size(); i++) {
                    urlInsertSend.append(String.format("('%s', '%s', '%s', '%s', '%d', '0')", dataToSendList.get(i).getId(), dataToSendList.get(i).getName(), dataToSendList.get(i).getEmail(), dataToSendList.get(i).getExpirationDate(), dataToSendList.get(i).getType()));
                    urlUpdateReminders.append(" paper_id='").append(dataToSendList.get(i).getId()).append("'");
                    if (i + 1 != dataToSendList.size()) {
                        urlUpdateReminders.append(" or");
                        urlInsertSend.append(", ");
                    }
                }
                logger.logp(Level.INFO, "SQLConnector", "CheckDates", "Выполнение запроса на обновление статуса напоминания");
                statement.executeUpdate(urlUpdateReminders.toString());
                logger.logp(Level.INFO, "SQLConnector", "CheckDates", "Пополнение списка на отправку");
                statement.executeUpdate(urlInsertSend.toString());

            }
            logger.logp(Level.INFO, "SQLConnector", "CheckDates", "Закрытие соединения");

            connection.close();
        } catch (SQLException ex) {
            errorCount++;
            logger.logp(Level.SEVERE, "SQLConnector", "CheckDates", "Ошибка при подключении к базе данных");
            Tets2Application.databaseErrors.add(new ErrorLog(getErrorId(), ex.getErrorCode(), ex.getMessage()));
        }

    }

    public static void AddToDB(String name, String email, LocalDate expiryDate, int type) throws SQLException {
        logger.logp(Level.INFO, "SQLConnector", "AddToDB", "Начало добавления нового заявления в базу данных");
        try {
            logger.logp(Level.INFO, "SQLConnector", "AddToDB", "Установка соединения");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3308/postdb", "root", "root");
            Statement statement;
            statement = connection.createStatement();
            logger.logp(Level.INFO, "SQLConnector", "AddToDB", "Выполнение запроса");
            statement.executeUpdate(String.format("insert into paper (client_name, client_email, paper_type, expiration_date, reminder) values ('%s','%s','%d','%s-%s-%s','0')", name, email, type, expiryDate.getYear(), expiryDate.getMonthValue(), expiryDate.getDayOfMonth()));
            logger.logp(Level.INFO, "SQLConnector", "AddToDB", "Закрытие соединения");
            connection.close();
        } catch (SQLException ex) {
            errorCount++;
            logger.logp(Level.SEVERE, "SQLConnector", "AddToDB", "Ошибка при подключении к базе данных");
            Tets2Application.databaseErrors.add(new ErrorLog(getErrorId(), ex.getErrorCode(), ex.getMessage()));
            throw new SQLException();
        }
    }

    public static void getEmailList() {
        logger.logp(Level.INFO, "SQLConnector", "getEmailList", "Начало получения списка на отправку уведомлений");
        try {
            ArrayList<SendData> dataToSendList = new ArrayList<>();
            logger.logp(Level.INFO, "SQLConnector", "getEmailList", "Установка соединения");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3308/postdb", "root", "root");
            Statement statement = connection.createStatement();
            logger.logp(Level.INFO, "SQLConnector", "getEmailList", "Создание запроса к списку на отправку");
            ResultSet resultSet;
            if (errorId==0) {
                statement.executeQuery("select max(log_id) as max from logs");
                resultSet = statement.getResultSet();
                if (resultSet.next()) {
                    errorId = resultSet.getInt("max");
                }
            }
            logger.logp(Level.INFO, "SQLConnector", "getEmailList", "Выполнение запроса");
            statement.executeQuery("select * from send_list");
            logger.logp(Level.INFO, "SQLConnector", "getEmailList", "Получение ответа");
            resultSet = statement.getResultSet();
            while (resultSet.next()) {
                dataToSendList.add(new SendData(resultSet.getString("email"), resultSet.getString("paper_id"), resultSet.getString("client_name"), LocalDate.parse(resultSet.getString("reminder_date")), resultSet.getInt("type")));
            }
            logger.logp(Level.INFO, "SQLConnector", "getEmailList", "Передача списка на отправку");
            ArrayList<EmailHandler.EmailSendResult> sendResults = EmailHandler.sendEmails(dataToSendList);
            logger.logp(Level.INFO, "SQLConnector", "getEmailList", "Создание запроса на удаление из списка на отправку");
            StringBuilder deleteQuery = new StringBuilder("delete from send_list where ");
            boolean toDelete = false;
            for (EmailHandler.EmailSendResult item : sendResults) {
                if (item.getError() == -1) {
                    toDelete = true;
                    deleteQuery.append(String.format("paper_id='%s' or ", item.getId()));
                } else {
                    logger.logp(Level.INFO, "SQLConnector", "getEmailList", "Запрос на обновление с результатом ошибки отправки");
                    statement.executeUpdate(String.format("update send_list set attempt = attempt + 1, error_log_id=%d where paper_id='%s'", item.getError(), item.getId()));

                }
            }
            if (toDelete) {
                logger.logp(Level.INFO, "SQLConnector", "getEmailList", "Запрос на удаление");
                statement.executeUpdate(deleteQuery.toString().substring(0, deleteQuery.length() - 3));
            }
            logger.logp(Level.INFO, "SQLConnector", "getEmailList", "Закрытие соединения");
            connection.close();
        } catch (SQLException ex) {
            errorCount++;
            logger.logp(Level.SEVERE, "SQLConnector", "getEmailList", "Ошибка при подключении к базе данных");
            Tets2Application.databaseErrors.add(new ErrorLog(getErrorId(), ex.getErrorCode(), ex.getMessage()));
            System.out.println(ex.getMessage());
        }
    }

    public static ArrayList<ErrorLog> GetErrors(String admin) throws SQLException {
        logger.logp(Level.INFO, "SQLConnector", "GetErrors", "Начало получения ошибок из бд");
        try {
            ArrayList<ErrorLog> errorList = new ArrayList<>();
            logger.logp(Level.INFO, "SQLConnector", "GetErrors", "Установка соединения");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3308/postdb", "root", "root");
            Statement statement;
            statement = connection.createStatement();
            logger.logp(Level.INFO, "SQLConnector", "GetErrors", "Выполнение запроса");
            statement.executeQuery(String.format("select * from logs where admin_login='%s'", admin));
            logger.logp(Level.INFO, "SQLConnector", "GetErrors", "Получение ответа");
            ResultSet resultSet = statement.getResultSet();
            while (resultSet.next()) {
                errorList.add(new ErrorLog(resultSet.getInt("log_id"), resultSet.getInt("error_code"), resultSet.getString("error_message")));
            }
            logger.logp(Level.INFO, "SQLConnector", "GetErrors", "Закрытие соединения");
            connection.close();
            return errorList;
        } catch (SQLException ex) {
            errorCount++;
            logger.logp(Level.SEVERE, "SQLConnector", "GetErrors", "Ошибка при подключении к базе данных");
            Tets2Application.databaseErrors.add(new ErrorLog(getErrorId(), ex.getErrorCode(), ex.getMessage()));
            return null;
        }

    }

    public static ArrayList<AdminData> GetAdmins() {
        logger.logp(Level.INFO, "SQLConnector", "GetAdmins", "Начало получение данных администраторов");
        try {
            ArrayList<AdminData> dataList = new ArrayList<>();
            logger.logp(Level.INFO, "SQLConnector", "GetAdmins", "Установка соединения");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3308/postdb", "root", "root");
            Statement statement;
            statement = connection.createStatement();
            logger.logp(Level.INFO, "SQLConnector", "GetAdmins", "Выполнение запроса");
            statement.executeQuery("select * from admins");
            logger.logp(Level.INFO, "SQLConnector", "GetAdmins", "Получение ответа");
            ResultSet resultSet = statement.getResultSet();
            while (resultSet.next()) {
                dataList.add(new AdminData(resultSet.getString("admin_login"), resultSet.getString("admin_password")));
            }
            logger.logp(Level.INFO, "SQLConnector", "GetAdmins", "Закрытие соединения");
            connection.close();
            return dataList;
        } catch (SQLException ex) {
            errorCount++;
            logger.logp(Level.SEVERE, "SQLConnector", "GetAdmins", "Ошибка при подключении к базе данных");
            Tets2Application.databaseErrors.add(new ErrorLog(getErrorId(), ex.getErrorCode(), ex.getMessage()));
            return null;
        }
    }

    public static void AddError(ErrorLog errorLog) {
        logger.logp(Level.INFO, "SQLConnector", "AddError", "Начало добавления ошибки");
        String adminToday=getAdminToday();
        try {
            logger.logp(Level.INFO, "SQLConnector", "AddError", "Установка соединения");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3308/postdb", "root", "root");
            Statement statement;
            logger.logp(Level.INFO, "SQLConnector", "AddError", "Выполнение запроса");
            statement = connection.createStatement();
            String corrected = errorLog.getMessage().replaceAll("'", "\\\\'");
            statement.executeUpdate(String.format("insert into logs (log_id, error_code, error_message, admin_login) values ('%d', '%d','%s','%s')",errorLog.getId(), errorLog.getCode(), corrected, adminToday));
            logger.logp(Level.INFO, "SQLConnector", "AddError", "Закрытие соединения");
            connection.close();
        } catch (SQLException ex) {
            errorCount++;
            logger.logp(Level.SEVERE, "SQLConnector", "AddError", "Ошибка при подключении к базе данных");
            Tets2Application.databaseErrors.add(new ErrorLog(getErrorId(), ex.getErrorCode(), ex.getMessage()));
        }
    }

    public static void addDatabaseErrors() {
        logger.logp(Level.INFO, "SQLConnector", "addDatabaseErrors", "Начало добавление ошибок с базой данных");
        String adminToday=getAdminToday();
        try {
            logger.logp(Level.INFO, "SQLConnector", "addDatabaseErrors", "Установка соединения");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3308/postdb", "root", "root");
            Statement statement = connection.createStatement();
            ArrayList<ErrorLog> errorLogs = Tets2Application.databaseErrors;
            logger.logp(Level.INFO, "SQLConnector", "addDatabaseErrors", "Создание запроса");
            if (errorLogs.size() != 0) {
                StringBuilder urlInsertSend = new StringBuilder("insert into logs (log_id, error_code, error_message, admin_login) values ");
                for (int i = 0; i < errorLogs.size(); i++) {
                    String corrected = errorLogs.get(i).getMessage().replaceAll("'", "\\\\'");
                    urlInsertSend.append(String.format("('%d','%d','%s','%s')",errorLogs.get(i).getId(), errorLogs.get(i).getCode(), corrected,adminToday));
                    if (i + 1 != errorLogs.size()) {
                        urlInsertSend.append(", ");
                    }
                }
                logger.logp(Level.INFO, "SQLConnector", "addDatabaseErrors", "Выполнение запроса");
                statement.executeUpdate(urlInsertSend.toString());
            }
            logger.logp(Level.INFO, "SQLConnector", "addDatabaseErrors", "Закрытие соединения");
            connection.close();
        } catch (SQLException ex) {
            errorCount++;
            logger.logp(Level.SEVERE, "SQLConnector", "addDatabaseErrors", "Ошибка при подключении к базе данных");

        }
    }

    public static void updateDailyStats() {
        logger.logp(Level.INFO, "SQLConnector", "updateDailyStats", "Начало проверки сроков");
        String adminToday=getAdminToday();
        try {
            logger.logp(Level.INFO, "SQLConnector", "updateDailyStats", "Установка соединения");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3308/postdb", "root", "root");
            Statement statement = connection.createStatement();
            LocalDate today=LocalDate.now();
            logger.logp(Level.INFO, "SQLConnector", "updateDailyStats", "Выполнение запроса на текущие данные");
            statement.executeQuery(String.format("select * from stats where date = '%s-%s-%s'", today.getYear(), today.getMonthValue(), today.getDayOfMonth()));
            String query="";
            logger.logp(Level.INFO, "SQLConnector", "updateDailyStats", "Получение ответа");
            ResultSet resultSet = statement.getResultSet();
            if (!resultSet.next()){
                query=String.format("insert into stats (date,paper_amount,error_count,admin_login) values ('%s-%s-%s', %d, %d, '%s')",today.getYear(),today.getMonthValue(), today.getDayOfMonth(), MainController.counter, errorCount,adminToday);
            }
            else {
                query= String.format("update stats set paper_amount = '%d', error_count = '%d' where date = '%s-%s-%s'", MainController.counter, errorCount, today.getYear(), today.getMonthValue(), today.getDayOfMonth());
            }
            logger.logp(Level.INFO, "SQLConnector", "updateDailyStats", "Выполнение запроса с новыми данными");
            statement.executeUpdate(query);
            logger.logp(Level.INFO, "SQLConnector", "updateDailyStats", "Закрытие соединения");
            connection.close();
        }
        catch( SQLException ex)
        {
            errorCount++;
            logger.logp(Level.SEVERE, "SQLConnector", "updateDailyStats", "Ошибка при подключении к базе данных");
            Tets2Application.databaseErrors.add(new ErrorLog(getErrorId(), ex.getErrorCode(), ex.getMessage()));
        }
    }
    private static String getAdminToday() {
        return "admin";
    }


}
