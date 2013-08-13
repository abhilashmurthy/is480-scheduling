<%--
    Document   : ApproveReject
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
        <title>Approve Booking</title>
		<%@include file="footer.jsp"%>
		<script type="text/javascript">
		//To check/uncheck all boxes
		checked = false;
		function checkedAll () {
		  if (checked == false) {
			  checked = true
		  } else {
			  checked = false
		  }
		  for (var i = 0; i < document.getElementById('myform').elements.length; i++) {
			document.getElementById('myform').elements[i].checked = checked;
		  }
		}
		
		//To validate the form (Make sure all checkboxes have been checked
		function valthisform() {
			var checkboxs=document.getElementsByName("approveRejectArray");
			var okay = false;
			for(var i = 0, l = checkboxs.length; i<l; i++) {
				if(checkboxs[i].checked) {
					okay = true;
				}
			}
			if(!okay) {
				alert("Please choose a timeslot!");
				return false;
			}
			return true;
		}
		
		jQuery(document).ready(function(){
			if(document.getElementById('approveRejectArray') === null) {
				 document.getElementById("approveButton").style.visibility = "hidden";
				 document.getElementById("rejectButton").style.visibility = "hidden";
			} else {
				document.getElementById("approveButton").style.visibility = "visible";
				document.getElementById("rejectButton").style.visibility = "visible";
			}
		});
		//To show/hide buttons if checkbox exists or not
		</script>
		
    </head>
    <body>
        <%@include file="navbar.jsp" %>
		
		 <!-- Kick unauthorized user -->
        <%
            if (!activeRole.equals("Supervisor/Reviewer")) {
                request.setAttribute("error", "Oops. You are not authorized to access this page!");
                RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
                rd.forward(request, response);
            }
         %>
		 
        <div class="container">
        <h3>Approve Booking</h3>
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
			<!-- SECTION: Approve/Reject Bookings -->
			<s:if test="%{data.size() > 0 && data != null}"> 
				<%--<s:if test="%{teamName != null}"> --%>
				<form id="myform" action="updateBookingStatus" method="post">
					<table class="table table-hover">
						<thead>
							<tr>
								<%--<th>Team Id</th>--%>
								<th><input type="checkbox" name="checkall" onclick="checkedAll();"></th>
								<th>Team Name</th>
								<th>Presentation</th>
								<th>My Role</th>
								<th>Date</th>
								<th>Time</th>
								<th>Venue</th>
								<th>My Status</th>
							</tr>
						</thead>
						<tbody> 
							<s:iterator value="data">
								<s:if test="%{myStatus.equalsIgnoreCase('Pending')}"> 
									<tr class="warning">
								</s:if><s:elseif test="%{myStatus.equalsIgnoreCase('Accepted')}">
									<tr class="success">
								</s:elseif><s:elseif test="%{myStatus.equalsIgnoreCase('Rejected')}">
									<tr class="error">
								</s:elseif>
									<%--<td><s:property value="teamId"/></td> --%>
									<s:if test='myStatus.equals("PENDING")'>
										<td><input type="checkbox" id="approveRejectArray" name="approveRejectArray" value="<s:property value="timeslotId"/>"/></td>
									</s:if><s:else>
										<td><i class="icon-ok"></i></td>
									</s:else>
									<td><s:property value="teamName"/></td>
									<td><s:property value="milestone"/></td>
									<td><s:property value="userRole"/></td>
									<td><s:property value="date"/></td>
									<td><s:property value="time"/></td>
									<td><s:property value="venue"/></td>
									<td><s:property value="myStatus"/></td>
								</tr>
							</s:iterator>
							</tbody>
						</table>
						<table>
							<tr>
								<td><input type="submit" class="btn btn-success" id="approveButton" value="Approve" name="Approve" onclick="return valthisform();"/></td>
								<td>
									<span class="button-divider">
										<input type="submit" class="btn btn-danger" id="rejectButton" value="Reject" name="Reject" onclick="return valthisform();"/>
									</span>
								</td>
								<!--<td><input type="hidden" name="approveRejectArray" id="approveRejectArray" value="approveRejectArray" /></td> -->
							</tr>
						</table>
					</form>
			</s:if><s:else>
				<h4>No bookings available!</h4>
			</s:else>
		</div>		
		
        <script src="js/plugins/bootstrap.js" type="text/javascript"></script>
        <%-- <% String statuses = '<s:property value="message" />'; %> --%>
		
		<br/>
        </div>
    </body>
</html>
