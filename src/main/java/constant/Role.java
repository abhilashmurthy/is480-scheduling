/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package constant;

/**
 * List of all the roles present in the system
 * @author suresh
 */
public enum Role {
	STUDENT("Student"),
	FACULTY("Faculty"),
	TA("TA"),
	ADMINISTRATOR("Administrator"),
	COURSE_COORDINATOR("Course Coordinator");
	
	private Role(String displayName) {
		this.displayName = displayName;
	}
	
	private final String displayName;
	
	public String getDisplayName() { return displayName; }
}