<%@page import="javax.persistence.Persistence"%>
<%@page import="javax.persistence.EntityManagerFactory"%>
<%@page import="util.JPAUtil"%>
<%@page import="javax.persistence.EntityManager"%>
<%@page import="util.TestUser"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>IS480 Scheduling</title>
    </head>
    <body>
        
        <div class="container page">
            <% 
                
                String name = "temp";
                String username = "temptemp";
                out.write("Writing new User " + name + ", username: " + username);
                
                EntityManagerFactory emf = Persistence.createEntityManagerFactory("tutorialPU");
                EntityManager em = emf.createEntityManager();
                em.getTransaction().begin();

                TestUser tUser = new TestUser();
                tUser.setName(name);
                
                em.persist(tUser);
                out.write("Persisted User");
                em.getTransaction().commit();
                
                                
            %>
        </div>
    </body>
</html>
