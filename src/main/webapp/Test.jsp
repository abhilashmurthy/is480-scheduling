

<%@page import="org.hibernate.Session"%>
<%@page import="util.HibernateUtil"%>
<%@page import="util.HibernateUtil"%>
<%@page import="model.User"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>Test</title>
    </head>
    <body>
        <% 
	    System.out.println("Code called");
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            hibernateSession.beginTransaction();
            
            User user = new User();
            user.setEmail("lala@smu.edu.sg");
            user.setFirstName("La");
            user.setLastName("La");
	    
	    System.out.println("User object: " + user.toString());
            
            hibernateSession.save(user);
            hibernateSession.getTransaction().commit();
        %>
        <p>Success!!</p>
    </body>
</html>