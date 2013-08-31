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
			<div style="float: left; margin-right: 50px;">
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
						<form>
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
						</form>
					</tr>
					</s:iterator>
				</tbody>
			</table>
			</div>
			<div>
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
					</select>
				</td>
				</tr>
			</tbody>
			</table>
			</div>
			<br />
			<input type="submit" id="saveButton" class="btn btn-primary" name="Save" value="Save" style="width:100px; height:30px;" />
			</s:if><s:else>
				<h4>No Terms Exist!</h4>
			</s:else>
        </div>
		
		<%@include file="footer.jsp"%>
		<script type="text/javascript">
			$(function() {
				$('#saveButton').click(function() {
					var $this = $(this);
					$this.attr('disabled', 'disabled').html("Saving...");
					setTimeout(function() {
						$this.removeAttr('disabled').html('Save');
					}, 2000);
				});
				$(":radio").click(function(){
					var dropdown = $("#defaultActiveTermList");
					var selected = $(this);
					var tr = selected.parents("tr");
					var termId = $(tr).children(":hidden").text();
					
					//Checking if the term was set as Active or Inactive
					if ($(selected).val() === "true") {
						var newOption = document.createElement("option");
						var termName = $($(tr).children()[2]).text();
						$(newOption).val(termId);
						$(newOption).text(termName);
						$(dropdown).append(newOption);
					} else {
						//Remove the term from the dropdown list
						var options = $(dropdown).children();
						for (var i = 0; i < options.length; i++) {
							if ($(options[i]).val() === termId) {
								$(dropdown).find("option[value=" + termId + "]").remove();
								return;
							}
						}	
					}
				});
			});
		</script>
    </body>
</html>
