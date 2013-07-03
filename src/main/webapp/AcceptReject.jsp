<%-- 
    Document   : AcceptReject
    Created on : Jul 2, 2013, 11:14:06 PM
    Author     : Tarlochan
--%>

<%@page import="com.opensymphony.xwork2.ActionContext"%>
<%@page import="com.opensymphony.xwork2.util.ValueStack"%>
<%@page import="model.dao.TimeslotStatusDAO"%>
<%@page import="model.dao.TimeslotDAO"%>
<%@page import="util.Milestone"%>
<%@page import="model.*"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page contentType="text/html" pageEncoding="windows-1252"%>
<%@page import="model.dao.ScheduleDAO"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Accept/Reject Booking</title>
		<!-- Bootstrap -->
		<link href="css/bootstrap.css" rel="stylesheet" media="screen">
		<style>
			body {
				padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */
				padding-left: 10px;
			}
		</style>
    </head>
	<body>
		<div class="navbar navbar-inverse navbar-fixed-top">
			<div class="navbar-inner">
				<div class="container">
					<button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
					</button>
					<a class="brand" href="Index.jsp">IS480 Scheduling</a>
					<div class="nav-collapse collapse">
						<ul class="nav">
							<li><a href="Index.jsp">Home</a></li>
                                                        <li><a href="Booking.jsp">Create Booking</a></li>
							<li class="active"><a href="AcceptReject.jsp">Accept/Reject Booking</a></li>
						</ul>
					</div><!--/.nav-collapse -->
				</div>
			</div>
		</div>
            
                
                
                <h2>Accept/Reject Booking</h2>
		<form action="acceptReject" method="post">
			Enter Term Id <input type="text" name="termId"/> <br/>
			Enter User Id <input type="text" name="userId"/> <br/>
			<input type="submit" class="btn btn-primary" value="Search"/>
		</form>
		<script src="js/bootstrap.js" type="text/javascript"></script>
              <%-- <% String statuses = '<s:property value="message" />'; %> --%>
              
                 <s:set var = "breakLoop" value = "%{false}" />
                 <s:iterator value="message">
                      <s:if test="!#breakLoop">
                           <h4> List of Teams to Approve: </h4>
                            <s:set var = "breakLoop" value = "%{true}"/>
                      </s:if>
                  </s:iterator>
              
              
                  <s:iterator value="message">
                      <form action="SlotUpdated.jsp" method="post">
                  <li><s:property /></li> <input type="submit" class="btn btn-primary" value="Approve" name="Approve"/>
                    <input type="submit" class="btn btn-primary" value="Reject" name="Reject"/>
                    <input type="hidden" name="teamId" value="<s:property />" />
                    </form>
                  </s:iterator>
                  
		
	</body>
</html>
