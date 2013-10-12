/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;
import manager.SettingsManager;
import manager.UserManager;
import model.Term;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class LoginAction extends ActionSupport implements ServletRequestAware {

    //Request and Response
    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(LoginAction.class);
	private ArrayList<Role> allRoles = new ArrayList<Role>();
    private boolean isSupervisorReviewer;
    private boolean isAdministrator;
    private boolean isCourseCoordinator;
    // sorted in alphabetical order. ordering is important
    // when generating the signature
    private static final String[] keys = {
        "oauth_callback",
        "oauth_consumer_key",
        "oauth_nonce",
        "oauth_signature_method",
        "oauth_timestamp",
        "oauth_version",
        "smu_domain",
        "smu_fullname",
        "smu_groups",
        "smu_username"};
    private static final String SECRET_KEY = "psastest2012";

    public static String encode(String plain) {
        try {
            String encoded = URLEncoder.encode(plain, "UTF-8");

            return encoded.replace("*", "%2A")
                    .replace("+", "%20")
                    .replace("%7E", "~");
        } catch (Exception e) {
            e.printStackTrace(); // hopefully this wont happen
        }
        return "";
    }

    @Override
    public String execute() throws Exception {
		EntityManager em = null;
        try {
            em = MiscUtil.getEntityManagerInstance();
            logger.info("Reached LoginAction");
			
			if (request.getParameter("bypass") != null) { //BYPASS SSO LOGIN
				initializeUser(em);
			} else { //CODE FOR SSO
				//return to login
				if (request.getParameter("oauth_callback") == null) {
					return "error";
				}

				//Set strings of parameters
				for (Object o : request.getParameterMap().keySet()) {
					logger.info("Parameter key: " + (String) o + ", value: " + request.getParameter((String) o));
				}

				//Get callback URL
				String callbackUrl = null;
				if (request.getServerName().equals("localhost")) {
					callbackUrl = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getRequestURI();
				} else {
					callbackUrl = "http://" + request.getServerName() + request.getRequestURI();
				}

				String uri = "POST&" + encode(callbackUrl) + "&";
				String pairs = "";
				for (int i = 0; i < keys.length - 1; i++) {
					pairs += keys[i] + "=" + encode(request.getParameter(keys[i])) + "&";
				}

				//for-loop above stops before encoding the last key-value pair because
				// the last key-value pair doesnt need the trailing & character 
				pairs += keys[keys.length - 1] + "=" + encode(request.getParameter(keys[keys.length - 1]));
				uri += encode(pairs);

				// initialize the Mac object
				Mac mac = Mac.getInstance("HmacSHA1");
				SecretKey secretKey = new SecretKeySpec((SECRET_KEY + "&").getBytes(), mac.getAlgorithm());
				mac.init(secretKey);

				// generates the signature & retrieves server signature
				String generatedSignature = DatatypeConverter.printBase64Binary(mac.doFinal(uri.getBytes())).trim();
				String serverSignature = (request.getParameter("oauth_signature"));

				// gets the time when the server generates the signature
				long serverSignatureTime = Long.parseLong(request.getParameter("oauth_timestamp"));
				Calendar serverCal = Calendar.getInstance();
				serverCal.setTimeInMillis(serverSignatureTime);
				serverCal.setTimeInMillis(serverSignatureTime * 1000);

				// gets the current time in seconds
				Calendar clientCal = Calendar.getInstance();
				long currentTime = System.currentTimeMillis() / 1000;
				clientCal.setTimeInMillis(currentTime);
				clientCal.setTimeInMillis(currentTime * 1000);

				logger.info("Server signature: " + serverSignature);
				logger.info("Client signature: " + generatedSignature);
				logger.info("Server signature time * 1000: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(serverCal.getTime()));
				logger.info("Client signature time * 1000: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(clientCal.getTime()));

				if (serverSignature.equals(generatedSignature)) {
					initializeUser(em);
				} else {
					//Login unsuccessful
					logger.error("LOGIN - SOMETHING WENT WRONG");
				}
			} //END OF CODE FOR SSO
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            request.setAttribute("error", "Error with Login: Escalate to developers!");
            return ERROR;
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
		}
        return SUCCESS;
    }
	
	private void initializeUser(EntityManager em) throws ServletException, IOException {
		populateAllRoles();
		HttpSession session = request.getSession();
		
		//Check if user exists in our DB
		String smuUsername = request.getParameter("smu_username");
		String smuFullName = request.getParameter("smu_fullname");
		if (smuFullName == null) smuFullName = smuUsername; 
		//Getting the active term
		Term activeTerm = SettingsManager.getDefaultTerm(em);
		ArrayList<User> users = UserManager.findActiveRolesByUsername (em, smuUsername, activeTerm);

		if (users.isEmpty()) {
			User tempUser = new User(smuUsername, smuFullName, null, Role.GUEST, activeTerm);
		} else {
			User chosenRole = chooseRole(users); //Choosing the default role to begin with
			session.setAttribute("user", UserManager.getUser(chosenRole));
			session.setAttribute("activeRole", chosenRole.getRole());
			users.remove(chosenRole); //Removing the chosen object from the list of users
		}

		session.setAttribute("userRoles", users);
		session.setAttribute("currentActiveTerm", activeTerm);
	}
	
	//Choosing the user object with the least important/powerful role
	private User chooseRole(ArrayList<User> users) {
		User user = users.get(0);
		int smallestRoleIndex = allRoles.indexOf(user.getRole());
		
		Iterator<User> iter = users.iterator();
		while (iter.hasNext()) {
			User u = iter.next();
			if (allRoles.indexOf(u.getRole()) > smallestRoleIndex) { //Current object's role is the least important until now
				user = u;
				smallestRoleIndex = allRoles.indexOf(u.getRole());
			}
		}
		
		return user;
	}
	
	//Adding all the roles in the list in decreasing order of importance/power
	private void populateAllRoles() {
		allRoles.add(Role.ADMINISTRATOR);
		allRoles.add(Role.COURSE_COORDINATOR);
		allRoles.add(Role.FACULTY);
		allRoles.add(Role.STUDENT);
		allRoles.add(Role.TA);
		allRoles.add(Role.GUEST);
	}

    public HttpServletRequest getServletRequest() {
        return request;
    }

    public void setServletRequest(HttpServletRequest request) {
        this.request = request;
    }

} //end of class
