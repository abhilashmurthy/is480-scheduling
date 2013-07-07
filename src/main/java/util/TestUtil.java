/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author suresh
 */
public class TestUtil {
	
	public static void main(String[] args) {
		Calendar cal = Calendar.getInstance();
		System.out.println("Cal time: " + cal.getTimeInMillis());
		cal.set(2013, 0, 1, 0, 0, 0);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println(sdf.format(cal.getTime()));
		Timestamp time = new Timestamp(cal.getTimeInMillis());
		System.out.println(time.toString());
	}
	
}
