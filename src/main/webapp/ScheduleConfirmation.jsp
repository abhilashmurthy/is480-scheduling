<%-- 
    Document   : Schedule
    Created on : Jul 15, 2013, 12:37:07 AM
    Author     : Prakhar
--%>

<%@page contentType="text/html" pageEncoding="windows-1252"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Confirm Schedule</title>
    </head>
    <body>
		
        <!-- Navigation -->
        <%@include file="navbar.jsp" %>
        <div class="container page">
            <h2>Confirm Schedule</h2> <br/>
				<s:if test="%{dataList.size() > 0}">
					<s:iterator value="dataList">
						<table class="table table-hover">
							<thead>
							<tr>	
								<th><b>Milestone</b></th>
								<th><b>Start Date</b></th>
								<th><b>End Date</b></th>
							</tr>
							</thead>
							<tr>
								<td><b><s:property value="milestoneName"/></b></td>
								<td><s:property value="startDate"/></td>
								<td><s:property value="endDate"/></td>
								<form action="timeslots.jsp" method="post">
									<td><input type="submit" class="btn btn-primary" name="Manage Timeslots" value="Manage Timeslots"/></td>
									<input type="hidden" name="startDate" id="startDate" value="<s:property value="startDate"/>"/>
									<input type="hidden" name="endDate" id="endDate" value="<s:property value="endDate"/>"/>
									<input type="hidden" name="slotDuration" id="slotDuration" value="<s:property value="slotDuration"/>"/>
									<input type="hidden" name="scheduleId" id="scheduleId" value="<s:property value="scheduleId"/>"/>
								</form>
							</tr>
						</table>
					</s:iterator>
				</s:if><s:else>
					<h4>No schedule created!
				</s:else>
		</div>
	</body>
</html>
