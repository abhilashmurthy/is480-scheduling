<%-- 
    Document   : SlotUpdated
    Created on : Jul 3, 2013, 2:58:45 PM
    Author     : Tarlochan
--%>

<%@page import="model.dao.TimeslotStatusDAO"%>
<%@page contentType="text/html" pageEncoding="windows-1252"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=windows-1252">
        <title>Slot Confirmed</title>
        <%@include file="imports.jsp" %>
    </head>
    <body>
        <% 
           String approve = request.getParameter("Approve"); 
           String reject = request.getParameter("Reject"); 
           String teamId = request.getParameter("teamId");
           String status = "";
           
           //based on teamId, update the database
		   String[] teamInfo = teamId.split("-");
		   teamInfo[0].trim();
		   teamInfo[0].trim();
           int teamInt = Integer.parseInt(teamInfo[0]);
           if(approve!=null){
               status = "ACCEPTED";
           }else if(reject !=null){
               status = "REJECTED";
           }
           
           //out.println(status);
           //out.println(teamId);
           TimeslotStatusDAO.updateTimeSlotStatusByTeamId(teamInt, status);
       
           response.sendRedirect("AcceptReject.jsp");
        
        
        %>
        
        
    </body>
</html>
