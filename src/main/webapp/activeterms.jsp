<%@page contentType="text/html" pageEncoding="windows-1252"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Manage Active Terms</title>
		<style type="text/css">
			#termTable {
				width: auto;
			}
		</style>
    </head>
    <body>
        <%@include file="navbar.jsp" %>
		
		 <!-- Kick unauthorized user -->
        <%
            if (activeRole != Role.ADMINISTRATOR && activeRole != Role.COURSE_COORDINATOR) {
                request.setAttribute("error", "Oops. You are not authorized to access this page!");
                RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
                rd.forward(request, response);
            }
         %>
        <div class="container">
			<h3>Manage Active Terms</h3>
			<s:if test="%{allTerms != null && allTerms.size() > 0}">
			<table id="termTable" class="table table-hover">
				<thead>
					<tr>
					<th>Term Name</th>
					<th>Active</th>
					<th>Inactive</th>
				</thead>
				<tbody>
					<s:iterator value="allTerms">
					<tr>
						<td hidden><s:property value="id"/></td>
						<td><s:property value="displayName"/></td>
						<form>
						<s:if test="%{activeTerms.contains(id)}">
						<td><input type="radio" name="isActive" value="true" checked></td>
						<td><input type="radio" name="isActive" value="false"></td>
						</s:if><s:else>
						<td><input type="radio" name="isActive" value="true"></td>
						<td><input type="radio" name="isActive" value="false" checked></td>
						</s:else>
						</form>
					</tr>
					</s:iterator>
				</tbody>
			</table>
			</s:if>
        </div>
		<%@include file="footer.jsp"%>
		<script type="text/javascript">
			
		</script>
    </body>
</html>
