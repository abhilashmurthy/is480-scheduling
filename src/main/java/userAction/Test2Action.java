package userAction;

import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.ActionSupport;

public class Test2Action extends ActionSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(Test2Action.class);
	
	public String start() throws Exception {
//		Team teamObj = TeamDAO.findByUserId(teamId);
//		
//		if (teamObj != null) {
//			
//			return SUCCESS;
//		}
//		
		return ERROR;
	}

	private int teamId;

	public int getTeamId() {
		return teamId;
	}

	public void setTeamId(int teamId) {
		this.teamId = teamId;
	}
}
