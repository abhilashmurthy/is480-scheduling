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
						<td>
							<s:property value="displayName"/>
						</td>
						<s:if test="%{activeTermIds.contains(id)}">
							<td>
								<input type="radio" name="isActive" value="true" checked />
							</td>
							<td>
								<input type="radio" name="isActive" value="false" />
							</td>
						</s:if><s:else>
							<td>
								<input type="radio" name="isActive" value="true" />
							</td>
							<td>
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
					<select id="defaultActiveTermList">
						<s:iterator value="activeTermObjects">
							<option value="<s:property value="id"/>" <s:if test="%{id == defaultTerm}">selected</s:if> >
								<s:property value="displayName"/>
							</option>
						</s:iterator>
					</select>
				</td>
				</tr>
			</tbody>
			</table>
			</div>
			<br />
			<button id="submitFormBtn" class="btn btn-primary" data-loading-text="Saving...">Save</button>
			</s:if><s:else>
				<h4>No Terms Exist!</h4>
			</s:else>
        </div>
		
		<%@include file="footer.jsp"%>
		<script type="text/javascript">
			$(function() {
				$(":radio").click(function(){
					var dropdown = $("#defaultActiveTermList");
					var selected = $(this);
					var tr = selected.parents("tr");
					var termId = $(tr).children(":hidden").text();
					
					//Checking if the term was set as Active or Inactive
					if ($(selected).val() === "true") {
						//Checking if the term is already in the list
						var options = $(dropdown).children();
						for (var i = 0; i < options.length; i++) {
							if ($(options[i]).val() === termId) { return; }
						}
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
				
				$('#submitFormBtn').click(function() {
					
				});
			});
		</script>
    </body>
</html>
