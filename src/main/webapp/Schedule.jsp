<%-- 
    Document   : Schedule
    Created on : Jul 10, 2013, 10:53:07 PM
    Author     : Prakhar
--%>

<%@page contentType="text/html" pageEncoding="windows-1252"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Schedule</title>
        <!--<script type="text/javascript" src="js/app/pages/createbooking.js">
			$('.datepick').each(function(){
		    $(this).datepicker();
			});
		</script>-->
		<!--<script type="text/javascript" src="js/plugins/jquery-ui/js/datepicker.js"></script>-->
    </head>
    <body>
         <!-- Navigation -->
        <%@include file="navbar.jsp" %>
        <div class="container page">
            <h3>Create Schedule</h3> <br/>
			<form action="createSchedule" method="post">
				<b> Choose Term </b>&nbsp&nbsp;<select name="term" > 
								<option value="2013,1">2013-2014 Term 1</option>
								<option value="2013,2">2013-2014 Term 2</option>
							</select> <br/>
				<b>Milestone 1: Acceptance</b> &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp;
					Start Date <input type="text" class="input-medium datepicker" name="acceptanceStartDate"/> 
   &nbsp&nbsp&nbsp&nbsp; End Date <input type="text" class="input-medium datepicker" name="acceptanceEndDate"/><br/>
				<b>Milestone 2: Midterm</b>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp;
					Start Date <input type="text" class="input-medium datepicker" name="midtermStartDate"/> 
   &nbsp&nbsp&nbsp&nbsp; End Date <input type="text" class="input-medium datepicker" name="midtermEndDate"/><br/>
				<b>Milestone 3: Final</b> &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
										  &nbsp&nbsp&nbsp&nbsp;
					Start Date <input type="text" class="input-medium datepicker" name="finalStartDate"/> 
   &nbsp&nbsp&nbsp&nbsp; End Date <input type="text" class="input-medium datepicker" name="finalEndDate"/><br/>
				<input type="submit" class="btn btn-primary" value="Create"/>
			</form>
		</div>
    </body>
</html>