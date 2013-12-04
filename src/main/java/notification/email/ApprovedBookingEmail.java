/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package notification.email;

import constant.Response;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import model.Booking;
import model.User;

/**
 *
 * @author suresh
 */
public class ApprovedBookingEmail extends EmailTemplate{
	Booking b;
	User approver;
	
	public ApprovedBookingEmail(Booking b, User approver) {
		super("approved_booking.html");
		this.b = b;
		this.approver = approver;
	}

	@Override
	public String generateEmailSubject() {
		return generateBookingSubjectTitle(b, "Approved by " + approver.getFullName() + " for ");
	}

	@Override
	public Set<String> generateToAddressList() {
		Set<String> emails = new HashSet<String>();
		for (User u : b.getTeam().getMembers()) {
			emails.add(u.getEmail());
		}
		
		return emails;
	}

	@Override
	public Set<String> generateCCAddressList() {
		return null;
	}

	@Override
	public HashMap<String, String> prepareBodyData() {
		HashMap<String, String> map = new HashMap<String, String>();
		
		map = generateStandardDetails(b, map);
		
		//Inserting approver name
		map.put("[APPROVER_NAME]", approver.getFullName());
		
		//Getting the list of people who are yet to respond
		HashSet<User> remainingPeople = new HashSet<User>();
		Iterator<Entry<User, Response>> responseIter = b.getResponseList().entrySet().iterator();
		while (responseIter.hasNext()) {
			Entry<User, Response> e = responseIter.next();
			if (e.getValue() == Response.PENDING) remainingPeople.add(e.getKey());
		}
		//Inserting the names of the people who are yet to respond
		StringBuilder remainingStr = new StringBuilder();
		Iterator<User> remainingIter = remainingPeople.iterator();
		while (remainingIter.hasNext()) {
			remainingStr.append(remainingIter.next().getFullName());
			if (remainingIter.hasNext()) {
				remainingStr.append(",&nbsp;");
			}
		}
		map.put("[REMAINING_RESPONDENTS]", remainingStr.toString());
		
		return map;
	}

	@Override
	public File getFileAttachment() {
		return null;
	}

	@Override
	public String getFileAttachmentName() {
		return null;
	}
	
}
