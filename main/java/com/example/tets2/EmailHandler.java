package com.example.tets2;

import com.sanctionco.jmail.JMail;
import org.simplejavamail.MailException;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailHandler {
    private static Logger logger=Logger.getLogger(Tets2Application.class.getName());

    public static class EmailSendResult {
        private String id;
        private int errorId;

        public EmailSendResult(String id, int error) {
            this.id = id;
            this.errorId = error;
        }

        public String getId() {
            return id;
        }

        public int getError() {
            return errorId;
        }
    }
    public static ArrayList<EmailSendResult> sendEmails(ArrayList<SQLConnector.SendData> list) {
        logger.logp(Level.INFO, "EmailHandler", "sendEmails", "Начало отправки списка email");
        ArrayList<EmailSendResult> results=new ArrayList<>();
        for (SQLConnector.SendData item: list) {
            results.add(new EmailSendResult(item.getId(), SendExpiryNotification(item.getName(), item.getEmail(),item.getExpirationDate(), item.getType())));
        }
        return results;
    }
    private static int SendExpiryNotification(String name, String emailLine, LocalDate expiryDate, int type) {
        logger.logp(Level.INFO, "EmailHandler", "SendExpiryNotification", "Начало отправки письма");

        String typeString="";
        switch (type) {
            case 1:
                typeString="-доверенность";
                break;
            case 2:
            case 3:
                typeString=" о досыле";
                break;
            default:
                break;
        }
        logger.logp(Level.INFO, "EmailHandler", "SendExpiryNotification", "Создание письма");
        Email email = EmailBuilder.startingBlank()
                .from("Почта", "post@post.by")
                .to(name, emailLine)
                .withSubject("Напоминание о заявлении")
                .withPlainText(String.format("Здравствуйте. \nНапоминаем, что ваше заявление%s на %s заканчивается уже через 30 дней.\n " +
                        "Если хотите и дальше пользоваться услугами почты, требуется его обновить до %d %s %d.",typeString, name, expiryDate.getDayOfMonth(), expiryDate.getMonth().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("ru")), expiryDate.getYear()))
                .buildEmail();
        try {
            logger.logp(Level.INFO, "EmailHandler", "SendExpiryNotification", "Отправка письма");

            MailerBuilder
                    .withSMTPServer("127.0.0.1", 8025)
                    .buildMailer()
                    .sendMail(email);
            return -1;
        } catch (MailException ex) {
            logger.logp(Level.SEVERE, "EmailHandler", "SendExpiryNotification", "Ошибка при отправке письма");
            int errorId=SQLConnector.getErrorId();
            ErrorLog log=new ErrorLog(errorId,500,ex.getMessage());
            SQLConnector.AddError(log);
            SQLConnector.errorCount++;
            return errorId;
        }
    }
    public static boolean ValidateEmail(String email) {
        return JMail.isValid(email);
    }
}

