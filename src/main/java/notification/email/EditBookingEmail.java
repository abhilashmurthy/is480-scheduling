/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package notification.email;

import constant.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import manager.ICSFileManager;
import manager.UserManager;
import model.Booking;
import model.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author suresh
 */
public class EditBookingEmail extends EmailTemplate {
	
	public enum EditType {
		VENUE, TIME, OPTIONAL_ATTENDEES
	}
	
	Booking b;
	User editor;
	ArrayList<EditType> partsEdited;
	
	public EditBookingEmail(Booking b, User editor, ArrayList<EditType> partsEdited) {
		super("edited_booking.html");
		this.b = b;
		this.editor = editor;
		this.partsEdited = partsEdited;
	}

	@Override
	public String generateEmailSubject() {
		return generateBookingSubjectTitle(b, "Booking updated for ");
	}

	@Override
	public Set<String> generateToAddressList() {
		Set<String> emails = new HashSet<String>();
		
		//Adding the team members
		for (User u : b.getTeam().getMembers()) {
			emails.add(u.getEmail());
		}

		//Including other people in the email only if it is an important update: VENUE and/or TIME
		if (partsEdited.contains(EditType.TIME) || partsEdited.contains(EditType.VENUE)) {
			//Adding required attendees
			for (User u : b.getResponseList().keySet()) {
				emails.add(u.getEmail());
			}
			//Check if the booking was previously confirmed
			boolean confirmed = true;
			for (Response r : b.getResponseList().values()) {
				if (r == Response.PENDING || r == Response.REJECTED) {
					confirmed = false;
					break;
				}
			}

			//Adding the course coordinator and optional attendees if this is a previously confirmed booking
			if (confirmed) {
				//Adding the course coordinator
				emails.add(UserManager.getCourseCoordinator(em).getEmail());

				//Adding the optional attendees
				for (String s : b.getOptionalAttendees()) {
					emails.add(s);
				}
			}	
		}
		return emails;
	}

	@Override
	public Set<String> generateCCAddressList() {
		HashSet<String> emails = new HashSet<String>();
		//Adding the person who updated the details
		emails.add(editor.getEmail());
		
		return emails;
	}

	@Override
	public HashMap<String, String> prepareBodyData() {
		HashMap<String, String> map = new HashMap<String, String>();
		
		map = generateStandardDetails(b, map);
		
		//Inserting editor name
		map.put("[EDITOR_NAME]", editor.getFullName());
		
		/*
		 * HIGHLIGHTING THE INFORMATION THAT HAS BEEN UPDATED
		 */
		ArrayList<Element> toBeHighlighted = new ArrayList<Element>();
		Document html = Jsoup.parse(body);
		
		if (partsEdited.contains(EditType.TIME)) { //Time information will be highlighted
			toBeHighlighted.add(html.getElementById("startTime"));
			toBeHighlighted.add(html.getElementById("endTime"));
		}
		
		if (partsEdited.contains(EditType.VENUE)) { //Venue information will be highlighted
			toBeHighlighted.add(html.getElementById("venue"));
		}
		
		if (partsEdited.contains(EditType.OPTIONAL_ATTENDEES)) { //Optional attendees will be highlighted
			toBeHighlighted.add(html.getElementById("optionalAttendees"));
		}
		
		for (Element e : toBeHighlighted) {
			e.attr("style", "background-color: yellow;");
		}
		
		body = html.toString();
		
		/*
		 * END OF HIGHLIGHT CODE
		 */
		
		return map;
	}

	@Override
	public File getFileAttachment() {
		return ICSFileManager.createICSFile(b);
	}

	@Override
	public String getFileAttachmentName() {
		return b.getTeam().getTeamName() + " - Updated.ics";
	}
	
}
