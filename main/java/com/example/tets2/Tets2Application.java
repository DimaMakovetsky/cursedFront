package com.example.tets2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.core.env.Environment;

import java.io.Console;
import java.io.IOException;
import java.nio.file.LinkOption;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.logging.*;


@SpringBootApplication
public class Tets2Application{
	@Autowired
	private static Logger logger=Logger.getLogger(Tets2Application.class.getName());
	public static ArrayList<ErrorLog> databaseErrors;
	public static void main(String[] args) throws InterruptedException {
		Logger root=Logger.getLogger("");
		Handler[] handlers=root.getHandlers();
		for (Handler handler: handlers) {
			root.removeHandler(handler);
		}
		try {
			String logName=LocalDate.now().toString();
			Handler handler=new FileHandler(String.format("src/main/resources/logs/log_%s.txt",logName),1024*1024,5,true);
			handler.setFormatter(new SimpleFormatter());
			handler.setLevel(Level.ALL);
			root.addHandler(handler);

		} catch (Exception e) {
			logger.logp(Level.SEVERE, "Tets2Application", "crateLogger", "Не удалось создать логгер", e);
		}
		logger.logp(Level.INFO, "Tets2Application", "main", "Старт приложения");
		SpringApplication.run(Tets2Application.class, args);
		databaseErrors=new ArrayList<>();
		logger.logp(Level.INFO, "Tets2Application", "main", "Старт таймера");
		new Timer().scheduleAtFixedRate(new DatabaseRoutineTasks(), 0,1000);

	}
}
