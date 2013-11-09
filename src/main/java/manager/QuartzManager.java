/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import javax.servlet.ServletContext;
import model.Booking;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.quartz.impl.StdSchedulerFactory;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class QuartzManager {
	
	//Deleting the scheduled job to send SMS reminders
	public static void deleteSMSReminder(Booking b, ServletContext ctx) throws Exception {
		StdSchedulerFactory factory = (StdSchedulerFactory) ctx.getAttribute(QuartzInitializerListener.QUARTZ_FACTORY_KEY);
		Scheduler scheduler = factory.getScheduler();
		scheduler.deleteJob(new JobKey(b.getId().toString(), MiscUtil.SMS_REMINDER_JOBS));
	}
}
