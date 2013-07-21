/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import model.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import manager.TermManager;
import org.apache.struts2.interceptor.ServletRequestAware;

/**
 *
 * @author Prakhar
 */
public class CreateTermAction extends ActionSupport implements ServletRequestAware {

    private int year;    
    private String semester;
    private boolean canAdd;
    private HashMap<String, Object> json = new HashMap<String, Object>();

    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }

    public boolean isHasBeenAdded() {
        return canAdd;
    }

    public void setHasBeenAdded(boolean canAdd) {
        this.canAdd = canAdd;
    }


    private HttpServletRequest request;
    static final Logger logger = LoggerFactory.getLogger(CreateBookingAction.class);

    @Override
    public String execute() throws Exception {
        //To check if this term already exists
        
        json.put("year", year);
        json.put("semester", semester);
        
        Term existingTerm = TermManager.findByYearAndSemester(year, semester);
        if (existingTerm != null) {
            request.setAttribute("error", "This term has already been created. Please try again!");
            logger.error("Term already created");
            json.put("canAdd", false);
            return SUCCESS;
//            return ERROR;
        }

        //Don't add Term yet. Add it after Term + Schedule + Timeslots have all been set
//        EntityTransaction transaction = null;
//        Term term = new Term();
//        term.setAcademicYear(year);
//        term.setSemester(semester);
//        boolean result = TermManager.save(term, transaction);
//        if (result == false) {
//            request.setAttribute("error", "Oops. Something went wrong on our end. Please try again later.");
//            logger.error("Error while creating new term");
//            json.put("canAdd", false);
//            return SUCCESS;
////            return ERROR;
//        }
        json.put("canAdd", true);
        return SUCCESS;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }
}
