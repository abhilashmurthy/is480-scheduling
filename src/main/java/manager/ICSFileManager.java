/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import constant.BookingStatus;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletContext;
import model.Booking;
import model.User;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Sequence;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author suresh
 */
public class ICSFileManager {
	
	private static Logger logger = (Logger) LoggerFactory.getLogger(ICSFileManager.class);
	
	public synchronized static File createICSFile(Booking b) {
		return createICSFile(b, false);
	}
	
	public synchronized static File createICSFile(Booking b, boolean previouslyConfirmed) {
		FileOutputStream fs = null;
		try {
			net.fortuna.ical4j.model.Calendar icsCal = createICSCalendarForBooking(b, previouslyConfirmed);
			File icsFile = new File("BID-" + b.getId() + ".ics");
			fs = new FileOutputStream(icsFile);
			CalendarOutputter calOut = new CalendarOutputter();
			calOut.output(icsCal, fs);
			return icsFile;
		} catch (Exception e) {
			logger.error("ICS File error");
			logger.error(e.getMessage());
			for (StackTraceElement s : e.getStackTrace()) {
				logger.error(s.toString());
			}
		} finally {
			if (fs != null) try { fs.close(); } catch (IOException ignore) {}
		}
		return null;
	}
	
	public synchronized static String createICSCalendar(List<Booking> bookings, User u, ServletContext ctx) throws Exception {
		net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(new ProdId("-//Events Calendar//iCal4j 1.0//EN"));
		calendar.getProperties().add(Method.REQUEST);
		calendar.getProperties().add(CalScale.GREGORIAN);

		for (Booking b : bookings) {
			calendar.getComponents().add(createVEvent(b));	
		}
		
		FileOutputStream fs = null;
		try {
			StringBuilder path = new StringBuilder();
			path.append("ICS/").append(u.getUsername());
			if (u.getTerm() != null) path.append("-").append(u.getTerm().getDisplayName());
			path.append(".ics");
			String pathToReturn = path.toString();
			path.insert(0, ctx.getRealPath("/"));
			File icsFile = new File(path.toString());
			fs = new FileOutputStream(icsFile);
			CalendarOutputter calOut = new CalendarOutputter();
			calOut.output(calendar, fs);
			return pathToReturn;
		} catch (Exception e) {
			logger.error("ICS File error");
			logger.error(e.getMessage());
			for (StackTraceElement s : e.getStackTrace()) {
				logger.error(s.toString());
			}
		} finally {
			if (fs != null) try { fs.close(); } catch (IOException ignore) {}
		}
		return null;
	}
	
	private static net.fortuna.ical4j.model.Calendar createICSCalendarForBooking(Booking b, boolean previouslyConfirmed) throws Exception {
		net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(new ProdId("-//Events Calendar//iCal4j 1.0//EN"));
		
		//CODE TO SUPPORT MS OUTLOOK
		if (b.getBookingStatus() == BookingStatus.DELETED || b.getBookingStatus() == BookingStatus.REJECTED) {
			calendar.getProperties().add(Method.CANCEL);
		} else {
			calendar.getProperties().add(Method.REQUEST);
		}
		
		calendar.getProperties().add(CalScale.GREGORIAN);
		calendar.getComponents().add(createVEvent(b, previouslyConfirmed));	
		
		return calendar;
	}
	
	private static VEvent createVEvent(Booking b) throws Exception {
		return createVEvent(b, false);
	}
	
	private static VEvent createVEvent(Booking b, boolean previouslyConfirmed) throws Exception {
		//Initializing timezone settings (GMT +08:00)
		TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
		TimeZone timezone = registry.getTimeZone("Asia/Kuala_Lumpur");
		VTimeZone tz = timezone.getVTimeZone();
		
		//Start time
		GregorianCalendar start = (GregorianCalendar) GregorianCalendar.getInstance();
		start.setTimeZone(timezone);
		start.setTimeInMillis(b.getTimeslot().getStartTime().getTime());

		//End time
		GregorianCalendar end = (GregorianCalendar) GregorianCalendar.getInstance();
		end.setTimeZone(timezone);
		end.setTimeInMillis(b.getTimeslot().getEndTime().getTime());

		//Creating the calendar event (VEvent file)
		StringBuilder eventName = new StringBuilder();
		if (b.getBookingStatus() == BookingStatus.PENDING
			|| (b.getBookingStatus() == BookingStatus.REJECTED && previouslyConfirmed)
			|| (b.getBookingStatus() == BookingStatus.DELETED && !previouslyConfirmed)) {
			eventName.append("(Tentative) ");
		}
		eventName.append(b.getTeam().getTeamName()).append(" - ");
		eventName.append(b.getTimeslot().getSchedule().getMilestone().getName());
		eventName.append(" Presentation");
		DateTime startTime = new DateTime(start.getTimeInMillis());
		DateTime endTime = new DateTime(end.getTimeInMillis());
		VEvent meeting = new VEvent(startTime, endTime, eventName.toString());
		meeting.getProperties().add(tz.getTimeZoneId());
		meeting.getProperties().add(new Organizer("is480.scheduling@gmail.com"));
		Status status;
		if (b.getBookingStatus() == BookingStatus.PENDING) { status = Status.VEVENT_TENTATIVE; }
		else if (b.getBookingStatus() == BookingStatus.APPROVED) { status = Status.VEVENT_CONFIRMED; }
		else { status = Status.VEVENT_CANCELLED; }
		meeting.getProperties().add(status);

		//Generate unique ID for the calendar entry based on the booking ID
		Uid uid = new Uid("IS480-PSAS-BID" + b.getId() + "@" + InetAddress.getLocalHost().getHostName());
		meeting.getProperties().add(uid);
		meeting.getProperties().add(new Sequence(0));
		
		//Adding all attendees
		addRequiredAttendees(meeting, b.getRequiredAttendees());
		for (String s : b.getOptionalAttendees()) { addAttendee(meeting, s, s, Role.OPT_PARTICIPANT); }
		
		return meeting;
	}
	
	private static void addRequiredAttendees(VEvent meeting, Set<User> requiredAttendees) {
		for (User u : requiredAttendees) {
			addAttendee(meeting, u.getFullName(), u.getEmail(), Role.REQ_PARTICIPANT);
		}
	}
	
	private static void addAttendee(VEvent meeting, String fullName, String emailAddress, Role participation) {
		Attendee att = new Attendee(URI.create("mailto:" + emailAddress));
		att.getParameters().add(participation);
		String name = (fullName != null) ? fullName : emailAddress;
		att.getParameters().add(new Cn(name));
		meeting.getProperties().add(att);
	}
}
