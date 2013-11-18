/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;
import manager.SettingsManager;
import manager.UserManager;
import model.Settings;
import model.SystemActivityLog;
import model.Term;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.CustomException;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class LoginAction extends ActionSupport implements ServletRequestAware {

    //Request and Response
    private HttpServletRequest request;
	
	//Stores the callback URL in case of an error
	private String responseURL;

    private static Logger logger = LoggerFactory.getLogger(LoginAction.class);
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
		HttpSession session = request.getSession();
		
		Calendar nowCal = Calendar.getInstance();
		Timestamp now = new Timestamp(nowCal.getTimeInMillis());
		
		SystemActivityLog logItem = new SystemActivityLog();
		logItem.setActivity("Login");
		logItem.setRunTime(now);
		
		EntityManager em = null;
        try {
            em = MiscUtil.getEntityManagerInstance();
			
			if (request.getParameter("bypass") != null) { //BYPASS SSO LOGIN
				//Validating password for bypass login
				String password = request.getParameter("password");
				Settings bypassPassword = SettingsManager.getByName(em, "bypassPassword");
				if (password == null || !bypassPassword.getValue().equals(password)) throw new CustomException("Administrator entered "
						+ "incorrect password. Please try again!");
				
				initializeUser(em);
				User userForLog = (User) session.getAttribute("user");
				if (userForLog.getId() != null) logItem.setUser(userForLog);
				logItem.setMessage("Login successful. Administrator logged in as " + userForLog.toString());
			} else { //CODE FOR SSO
				//return to login
				if (request.getParameter("oauth_callback") == null) {
					return "error";
				}
				
				//Setting the smu_groups value in session (For RSVP restrictions)
				String smuGroups = request.getParameter("smu_groups");
				session.setAttribute("smu_groups", smuGroups);
				
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

				if (serverSignature.equals(generatedSignature)) {
					initializeUser(em);
					User userForLog = (User) session.getAttribute("user");
					logItem.setUser(userForLog);
					logItem.setMessage("Login successful. " + userForLog.toString());
				} else {
					//Login unsuccessful
					logItem.setMessage("Login unsuccessful");
					logger.error("Signature mismatch. SSO Login failed. ");
					logger.info("Server signature: " + serverSignature);
					logger.info("Client signature: " + generatedSignature);
					logger.info("Server signature time * 1000: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(serverCal.getTime()));
					logger.info("Client signature time * 1000: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(clientCal.getTime()));
					throw new CustomException("SSO Login failed! Please contact the system administrator.");
				}
			} //END OF CODE FOR SSO
        } catch (CustomException e) {
			User userForLog = (User) session.getAttribute("user");
			if (userForLog != null && userForLog.getId() != null) {
				logItem.setUser(userForLog);
			}
			logItem.setMessage("Error: " + e.getMessage());
			
			//Construct callback URL
			StringBuilder sb = new StringBuilder();
			if (request.getParameter("bypass") != null) sb.append("admin");
			sb.append("login.jsp").append("?error=").append(e.getMessage());
			responseURL = sb.toString();
			return ERROR;
		} catch (Exception e) {
			User userForLog = (User) session.getAttribute("user");
			if (userForLog != null && userForLog.getId() != null) {
				logItem.setUser(userForLog);
			}
			logItem.setMessage("Error: " + e.getMessage());
			
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            request.setAttribute("error", "Error with Login: Escalate to developers!");
            return ERROR;
        } finally {
			 if (em != null) {
				//Saving job log in database
				if (!em.getTransaction().isActive()) em.getTransaction().begin();
				em.persist(logItem);
				em.getTransaction().commit();
				
				if (em.getTransaction().isActive()) em.getTransaction().rollback();
				if (em.isOpen()) em.close();
			}
		}
        return SUCCESS;
    }
	
	private void initializeUser(EntityManager em) throws Exception {
		
		//Check if user exists in our DB
		String smuUsername = request.getParameter("smu_username");
		String smuFullName = request.getParameter("smu_fullname");
		if (smuFullName == null) smuFullName = smuUsername; 
		//Getting the active term
		Term activeTerm = SettingsManager.getDefaultTerm(em);
		
		HttpSession session = request.getSession();
		session.setAttribute("currentActiveTerm", activeTerm);
		UserManager.initializeUser(em, session, smuUsername, smuFullName, activeTerm);
		MiscUtil.logActivity(logger, smuUsername, null, "Logged in");
	}
	
    public HttpServletRequest getServletRequest() {
        return request;
    }

    public void setServletRequest(HttpServletRequest request) {
        this.request = request;
    }

	public String getResponseURL() {
		return responseURL;
	}

	public void setResponseURL(String responseURL) {
		this.responseURL = responseURL;
	}
	
} //end of class
