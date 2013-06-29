<%@page import="model.dao.TermDAO"%>
<%@page import="java.sql.Date"%>
<%@page import="model.Term"%>
<%@page import="model.dao.UserDAO"%>
<%@page import="org.hibernate.Session"%>
<%@page import="util.HibernateUtil"%>
<%@page import="util.HibernateUtil"%>
<%@page import="model.User"%>
<%@page import="org.slf4j.Logger"%>
<%@page import="org.slf4j.LoggerFactory"%>

<%!
    static final Logger logger = LoggerFactory.getLogger("jspLogger");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>Test</title>
    </head>
    <body>
        <%
	    logger.info("Code called");
            
            User user = new User();
            user.setEmail("lala@smu.edu.sg");
            user.setFirstName("La");
            user.setLastName("La");
            
            UserDAO.save(user);
            
            Term term = new Term();
            term.setYear(new Date(2013, 1, 1));
            term.setTerm(1);
            TermDAO.save(term);
	    
	    logger.info("User object: " + user.toString());
            
            
            
            
        %>
        <p>Success!!</p>
    </body>
</html>