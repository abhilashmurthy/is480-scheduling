<%@page contentType="text/html" pageEncoding="windows-1252"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
    <head>
        <%@include file="header.jsp" %>
        <title>IS480 Scheduling System | Terms </title>
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
			<div style="float: left;">
			</div>
			<div style="clear: both;">
			<button id="submitFormBtn" class="btn btn-primary" data-loading-text="Saving..." style="margin-bottom: 20px;">Save</button>
			<!-- SECTION: Response Banner -->
<!--			<div id="responseBanner" class="alert fade in" hidden>
				<span id="responseMessage" style="font-weight: bold"></span>
			</div>-->
			</div>
			</s:if><s:else>
				<h4>No Terms Exist!</h4>
			</s:else>
        </div>
		
		<%@include file="footer.jsp"%>
		<script type="text/javascript">
			loadActiveTerms = function() {
				//Method to update the dropdown list based on the radio buttons selected
				$(":radio").click(function(){
					var tr = $(this).parents("tr");
					var termId = $(tr).children(":hidden").text();
					
					//Checking if the term was set as Active or Inactive
					if ($(this).val() === "true") {
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
				
				//Submit changes to backend
				$('#submitFormBtn').click(function() {
					$(this).button('loading');
					var activeTermData = new Object();
					activeTermData["activeTerms"] = generateArray($(":radio:checked[value='true']"));
					
					if (activeTermData.activeTerms.length === 0) {
						showNotification("WARNING", "Please select atleast one active term!");
						$("#submitFormBtn").button('reset');
						return false;
					}
					
					$.ajax({
						type: 'POST',
						async: false,
						url: 'updateActiveTerms',
						data: {jsonData: JSON.stringify(activeTermData)}	
					}).done(function(response) {
						$("#submitFormBtn").button('reset');
						console.log(response);
//						$("#responseBanner").show().delay(2000).fadeOut(400);
						if (response.success) {
//							$("#responseBanner").removeClass("alert-error").addClass("alert-success");
//							$("#responseMessage").text(response.message);
							showNotification("SUCCESS", response.message);
						} else {
//							$("#responseBanner").removeClass("alert-success").addClass("alert-error");
//							$("#responseMessage").text(response.message);
							showNotification("ERROR", response.message);
						}
					}).fail(function(response) {
						$("#submitFormBtn").button('reset');
						console.log(response);
//						$("#responseBanner").show().delay(2000).fadeOut(400);
//						$("#responseBanner").removeClass("alert-success").addClass("alert-error");
//						$("#responseMessage").text("Oops. Something went wrong. Please try again!");
						showNotification("WARNING", "Oops. Something went wrong. Please try again!");
					});
					return false;
				});
				
				//Generate array of active term IDs
				function generateArray(list) {
					var arr = new Array();
					for (var i = 0; i < list.length; i++) {
						var tr = $(list[i]).parents("tr");
						arr.push(parseInt($(tr).children(":hidden").text()));
					}
					return arr;
				}
				
				//Notification-------------
				function showNotification(action, notificationMessage) {
					var opts = {
						title: "Note",
						text: notificationMessage,
						type: "warning",
						icon: false,
						sticker: false,
						mouse_reset: false,
						animation: "fade",
						animate_speed: "fast",
						before_open: function(pnotify) {
							pnotify.css({
							   top: "52px",
							   left: ($(window).width() / 2) - (pnotify.width() / 2)
							});
						}
					};
					switch (action) {
						case "SUCCESS":
							opts.title = "Updated";
							opts.type = "success";
							break;
						case "ERROR":
							opts.title = "Error";
							opts.type = "error";
							break;
						case "INFO":
							opts.title = "Error";
							opts.type = "info";
							break;
						case "WARNING":
							$.pnotify_remove_all();
							opts.title = "Note";
							opts.type = "warning";
							break;
						default:
							alert("Something went wrong");
					}
					$.pnotify(opts);
					return false;
				}
			};
			
			
			addLoadEvent(loadActiveTerms);
		</script>
    </body>
</html>
