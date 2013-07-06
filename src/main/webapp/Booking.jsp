<%-- 
    Document   : Booking
    Created on : Jun 30, 2013, 5:45:00 PM
    Author     : Prakhar
--%>

<%@page contentType="text/html" pageEncoding="windows-1252"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Create Booking</title>
		<!--		<script type="text/javascript" src="js/plugins/jquery-ui/js/jquery-ui-1.10.3.custom.min.js"></script>
				<script type="text/javascript" src="js/plugins/jquery-2.0.2.js"></script>-->
    </head>
    <body>
        <!-- Navigation -->
        <%@include file="navbar.jsp" %>
        <div class="container page">
            <h2>Create Booking</h2>

			<!-- SECTION: Timeslot Table -->
			<s:if test="%{data.size() > 0}">
				<div>
					<h3>Time Slots in Chosen Milestone: </h3>
					<table class="table table-hover">
						<thead>
							<tr>
								<th>Date</th>
								<th>Start Time</th>
								<th>End Time</th>
								<th>Team</th>
							</tr>
						</thead>
						<tbody>
							<s:iterator value="data">
								<s:if test="%{teamName != null}">
									<tr class="error">	
								</s:if><s:else>
								<tr>	
								</s:else>
								
									<td><s:property value="date"/></td>
									<td><s:property value="startTime"/></td>
									<td><s:property value="endTime"/></td>
									<td><s:property value="teamName"/></td>
								</tr>
							</s:iterator>
						</tbody>
					</table>
				</div>
			</s:if>

			<!-- SECTION: Timeslot Table -->
			<div>
				<form action="createBooking" method="post">
					Date: <input type="text" class="input-medium" name="date" id="datepicker"/> &nbsp;
					Start Time:
					<input type="text" class="input-medium" name="startTime" id="timepicker"/> &nbsp;<br />
					<select name="termId">
						<option value="2013,1">2013-14 Term 1</option>
						<option value="2013,2">2013-14 Term 2</option>
					</select> &nbsp;
					<select name="milestone">
						<option value="acceptance">Acceptance</option>
						<option value="midterm">Midterm</option>
						<option value="final">Final</option>
					</select> <br /> <br />
					<input type="submit" class="btn btn-primary" value="Create"/>
				</form>
			</div>
        </div>

    </body>
</html>
