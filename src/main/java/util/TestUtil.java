/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import model.Team;
import model.dao.TeamDAO;

/**
 *
 * @author suresh
 */
public class TestUtil {
	public static void main(String[] args) {
		Team team = TeamDAO.findByTeamId(1);
		
		System.out.println("Team retrieved successfully");
	}
}
