/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Team;
import model.Term;
import model.User;
import model.dao.TeamDAO;
import model.dao.TermDAO;
import model.dao.UserDAO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ABHILASHM.2010
 */
public class PopulatorUtil {

    //The wiki stuff
    Elements tables;
    Element term1;
    //Our own stuff
    static final org.slf4j.Logger logger = LoggerFactory.getLogger(PopulatorUtil.class);
    List<User> users = new ArrayList<User>();

    //Testing
    public static void main(String[] args) {
        PopulatorUtil p = new PopulatorUtil();
        p.init();
        p.populateTerms();
        p.populate();
    }

    public void init() {
        try {
            Document doc = Jsoup.connect("https://wiki.smu.edu.sg/is480/Main_Page").get();
            //Get base tables
            tables = doc.select("table");
            //Using Term 1 for dummy data
            term1 = tables.get(2);
        } catch (IOException ex) {
            logger.error("\nException caught: " + ex.getMessage());
        }
    }
    
    public void populateTerms() {
        Object[][] terms = {
            {new Date(2012, 1, 1), 1},
            {new Date(2012, 1, 1), 2},
            {new Date(2013, 1, 1), 1},
            {new Date(2013, 1, 1), 2}
        };
        for(Object[] term : terms) {
            Term newTerm = new Term();
            newTerm.setYear((Date) term[0]);
            newTerm.setTerm((Integer) term[1]);
            TermDAO.save(newTerm);
        }
    }

    public void populate() {
        Elements rows = term1.getElementsByTag("tr");
        rows.remove(0); //Removes header row
        for (Element row : rows) {
            //Getting the users
            Elements cells = row.getElementsByTag("td");
            //gets team names from the first anchor tag of every 3rd cell in each row. value of the attribute "name" of that
            //achor tag is the team name
            String supervisor = cells.get(0).ownText();
            String[] reviewers = cells.get(1).ownText().split("&");
            String teamName = cells.get(2).select("a").first().attr("name");
            Element membersCell = cells.get(4);
            Elements members = membersCell.getElementsByTag("li");
            ArrayList<String> memberList = new ArrayList<String>();
            for (Element member : members) {
                memberList.add(member.ownText());
            }
            String client = cells.get(5).ownText();
            
            //Storing the users
            User supervisorUser = new User();
            supervisorUser.setFirstName(supervisor.split(" ")[0]);
            supervisorUser.setLastName(supervisor.split(" ")[1]);
            supervisorUser.setEmail(supervisor.replaceAll(" ", "") + "@smu.edu.sg");
            UserDAO.save(supervisorUser);
            
            User reviewer1User = new User();
            reviewer1User.setFirstName(reviewers[0].split(" ")[0]);
            reviewer1User.setLastName(reviewers[0].split(" ")[1]);
            reviewer1User.setEmail(reviewers[0].replaceAll(" ", "") + "@smu.edu.sg");
            UserDAO.save(reviewer1User);
            
            User reviewer2User = new User();
            reviewer2User.setFirstName(reviewers[1].split(" ")[0]);
            reviewer2User.setLastName(reviewers[1].split(" ")[1]);
            reviewer2User.setEmail(reviewers[1].replaceAll(" ", "") + "@smu.edu.sg");
            UserDAO.save(reviewer2User);
            
            //Storing the teams
            Team team = new Team();
            team.setTeamName(teamName);
            Term term = new Term();
            term.setYear(new Date(2013, 1, 1));
            term.setTerm(1);
            team.setTerm(term);
            team.setSupervisor(supervisorUser);
            team.setReviewer1(reviewer1User);
            team.setReviewer2(reviewer2User);
            TeamDAO.save(team);
            
            for (String member : memberList) {
                User memberUser = new User();
                memberUser.setFirstName(member.split(" ")[0]);
                memberUser.setLastName(member.split(" ")[1]);
                memberUser.setEmail(member.replaceAll(" ", "") + "@smu.edu.sg");
//                memberUser.setTeamId(TeamDAO.getIdByName(teamName));
                UserDAO.save(memberUser);
            }
        }
    }
}
