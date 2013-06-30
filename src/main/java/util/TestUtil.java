/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import model.Schedule;
import model.dao.ScheduleDAO;

/**
 *
 * @author suresh
 */
public class TestUtil {
	public static void main(String[] args) {
		Schedule schedule = ScheduleDAO.findByScheduleId(1, "Acceptance");
		
		System.out.println("Schedule retrieved successfully");
	}
}
