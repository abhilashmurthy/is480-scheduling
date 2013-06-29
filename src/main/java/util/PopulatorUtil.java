/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;
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

    public void populate() {
        Elements rows = term1.getElementsByTag("tr");
        rows.remove(0); //Removes header row
        for (Element row : rows) {
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
        }
        
    }
}
