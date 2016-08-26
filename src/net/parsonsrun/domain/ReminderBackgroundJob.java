package net.parsonsrun.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ReminderBackgroundJob implements Runnable 
{	
    public void run() 
    {
    	try
    	{
    		if (Neighborhood.getSingleton() != null)
    		{
				Date d = new Date();
				SimpleDateFormat fd = new SimpleDateFormat("u");
				SimpleDateFormat fh = new SimpleDateFormat("H");
				int day = Integer.parseInt(fd.format(d));
				int hour = Integer.parseInt(fh.format(d));
				if (day == 5 && hour == 0) // Friday morning between midnight and 1am
					Neighborhood.getSingleton().sendUnplayedMatchReminders("This is a friendly automated reminder.");
				if (day == 7 && hour == 0) // Sunday morning between midnight and 1am
					Neighborhood.getSingleton().sendUnplayedMatchReminders("This is an URGENT automated reminder.<br><br>If the match has not been scored by midnight, both teams will default.");
				if (day == 7 && hour == 23) // Sunday night between 11 and midnight
					Neighborhood.getSingleton().closeOutWeek(d);
    		}
    	} catch (Exception e)
    	{
    		System.out.println("Caught exception in background process: " + e);
    		e.printStackTrace();
    	}
    }
}
