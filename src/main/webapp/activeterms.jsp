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
			<form id="myform" action="updateActiveTerms" method="post">
			<table id="termTable" class="table table-hover">
				<thead>
					<tr>
						<th>Term Name</th>
						<th>Active</th>
						<th>Inactive</th>
					</tr>
				</thead>
				<tbody>
					<s:iterator value="allTerms">
					<tr align="center">
						<td hidden><s:property value="id"/></td>
						<td style="text-align:center; vertical-align:middle;">
							<s:property value="displayName"/>
						</td>
						<s:if test="%{activeTerms.contains(id)}">
							<td style="text-align:center; vertical-align:middle;">
								<input type="radio" name="isActive" value="true" checked />
							</td>
							<td style="text-align:center; vertical-align:middle;">
								<input type="radio" name="isActive" value="false" />
							</td>
						</s:if><s:else>
							<td style="text-align:center; vertical-align:middle;">
								<input type="radio" name="isActive" value="true" />
							</td>
							<td style="text-align:center; vertical-align:middle;">
								<input type="radio" name="isActive" value="false" checked />
							</td>
						</s:else>
					</tr>
					</s:iterator>
				</tbody>
			</table>
			<br/>
			<table class="table table-hover" style="width:auto">
			<thead>
				<tr>
				<th>
					<b>Default Active Term</b>
				</th>
				</tr>
			</thead>
			<tbody>
				<tr>
				<td>
					<select name="defaultActiveTerm" id="defaultActiveTermList">
						<option value=""></option>
					</select>
				</td>
				</tr>
			</tbody>
			</table>
			<br/><br/><br/>
			
			<input type="submit" id="saveButton" class="btn btn-primary" name="Save" value="Save" style="width:100px; height:30px;" />
			
			</form>
			</s:if><s:else>
				<h4>No Terms Exist!</h4>
			</s:else>
        </div>
		
		<%@include file="footer.jsp"%>
		<script type="text/javascript">
			$(function(){
				var $btn = $('#saveButton');
				$btn.click(function(){
					var $this = $(this);
					$this.attr('disabled', 'disabled').html("Saving...");
					setTimeout(function () {
						$this.removeAttr('disabled').html('Save');
					}, 2000)
				});
			})
		</script>
    </body>
</html>
