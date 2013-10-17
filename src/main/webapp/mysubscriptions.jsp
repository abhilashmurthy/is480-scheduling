<%-- 
    Document   : mysubscriptions
    Created on : Oct 17, 2013, 6:15:21 PM
    Author     : Prakhar
--%>

<%@page import="com.opensymphony.xwork2.ActionContext"%>
<%@page import="com.opensymphony.xwork2.util.ValueStack"%>
<%@page import="model.*"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page contentType="text/html" pageEncoding="windows-1252"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
    <head>
        <%@include file="header.jsp" %>
        <title>IS480 Scheduling System | My Subscriptions</title>
    </head>
    <body>
        <%@include file="navbar.jsp" %>
		
		 <!-- Kick unauthorized user -->
        <%
            if (!activeRole.equals(Role.STUDENT)) {
                request.setAttribute("error", "Oops. You are not authorized to access this page!");
                RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
                rd.forward(request, response);
            }
         %>
		 
        <div class="container">
			<h3 style="float: left; margin-right: 50px;">My Subscriptions</h3>
			<s:if test="%{data != null && data.size() > 0}"> 
					<table id="mySubscriptionsTable" class="table table-hover" style="font-size: 13px;">
						<thead>
							<tr>
								<th>Team Presenting</th>
								<th>Date of Presentation</th>
								<th>Venue</th>
								<th>More Information</th>
							</tr>
						</thead>
						<tbody> 
							<s:iterator value="data">
								<s:if test="%{myStatus.equalsIgnoreCase('Pending')}"> 
									<tr class="warning">
								</s:if><s:elseif test="%{myStatus.equalsIgnoreCase('Approved')}">
									<tr class="success">
								</s:elseif><s:elseif test="%{myStatus.equalsIgnoreCase('Rejected')}">
									<tr class="error">
								</s:elseif>
									<td><s:property value="teamName"/></td>
									<td><s:property value="date"/> <s:property value="time"/></td>
									<td><s:property value="venue"/></td>
									<td><s:property value="wikiLink"/></td>
								</tr>
							</s:iterator>
							</tbody>
						</table>
						<br/><br/>
				</div>
			</s:if><s:else>
				<h4>You haven't subscribed to any presentations yet!</h4>
			</s:else>
		 
		<%@include file="footer.jsp"%>
		
		<script type='text/javascript'>
			mySubscriptionsLoad = function() {
				var activeBtn = null;
				
					$('#mySubscriptionsTable').dataTable({
//						"aLengthMenu": [
//							[5, 10, 20, -1],[5, 10, 20, "All"]], 
//						"iDisplayLength" : -1,
		//				"bPaginate": false,
		//				"bLengthChange": false,
		//				"bFilter": false,
		//				"bSort": false,
						"bInfo": false,
		//				"bAutoWidth": false,
		//				"asStripClasses": null,
						//To prevent highlighing of sorted column
						"bSortClasses": false
					});

					$('.dataTables_filter input').attr("placeholder", "e.g. SIS SR 2.1");
					$('.dataTables_filter input').attr("title", "Search any keyword in the table below");
					$('.dataTables_filter input').on('mouseenter', function(){
						$(this).tooltip('show');
					});
			};
			addLoadEvent(mySubscriptionsLoad);
		</script>
    </body>
</html>
