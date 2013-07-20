<%@page import="model.Role"%>
<%@page import="java.util.List"%>
<%@page import="model.User"%>
<%@page import="org.slf4j.LoggerFactory"%>
<%@page import="org.slf4j.Logger"%>

<!-- CSS imports -->
<link href="css/app.css" rel="stylesheet">
<link href="css/redmond/jquery-ui-1.10.3.custom.min.css" rel="stylesheet">
<link href="css/redmond/jquery.timepicker.css" rel="stylesheet">
<link href="css/bootstrap.css" rel="stylesheet" media="screen">

<%!
    static final Logger logger = LoggerFactory.getLogger("jspLogger");
%>

<!-- Ensure user has logged in -->
<% logger.info("Reached imports"); %>
<% User user = (User) session.getAttribute("user");
if (session.getAttribute("user") == null) {
	response.sendRedirect("login.jsp");
	return; 
}

//To check the user's role
boolean isSupervisorOrReviewer = false;  //Supervisor and Reviewer will have the same view
boolean isStudent = false;
//boolean isAdmin = false;
boolean isTA = false;
//Getting the users roles
List<Role> userRoles = user.getRoles();
if (userRoles != null || userRoles.size() > 0) {
	for (Role role: userRoles) {
		if (role.getName().equalsIgnoreCase("Supervisor") || role.getName().equalsIgnoreCase("Reviewer")) {
			isSupervisorOrReviewer = true;
			session.setAttribute("isSupervisorOrReviewer", isSupervisorOrReviewer);
		} else if (role.getName().equalsIgnoreCase("Student")) {
			isStudent = true;
			session.setAttribute("isStudent", isStudent);
		} else if (role.getName().equalsIgnoreCase("TA")) {
			isTA = true;
			session.setAttribute("isTA", isTA);
		}
	}
} else {
	response.sendRedirect("login.jsp");
	return;
}
%>