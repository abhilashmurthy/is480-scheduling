/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package constant;

import model.User;
import model.role.Faculty;
import model.role.Student;
import model.role.TA;

/**
 * List of all the roles present in the system
 * @author suresh
 */
public enum Role {
	STUDENT("Student", Student.class),
	FACULTY("Faculty", Faculty.class),
	TA("TA", TA.class),
	ADMINISTRATOR("Administrator", User.class),
	COURSE_COORDINATOR("Course Coordinator", User.class),
	GUEST("Guest", User.class);
	
	private Role(String displayName, Class baseClassType) {
		this.displayName = displayName;
		this.baseClassType = baseClassType;
	}
	
	private final String displayName;
	private final Class baseClassType;
	
	public String getDisplayName() { return displayName; }
	
	public Class getBaseClassType() { return baseClassType; }
}