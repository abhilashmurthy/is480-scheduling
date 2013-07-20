<%-- 
    Document   : AcceptReject
    Created on : Jul 2, 2013, 11:14:06 PM
    Author     : Tarlochan
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
        <title>Approve/Reject Booking</title>
		<script type="text/javascript">
			// Listen for click on toggle checkbox
			$('#select-all').click(function(event) {   
				if(this.checked) {
					// Iterate each checkbox
					$(':checkbox').each(function() {
						this.checked = true;                        
					});
				}
			});
		</script>
		
    </head>
    <body>
        <%@include file="navbar.jsp" %>
        <div class="container">
        <h3>Approve/Reject Booking</h3>
        <!--<form action="approveReject" method="post">
			<!-- Putting default values for testing purposes 
            Choose Term <select name="termId"> 
							<option value="1">1</option>
							<option value="2">2</option>
						</select> <br/>
            Enter User Id <input type="text" name="userId" value="6"/> <br/>
			<p style=text-indent:16.5em;>
			   <input type="submit" class="btn btn-primary" value="Search"/>
			</p>
        </form> -->
		
		<div id ="confirmBookings"> 
			<%-- Tab to view Accept/Reject Bookings --%>
			<ul id="approveBookingTab" class="nav nav-tabs">
				<li class="active">
					<a id="acceptReject" data-toggle="tab">Accept/Reject</a>
				</li>
			</ul>

			<!-- SECTION: Approve/Reject Bookings -->
			<s:if test="%{data.size() > 0 && data != null}"> 
				<%--<s:if test="%{teamName != null}"> --%>
					<table class="table table-hover">
						<thead>
							<tr>
								<%--<th>Team Id</th>--%>
								<th><input type="checkbox" name="select-all" id="select-all"/></th>
								<th>Team Name</th>
								<th>Presentation Type</th>
								<th>Start Time</th>
								<th>End Time</th>
								<th>Venue</th>
								<th>Your Status</th>
								<th>Overall Booking Status</th>
							</tr>
						</thead>
						<tbody> 
							<s:iterator value="data">
								<tr>	
									<%--<td><s:property value="teamId"/></td> --%>
									<td><input type="checkbox" name="approveRejectArray" value="<s:property value="timeslotId"/>"</td>
									<td><s:property value="teamName"/></td>
									<td><s:property value="milestoneName"/></td>
									<td><s:property value="startTime"/></td>
									<td><s:property value="endTime"/></td>
									<td><s:property value="venue"/></td>
									<td><s:property value="myStatus"/></td>
									<td><s:property value="overallStatus"/></td>
								</tr>
						</table>
								<!--<form action="slotupdated.jsp" method="post">-->
								<form action="updateBookingStatus" method="post">
									<table>
									<tr>
										<td><input type="submit" class="btn btn-primary" value="Approve" name="Approve"/></td>
										<td><input type="submit" class="btn btn-primary" value="Reject" name="Reject"/></td>
										<input type="hidden" name="approveRejectArray" id="approveRejectArray" value="approveRejectArray"/> 
									</tr>
									</table>
								</form>
							</s:iterator>
						</tbody>
					</table>
				<%-- </s:if><s:else>
					<h4>No pending bookings available for Approve/Reject!</h4> 
				</s:else> --%>
			</s:if><s:else>
				<h4>No pending bookings available for Approve/Reject!</h4>
			</s:else>
		</div>
		
		
        <script src="js/plugins/bootstrap.js" type="text/javascript"></script>
        <%-- <% String statuses = '<s:property value="message" />'; %> --%>
		
		<br/>
		
		
        <s:iterator value="message">
			<%-- <s:textfield name="message" value="%{[0].toString()}" /><br/>--%>
			<s:property value="teamName"/><br/>
            <form action="slotupdated.jsp" method="post">
                <input type="submit" class="btn btn-primary" value="Approve" name="Approve"/>
                <input type="submit" class="btn btn-primary" value="Reject" name="Reject"/>
                <input type="hidden" name="teamId" value="<s:property value="teamIdInt"/>" />
            </form>
        </s:iterator>

        </div>
    </body>
</html>
