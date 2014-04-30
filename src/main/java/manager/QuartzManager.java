/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.servlet.ServletContext;
import model.Booking;
import model.Settings;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import systemAction.quartz.SMSReminderJob;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class QuartzManager {
	private static Logger logger = LoggerFactory.getLogger(QuartzManager.class);
	
	public static void scheduleSMSReminder(Booking booking, EntityManager em, ServletContext ctx) throws Exception {
		Settings reminderSettings = SettingsManager.getNotificationSettings(em);
		int smsHours = -1 * new Gson().fromJson(reminderSettings.getValue(), JsonArray.class).get(1).getAsJsonObject().get("smsFrequency").getAsInt();
		if (smsHours == 0) return; //0 means the feature is disabled
		StdSchedulerFactory factory = (StdSchedulerFactory) ctx.getAttribute(QuartzInitializerListener.QUARTZ_FACTORY_KEY);
		Scheduler scheduler = factory.getScheduler();
		JobDetail jd = JobBuilder.newJob(SMSReminderJob.class)
				.usingJobData("bookingId", booking.getId())
				.withIdentity(String.valueOf(booking.getId()), MiscUtil.SMS_REMINDER_JOBS).build();
		//Calculating the time to trigger the job
		Calendar scheduledTime = Calendar.getInstance();
		if (MiscUtil.DEV_MODE) { //TESTING CODE
			scheduledTime.add(Calendar.MINUTE, 1);
		} else { //LIVE CODE
			scheduledTime.setTimeInMillis(booking.getTimeslot().getStartTime().getTime());
			scheduledTime.add(Calendar.HOUR, smsHours);
			if (scheduledTime.getTime().before(new Date())) {
				scheduledTime.setTimeInMillis(new Date().getTime());
				scheduledTime.add(Calendar.SECOND, 10);
			}	
		}
		
		logger.debug("Scheduled SMS at: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(scheduledTime.getTimeInMillis()));
		Trigger tr = TriggerBuilder.newTrigger().withIdentity(String.valueOf(booking.getId()), MiscUtil.SMS_REMINDER_JOBS)
						.startAt(scheduledTime.getTime()).build();
		scheduler.scheduleJob(jd, tr);
	}
	
	//Deleting the scheduled job to send SMS reminders
	public static void deleteSMSReminder(Booking b, ServletContext ctx) throws Exception {
		StdSchedulerFactory factory = (StdSchedulerFactory) ctx.getAttribute(QuartzInitializerListener.QUARTZ_FACTORY_KEY);
		Scheduler scheduler = factory.getScheduler();
		scheduler.deleteJob(new JobKey(b.getId().toString(), MiscUtil.SMS_REMINDER_JOBS));
	}
}
