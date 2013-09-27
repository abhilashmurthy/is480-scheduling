/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import au.com.bytecode.opencsv.CSVReader;
import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.io.File;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import javax.persistence.EntityManager;
import util.MiscUtil;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletContext;
import org.apache.struts2.util.ServletContextAware;
//import util.FilesUtil;
/**
 *
 * @author Prakhar
 */
public class UploadFileAction extends ActionSupport implements ServletContextAware {

    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(UploadFileAction.class);
    private ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    private HashMap<String, Object> json = new HashMap<String, Object>();
	private File file;
    private String fileContentType;
    private String fileFileName;
    private String filesPath;
    private ServletContext context;
	
    @Override
    public String execute() throws Exception {
		EntityManager em = null;
        try {
            em = MiscUtil.getEntityManagerInstance();
//			System.out.println("File Name is:" + getFileFileName());
//			System.out.println("File ContentType is:" + getFileContentType());
			File csvFile = getFile();
			CSVReader reader = new CSVReader(new FileReader(csvFile));
			
			String[] nextLine;
			//Read one line at a time
			while ((nextLine = reader.readNext()) != null)
			{
				for(String token : nextLine) {
					//Print all tokens
					System.out.println(token);
				}
				break;
			}
		
//			json.put("success", true);
//			json.put("message", "File has been uploaded successfully!");
		} catch (Exception e) {
           logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("exception", true);
            json.put("message", "Error with UploadFileAction: Escalate to developers!");
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
		}
		return SUCCESS;
    } //end of execute function

    public ArrayList<HashMap<String, Object>> getData() {
        return data;
    }

    public void setData(ArrayList<HashMap<String, Object>> data) {
        this.data = data;
    }

    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }
	
	public File getFile() {
        return file;
    }
 
    public void setFile(File file) {
        this.file = file;
    }
 
    public String getFileContentType() {
        return fileContentType;
    }
 
    public void setFileContentType(String fileContentType) {
        this.fileContentType = fileContentType;
    }
 
    public String getFileFileName() {
        return fileFileName;
    }
 
    public void setFileFileName(String fileFileName) {
        this.fileFileName = fileFileName;
    }
 
    public void setFilesPath(String filesPath) {
        this.filesPath = filesPath;
    }
 
    @Override
    public void setServletContext(ServletContext ctx) {
        this.context=ctx;
    }
} //end of class