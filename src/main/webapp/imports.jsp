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
%>