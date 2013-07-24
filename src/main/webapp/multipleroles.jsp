<%-- 
    Document   : multipleroles
    Created on : Jul 23, 2013, 1:43:31 PM
    Author     : Prakhar
--%>

<%@page contentType="text/html" pageEncoding="windows-1252"%>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="%{userRoles.size() > 1}"> 
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
				<table align="center">
					<tr>
					<s:if test='isAdmin'>
						<td width="30%">
							<a href="index.jsp?role=ar"><img src="img/administrator.jpg" class="img-polaroid" title="Administrator" height="200" width="150"/></a>
						</td>
					</s:if>
					<s:if test='isSupervisor'>
						<td width="40%">
							<a href="index.jsp?role=sr"><img src="img/supervisor.jpg" class="img-polaroid" title="Supervisor" height="210" width="150">
						</td>
					</s:if>
					<s:if test='isReviewer'>
						<td width="48%">
							<a href="index.jsp?role=rr"><img src="img/reviewer.jpg" class="img-polaroid" title="Reviewer" height="220" width="170">
						</td>
					</s:if>
					</tr>
					<tr>
						<s:if test='isAdmin'>
							<td><h4>Administrator</h4></td>
						</s:if>
						<s:if test='isSupervisor'>
							<td><h4>Supervisor</h4></td>
						</s:if>
						<s:if test='isReviewer'>
							<td><h4>Reviewer</h4></td>
						</s:if>
					</tr>
				</table>
			</div>
		</div>
    </body>
</html>
</s:if><s:else>
	<% response.sendRedirect("index.jsp"); 	%>
</s:else>

