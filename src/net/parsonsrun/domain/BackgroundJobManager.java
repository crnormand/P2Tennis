package net.parsonsrun.domain;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener 
public class BackgroundJobManager implements ServletContextListener {

    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        
		Date d = new Date();
		SimpleDateFormat fm = new SimpleDateFormat("m");
		int min = Integer.parseInt(fm.format(d));
		int wait = Math.max(0, 55 - min);
        scheduler.scheduleAtFixedRate(new ReminderBackgroundJob(), wait, 60, TimeUnit.MINUTES);
    	System.out.println("Starting background job manager " + d + ", waiting " + wait + " minutes");
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    	System.out.println("Stopping background job manager");
        scheduler.shutdownNow();
    }

}