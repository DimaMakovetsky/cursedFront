package com.example.tets2;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseRoutineTasks extends TimerTask {
    private Logger logger=Logger.getLogger(Tets2Application.class.getName());

    @Override
    public void run()
    {
        logger.logp(Level.INFO, "DatabaseRoutineTasks", "run", "Начало регулярных задач");

        try {
            SQLConnector.CheckDates();
		    SQLConnector.getEmailList();
		    SQLConnector.addDatabaseErrors();
		    SQLConnector.updateDailyStats();
            logger.logp(Level.INFO, "DatabaseRoutineTasks", "run", "Конец регулярных задач");

            TimeUnit.MINUTES.sleep(10);
        }
        catch (InterruptedException ex) {
            logger.logp(Level.SEVERE, "DatabaseRoutineTasks", "run", "Ошибка в потоке регулярных задач", ex);
            throw new RuntimeException(ex);
        }
    }
}
