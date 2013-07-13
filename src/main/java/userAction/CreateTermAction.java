/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import javax.servlet.http.HttpServletRequest;
import model.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.EntityTransaction;
import manager.TermManager;
import org.apache.struts2.ServletActionContext;

/**
 *
 * @author Prakhar
 */
public class CreateTermAction extends ActionSupport {

    private int year;
    private int semester;
    private boolean hasBeenAdded;

    public boolean isHasBeenAdded() {
        return hasBeenAdded;
    }

    public void setHasBeenAdded(boolean hasBeenAdded) {
        this.hasBeenAdded = hasBeenAdded;
    }


    private HttpServletRequest request;
    static final Logger logger = LoggerFactory.getLogger(CreateBookingAction.class);

    @Override
    public String execute() throws Exception {
        //To check if this term already exists
        
        request = ServletActionContext.getRequest();

        Term existingTerm = TermManager.findByYearAndSemester(year, semester);
        if (existingTerm != null) {
            request.setAttribute("error", "This term has already been created. Please try again!");
            logger.error("Term already created");
            hasBeenAdded = false;
            return SUCCESS;
//            return ERROR;
        }

        EntityTransaction transaction = null;
        Term term = new Term();
        term.setAcademicYear(year);
        term.setSemester(semester);
        boolean result = TermManager.save(term, transaction);
        if (result == false) {
            request.setAttribute("error", "Oops. Something went wrong on our end. Please try again later.");
            logger.error("Error while creating new term");
            hasBeenAdded = false;
            return SUCCESS;
//            return ERROR;
        }
        hasBeenAdded = true;
        return SUCCESS;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }
}