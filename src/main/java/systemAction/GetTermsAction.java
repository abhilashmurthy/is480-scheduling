/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import manager.TermManager;
import model.Term;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import userAction.CreateBookingAction;

/**
 *
 * @author Prakhar
 */
public class GetTermsAction extends ActionSupport implements ServletRequestAware{
	private List<Term> listTerms;
	private ArrayList<HashMap<String, String>> data;
	private HttpServletRequest request;
	static final Logger logger = LoggerFactory.getLogger(CreateBookingAction.class);
	
	@Override
	public String execute() {
		//Getting all the term objects
		listTerms = TermManager.getAllTerms();
		if (listTerms != null) {
			HashMap<String, String> map = new HashMap<String, String>();
			data = new ArrayList<HashMap<String, String>>();
			for (Term term: listTerms) {
				int semester = term.getSemester();  //e.g. 1
				int year = term.getAcademicYear();  //e.g. 2013
				long termId = term.getId();
				//e.g. 2013-2014
				String academicYear = Integer.toString(year) + "-" + Integer.toString(year + 1);
				//e.g. Term 1
				String academicTerm = " Term " + Integer.toString(semester);
				String termName = academicYear + academicTerm;
				map.put("termName", termName);
				map.put("termId", Long.toString(termId));
				
				data.add(map);
			}
		} else {
			request.setAttribute("error", "You cannot create a schedule right now. Please create a term first!");
            logger.error("User cannot create schedule before creating term.");
            return ERROR;
		}
		return SUCCESS;
	}

	public ArrayList<HashMap<String, String>> getData() {
		return data;
	}
	
	public void setData(ArrayList<HashMap<String, String>> data) {
		this.data = data;
	}
	
	public void setServletRequest(HttpServletRequest hsr) {
		request = hsr;
	}
}
