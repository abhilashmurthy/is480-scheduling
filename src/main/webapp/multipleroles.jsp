<%-- 
    Document   : multipleroles
    Created on : Jul 23, 2013, 1:43:31 PM
    Author     : Prakhar
--%>

<%@page contentType="text/html" pageEncoding="windows-1252"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=windows-1252">
        <title>Welcome | Roles</title>
		<%@include file="footer.jsp"%>
    </head>
    <body>
        <%@include file="navbar.jsp" %>
        <div class="container">
			<h3>Choose your Role</h3>
			<div style="text-align:center" align="middle">
				<table align="center">
					<tr>
						<td width="30%">
							<a href="index.jsp?role=ar"><img src="img/administrator.jpg" class="img-polaroid" title="Course Coordinator" height="200" width="150"/></a>
						</td>
						<td width="40%">
							<a href="index.jsp?role=sr"><img src="img/supervisor.jpg" class="img-polaroid" title="Supervisor" height="210" width="150">
						</td>
						<td width="48%">
							<a href="index.jsp?role=rr"><img src="img/reviewer.jpg" class="img-polaroid" title="Reviewer" height="220" width="170">
						</td>
					</tr>
					<tr>
						<td style="display:none"><h4>Course Coordinator</h4></td>
						<td><h4>Supervisor</h4></td>
						<td><h4>Reviewer</h4></td>
					</tr>
				</table>
			</div>
		</div>
    </body>
</html>

