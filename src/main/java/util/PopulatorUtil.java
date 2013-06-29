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
            for (Element element : tables.subList(0, tables.size())) {
            }
            
        } catch (IOException ex) {
            logger.error("\nException caught: " + ex.getMessage());
        }
    }
    
    public void populateUsers() {
        
    }
    
}
