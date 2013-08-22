/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;
import manager.SettingsManager;
import manager.UserManager;
import model.Term;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class LoginAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

    //Request and Response
    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(LoginAction.class);
    private HttpServletResponse response;
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
        try {
            EntityManager em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
            logger.info("Reached LoginAction");
			
			if (true) { //CODE FOR LOCALHOST TESTING
				initializeUser(em);
			} else { //CODE FOR PRODUCTION SERVER
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
			} //END OF CODE FOR PRODUCTION SERVER

            //To check if user has multiple roles 
//            if (userRoles.size() > 1) {
//				isSupervisorReviewer = false;
//				isAdministrator = false;
//				isCourseCoordinator = false;
//				for (Role role: userRoles) {
//					if (role.getName().equalsIgnoreCase("Supervisor") || 
//							role.getName().equalsIgnoreCase("Reviewer")) {
//						isSupervisorReviewer = true;
//					} else if (role.getName().equalsIgnoreCase("Administrator")) {
//						isAdministrator = true;
//					} else if (role.getName().equalsIgnoreCase("Course Coordinator")) {
//						isCourseCoordinator = true;
//					}
//				}
//				//If user is just supervisor & reviewer then he wont go to the multiple roles page
//				if (isAdministrator == true || isCourseCoordinator == true) {
//					return "goToRoles";
//				}
//            }
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            request.setAttribute("error", "Error with Login: Escalate to developers!");
            return ERROR;
        }
        return SUCCESS;
    }
	
	private void initializeUser(EntityManager em) throws ServletException, IOException {
		HttpSession session = request.getSession();
		
		//Check if user exists in our DB
		String smuUsername = request.getParameter("smu_username");
		//Getting the active term
		ArrayList<Term> activeTerms = SettingsManager.getActiveTerms(em);
		//Getting the user based on username and active term
		ArrayList<User> users = UserManager.findActiveRolesByUsername (em, smuUsername, activeTerms.get(0));
		
		if (users.size() > 0) {
			//Welcome to the system. 
			session.setAttribute("userRoles", users);
			session.setAttribute("currentActiveTerm", activeTerms.get(0));

		} else {
			//Kick user out
			request.setAttribute("error", "Yo, understand you're from SMU, but you can't login here yo!");
			request.getRequestDispatcher("login.jsp").forward(request, response);
		}
	}

    public HttpServletRequest getServletRequest() {
        return request;
    }

    public HttpServletResponse getServletResponse() {
        return response;
    }

    public void setServletRequest(HttpServletRequest request) {
        this.request = request;
    }

    public void setServletResponse(HttpServletResponse response) {
        this.response = response;
    }

} //end of class
