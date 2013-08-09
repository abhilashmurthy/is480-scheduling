<%-- 
    Document   : multipleroles
    Created on : Jul 23, 2013, 1:43:31 PM
    Author     : Prakhar
--%>

<%@page import="java.util.ArrayList"%>
<%@page import="model.Role"%>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="windows-1252"%>
<%@ taglib prefix="s" uri="/struts-tags" %>

<% List<Role> userRoles = (List<Role>) session.getAttribute("userRoles");
   if (userRoles.size() > 1) {  %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=windows-1252">
        <title>Welcome | Your Roles</title>
		<%@include file="footer.jsp"%>	
    </head>
    <body>
        <%@include file="navbar_multipleroles.jsp" %>
        <div class="container">
			<h3>Choose your Role</h3>
				
			<div style="text-align:center" align="middle">
				<br/><br/><br/><br/>
				<div class="btn-group">
				<form id="myform" action="setRole" method="post">
				<table align="center">
					<tr>
					<s:if test="%{isAdministrator}">
 						<td>
							<input type="submit" class="btn btn-large" value="Administrator" name="administrator"/>
							<!--<img src="img/administrator.jpg" class="img-polaroid" title="Administrator" height="200" width="150"/>-->
						</td>
						<td style="width:20px"></td>
					</s:if>
					<s:if test="%{isSupervisorReviewer}">
						<td>
							<input type="submit" class="btn btn-large" value="Supervisor/Reviewer" name="supervisorReviewer"/>
							<!--<img src="img/supervisor.jpg" class="img-polaroid" title="Supervisor" height="210" width="150"/>-->
						</td>
						<td style="width:20px"></td>
					</s:if>
					<s:if test="%{isCourseCoordinator}">
						<td>
							<input type="submit" class="btn btn-large" value="Course Coordinator" name="courseCoordinator"/>
							<!--<img src="img/reviewer.jpg" class="img-polaroid" title="Reviewer" height="220" width="170"/>-->
						</td>
					</s:if>
					</tr>
					
				</table>
				</form><!--</form>-->
				</div>
			</div>
		</div>
					
		<!-- To display error messages -->
		<div class="container">
			<div class="row">
				<% Object msg = request.getAttribute("rolesError");
				   String errorMsg = "";
				   if (msg != null) {
					   errorMsg = msg.toString();
				   }
				%>
				<p class="text-error" style="text-align:center">
					<strong><%= errorMsg %></strong>
				</p>
			</div>
        </div>
    </body>
</html>
<% } else { %>
	<s:action name="index" executeResult="true"/> 	
<% } %>

