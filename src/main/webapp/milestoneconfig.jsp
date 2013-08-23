<%-- 
    Document   : milestoneconfig
    Created on : Aug 23, 2013, 10:09:25 PM
    Author     : Prakhar
--%>

<!-- Booking History page -->
<%@page import="com.opensymphony.xwork2.ActionContext"%>
<%@page import="com.opensymphony.xwork2.util.ValueStack"%>
<%@page import="model.*"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@page contentType="text/html" pageEncoding="windows-1252"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=windows-1252">
        <title>Milestone Configuration</title>
		<%@include file="footer.jsp"%>
    </head>
    <body>
		<%@include file="navbar.jsp" %>
		<div class="container">
		<h3>Milestone Configuration</h3>
		
		<!-- Kick unauthorized user -->
        <%	if (!activeRole.equals(Role.ADMINISTRATOR) && !activeRole.equals(Role.COURSE_COORDINATOR)) {
                request.setAttribute("error", "Oops. You are not authorized to access this page!");
                RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
                rd.forward(request, response);
            }
         %>
		
		<!-- SECTION: Booking History -->
		<s:if test="%{data.size() > 0 && data != null}"> 
			<table id="milestoneConfigTable" class="table table-hover">
				<thead>
					<tr>
						<th>Order</th>
						<th>Milestone</th>
						<th>Duration</th>
						<th>Required Attendees</th>
					</tr>
				</thead>
				<tbody> 
					<s:iterator value="data">
						<tr>
							<td><s:property value="order"/></td>
							<td><s:property value="name"/></td>
							<td><s:property value="duration"/> minutes</td>
							<td>
							<s:iterator value="attendees">
								<s:property value="attendee"/> <br/> 
							</s:iterator>
							</td>
						</tr>
					</s:iterator>
					</tbody>
				</table>
		</s:if><s:else>
			<h4>No milestones to configure!</h4>
		</s:else>
		</div>
    </body>
</html>
