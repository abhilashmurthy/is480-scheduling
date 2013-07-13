/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import model.Schedule;

/**
 *
 * @author suresh
 */
public class TestUtil {
	
	public static void main(String[] args) {
		Schedule schedule = MiscUtil.getActiveSchedule();
		System.out.println("Schedule: End Date = " + schedule.getEndDate() + ", Milestone = " + schedule.getMilestone().getName());
	}
}
