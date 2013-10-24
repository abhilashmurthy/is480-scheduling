<%-- 
    Document   : manageusers
    Created on : Oct 24, 2013, 4:49:25 PM
    Author     : Abhilash
--%>

<%@page import="model.Term"%>
<%@ taglib prefix="s" uri="/struts-tags" %>

<!DOCTYPE html>
<html>
    <head>
        <%@include file="header.jsp" %>
        <title>IS480 Scheduling System | Users </title>
    </head>
    <body>
        <%@include file="navbar.jsp" %>
        <div class="container">
            <h3>Users</h3>
            <!-- Kick unauthorized user -->
            <% if (!activeRole.equals(Role.ADMINISTRATOR) && !activeRole.equals(Role.COURSE_COORDINATOR)) {
                    request.setAttribute("error", "Oops. You are not authorized to access this page!");
                    RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
                    rd.forward(request, response);
			} %>
			
			<!-- TERM TABLE -->
			
			<table id="selectTermTable">
				<tr>
					<td class="formLabelTd">Select Term</td>
					<td>
						<select class="termPicker" name="termId" onchange="this.form.submit()">
							<option value='<%= ((Term)session.getAttribute("currentActiveTerm")).getId() %>'><%= ((Term)session.getAttribute("currentActiveTerm")).getDisplayName() %></option>
							<s:iterator value="termData">
								<option value="<s:property value="termId"/>"><s:property value="termName"/></option>
							</s:iterator>
						</select>
					</td>
				</tr>
			</table>
							
			<!-- OTHERS USERS AND TEAMS -->				
							
			<div class='termUsers'>
				<div class='users'>
					<ul class='usersNav nav nav-tabs' id='myTab'>
						<li class='students active'><a href='#students'><h4>Students</h4></a></li>
						<li class='faculty'><a href='#faculty'><h4>Faculty</h4></a></li>
						<li class='tas'><a href='#tas'><h4>TAs</h4></a></li>
					</ul>
					<div class='tab-content'>
						<div class='tab-pane active' id='students'>
							<!-- Students -->
							<table id='studentUsersTable' class='subUsersTable table table-hover zebra-striped'>
								<thead>
									<tr><th></th><th>Name</th><th>Username</th><th>Phone</th><th>Team</th><th>Edit</th><th>Delete</th></tr>
								</thead>
								<tbody>
									<s:if test="%{studentData != null && studentData.size() > 0}">
										<s:iterator value="studentData">
											<tr id='user_<s:property value="id"/>' class='studentRow'>
												<td><i class='icon-user icon-black'></i></td>
												<td><s:property value="name"/></td>
												<td><s:property value="username"/></td>
												<td><s:property value="mobileNumber"/></td>
												<td><s:property value="teamName"/></td>
												<td>
													<button type='button' title="Edit User" class='btn btn-info'>
														<i class='icon-pencil icon-white'></i>
													</button>
												</td>
												<td>
													<button type='button' title="Delete User" class='btn btn-danger'>
														<i class='icon-trash icon-white'></i>
													</button>
												</td>
											</tr>
										</s:iterator>
										<tr>
											<td></td>
											<td></td>
											<td></td>
											<td></td>
											<td></td>
											<td></td>
											<td><button type='button' id='addStudentBtn' class='btn btn-primary'><i class='icon-plus icon-white'></i></button></td>
										</tr>
									</s:if>
									<s:else>
										<tr><h3 class='noUsersMsg'>No students set!</h3></tr>
										<tr>
											<td></td>
											<td></td>
											<td></td>
											<td></td>
											<td></td>
											<td></td>
											<td><button type='button' id='addStudentBtn' class='btn btn-primary'><i class='icon-plus icon-white'></i></button></td>
										</tr>
								</s:else>
								</tbody>
							</table>
						</div>
						<div class='tab-pane' id='faculty'>
							<!-- Faculty -->
							<table id='facultyUsersTable' class='subUsersTable table table-hover zebra-striped'>
								<thead>
									<tr><th></th><th>Name</th><th>Username</th><th>Phone</th><th>Info</th><th>Edit</th><th>Delete</th></tr>
								</thead>
								<tbody>
									<s:if test="%{facultyData != null && facultyData.size() > 0}">
										<s:iterator var="faculty" value="facultyData">
											<tr id='user_<s:property value="id"/>' class='studentRow'>
												<td><i class='icon-user icon-black'></i></td>
												<td><s:property value="name"/></td>
												<td><s:property value="username"/></td>
												<td><s:property value="mobileNumber"/></td>
												<td>
													<button type='button' title="Info" class='btn'>
														<i class='icon-info-sign icon-white'></i>
													</button>
												</td>
												<td>
													<button type='button' title="Edit User" class='btn btn-info'>
														<i class='icon-pencil icon-white'></i>
													</button>
												</td>
												<td>
													<button type='button' title="Delete User" class='btn btn-danger'>
														<i class='icon-trash icon-white'></i>
													</button>
												</td>
											</tr>
										</s:iterator>
										<tr>
											<td></td>
											<td></td>
											<td></td>
											<td></td>
											<td></td>
											<td></td>
											<td><button type='button' id='addFacultyBtn' class='btn btn-primary'><i class='icon-plus icon-white'></i></button></td>
										</tr>
									</s:if>
									<s:else>
										<tr><h3 class='noUsersMsg'>No faculty set!</h3></tr>
										<tr>
											<td></td>
											<td></td>
											<td></td>
											<td></td>
											<td></td>
											<td></td>
											<td><button type='button' id='addFacultyBtn' class='btn btn-primary'><i class='icon-plus icon-white'></i></button></td>
										</tr>
								</s:else>
								</tbody>
							</table>
						</div>
						<div class='tab-pane' id='tas'>
							<!-- TAs -->
							<table id='taUsersTable' class='subUsersTable table table-hover zebra-striped'>
								<thead>
									<tr><th></th><th>Name</th><th>Username</th><th>Phone</th><th>Info</th><th>Edit</th><th>Delete</th></tr>
								</thead>
								<tbody>
									<s:if test="%{taData != null && taData.size() > 0}">
										<s:iterator var="ta" value="taData">
											<tr id='user_<s:property value="id"/>' class='studentRow'>
												<td><i class='icon-user icon-black'></i></td>
												<td><s:property value="name"/></td>
												<td><s:property value="username"/></td>
												<td><s:property value="mobileNumber"/></td>
												<td>
													<button type='button' title="Info" class='btn'>
														<i class='icon-info-sign icon-white'></i>
													</button>
												</td>
												<td>
													<button type='button' title="Edit User" class='btn btn-info'>
														<i class='icon-pencil icon-white'></i>
													</button>
												</td>
												<td>
													<button type='button' title="Delete User" class='btn btn-danger'>
														<i class='icon-trash icon-white'></i>
													</button>
												</td>
											</tr>
										</s:iterator>
										<tr>
											<td></td>
											<td></td>
											<td></td>
											<td></td>
											<td></td>
											<td></td>
											<td><button type='button' id='addTABtn' class='btn btn-primary'><i class='icon-plus icon-white'></i></button></td>
										</tr>
									</s:if>
									<s:else>
										<tr><h3 class='noUsersMsg'>No TA set!</h3></tr>
										<tr>
											<td></td>
											<td></td>
											<td></td>
											<td></td>
											<td></td>
											<td></td>
											<td><button type='button' id='addTABtn' class='btn btn-primary'><i class='icon-plus icon-white'></i></button></td>
										</tr>
								</s:else>
								</tbody>
							</table>
						</div>
					</div>
				</div>
				<div class='teams'>
					<h4>Teams</h4>
				</div>
			</div>
			
			<!-- COURSE COORDINATORS -->
			
			<h4 class='userTypeTitle'>Course Coordinator</h4>
			<table id='ccUsersTable' class='usersTable table table-hover zebra-striped'>
				<thead>
					<tr><th></th><th>Name</th><th>Username</th><th>Phone</th><th>Edit</th></tr>
				</thead>
				<tbody>
				<s:if test="%{ccData != null && ccData.size() > 0}">
					<s:iterator value="ccData">
					<tr id='user_<s:property value="id"/>' class='ccRow'>
						<td><i class='icon-user icon-black'></i></td>
						<td><s:property value="name"/></td>
						<td><s:property value="username"/></td>
						<td><s:property value="mobileNumber"/></td>
						<td>
							<button type='button' title="Edit User" class='btn btn-info'>
								<i class='icon-pencil icon-white'></i>
							</button>
						</td>
					</tr>
					</s:iterator>
				</s:if>
				<s:else>
					<tr><h3 class='noUsersMsg'>No course coordinators set!</h3></tr>
					<tr>
						<td></td>
						<td></td>
						<td></td>
						<td></td>
						<td><button type='button' id='addCcBtn' class='btn btn-primary'><i class='icon-plus icon-white'></i></button></td>
					</tr>
				</s:else>
				</tbody>
			</table>
			
			<!-- ADMINISTRATORS -->

			<h4 class='userTypeTitle'>Administrator</h4>
			<table id='adminUsersTable' class='usersTable table table-hover zebra-striped'>
				<thead>
					<tr><th></th><th>Name</th><th>Username</th><th>Phone</th><th>Edit</th><th>Delete</th></tr>
				</thead>
				<tbody>
				<s:if test="%{adminData != null && adminData.size() > 0}">
					<s:iterator value="adminData">
					<tr id='user_<s:property value="id"/>' class='adminRow'>
						<td><i class='icon-user icon-black'></i></td>
						<td><s:property value="name"/></td>
						<td><s:property value="username"/></td>
						<td><s:property value="mobileNumber"/></td>
						<td>
							<button type='button' title="Edit User" class='btn btn-info'>
								<i class='icon-pencil icon-white'></i>
							</button>
						</td>
						<td>
							<button type='button' title="Delete User" class='btn btn-danger'>
								<i class='icon-trash icon-white'></i>
							</button>
						</td>
					</tr>
					</s:iterator>
					<tr>
						<td></td>
						<td></td>
						<td></td>
						<td></td>
						<td></td>
						<td><button type='button' id='addAdminBtn' class='btn btn-primary'><i class='icon-plus icon-white'></i></button></td>
					</tr>
				</s:if>
				<s:else>
					<tr><h3 class='noUsersMsg'>No admins here!</h3></tr>
					<tr>
						<td></td>
						<td></td>
						<td></td>
						<td></td>
						<td><button type='button' id='addAdminBtn' class='btn btn-primary'><i class='icon-plus icon-white'></i></button></td>
					</tr>
				</s:else>
				</tbody>
			</table>
			
        </div>
		
		<%@include file="footer.jsp"%>
        <script type="text/javascript">
			milestoneConfigLoad = function() {
				var milestones = {};

				loadUsers();
				
				function loadUsers() {
					var adminData = JSON.parse('<s:property escape="false" value="adminJson"/>');
					var ccData = JSON.parse('<s:property escape="false" value="ccJson"/>');
					var teamData = JSON.parse('<s:property escape="false" value="teamJson"/>');
					var studentData = JSON.parse('<s:property escape="false" value="studentJson"/>');
					var facultyData = JSON.parse('<s:property escape="false" value="facultyJson"/>');
					var taData = JSON.parse('<s:property escape="false" value="taJson"/>');
					console.log('\n\nAdmin data: ' + JSON.stringify(adminData));
					console.log('\n\nCC data: ' + JSON.stringify(ccData));
					console.log('\n\nTeam data: ' + JSON.stringify(teamData));
					console.log('\n\nStudent data: ' + JSON.stringify(studentData));
					console.log('\n\nFaculty data: ' + JSON.stringify(facultyData));
					console.log('\n\nTA data: ' + JSON.stringify(taData));
				}
				
				 //Manual navigation because of struts URL
                 $(".usersNav li a").on('click', function(){
					 var href = $(this).attr('href').split('#')[1];
					 $(".tab-pane, .nav-tabs li").removeClass('active');
					 $(".tab-pane").hide();
					 $(".tab-pane #" + href).addClass('active');
					 $(".nav-tabs ." + href).addClass('active');
					 $("#" + href).show();
					 return false;
                 });
				
				function loadMilestones() {
					<s:iterator value="data">
						var milestoneName = "<s:property value="milestone"/>";
						milestoneName = milestoneName.replace(' ', '').toLowerCase();
						milestones[milestoneName] = {
							duration: "<s:property value="duration"/>",
							order: "<s:property value="order"/>",
							attendees: JSON.parse('<s:property escape="false" value="attendeesJson"/>')
						};
					</s:iterator>
//					console.log('JSON: ' + JSON.stringify(milestones));
				}
				
				//Delete milestone
				$('body').on('click', '.deleteMilestoneBtn', function(){
					var $milestoneTr = $(this).closest('tr');
					$milestoneTr.fadeOut('slow', function(){
						$milestoneTr.remove();
						resetPlugins();
					});
				});
				
				//Add milestone
				$("#addMilestoneBtn").on('click', function(){
					$(".noMilestoneMsg").fadeOut('slow', function(){
						$(this).remove();
					});
					$(document.createElement('tr'))
						.addClass('milestoneRow')
						.append(
							//OrderNum
							$(document.createElement('td'))
								.addClass('orderNum')
						)
						.append(
							//Milestone name
							$(document.createElement('td'))
								.append(
									$(document.createElement('input'))
										.attr('type', 'text')
										.addClass('milestoneNameInput')
										.css({'width': '90px', 'height':'20px'})
								)
						)
						.append(
							//Duration
							$(document.createElement('td'))
								.addClass('fuelux')
								.append(
									$(document.createElement('div'))
										.addClass('durationSpinnerInput spinner')
										.append(
											$(document.createElement('input'))
												.attr('type', 'text')
												.addClass('durationInput spinner-input')
												.css('cssText', 'width: 50px !important')
										)
										.append(
											$(document.createElement('div'))
												.addClass('spinner-buttons btn-group btn-group-vertical')
												.append(
													$(document.createElement('button'))
														.attr('type', 'button')
														.addClass('btn spinner-up')
														.append($(document.createElement('i')).addClass('icon-chevron-up'))
												)
												.append(
													$(document.createElement('button'))
														.attr('type', 'button')
														.addClass('btn spinner-down')
														.append($(document.createElement('i')).addClass('icon-chevron-down'))
												)
										)
								)
						)	
						.append(
							//Attendees
							$(document.createElement('td'))
								.append(
									$(document.createElement('select'))
										.addClass('attendeesMultiselect multiselect')
										.attr('multiple', 'multiple')
										.append(function() {
												var optionsArray = new Array();
												optionsArray.push($(document.createElement('option')).attr('value', 'supervisor').html('Supervisor'));
												optionsArray.push($(document.createElement('option')).attr('value', 'reviewer1').html('Reviewer1'));
												optionsArray.push($(document.createElement('option')).attr('value', 'reviewer2').html('Reviewer2'));
												return optionsArray;
											}
										)
								)
						)
						.append(
							//Delete Button
							$(document.createElement('td'))
								.append(
									$(document.createElement('button'))
										.attr('type', 'button')
										.attr('title', 'Delete Milestone')
										.addClass('deleteMilestoneBtn btn btn-danger')
										.append($(document.createElement('i')).addClass('icon-trash icon-white'))
								)
								.append($(document.createElement('i')).addClass('moveIcon icon-move icon-black'))
						)
						.appendTo('#milestoneConfigTable tbody');
						resetPlugins();
				});
				
				//Save milestones
				$('#saveMilestonesBtn').on('click', function(){
					$(".noMilestoneMsg").fadeOut('slow', function(){
						$(this).remove();
					});
					$('button').attr('disabled', true);
					var updatedMilestones = new Array();
					$('.milestoneRow').each(function(){
						var $this = $(this);
						updatedMilestones.push({
							newOrderNumber: $this.find('.orderNum').text().toString(),
							newMilestoneName: $this.find('.milestoneNameInput').val(),
							newDuration: $this.find('.durationSpinnerInput').spinner('value').toString(),
							newAttendees: $this.find('.attendeesMultiselect').next().find('.dropdown-toggle').attr('title').replace(' ', '')
						});
					});
					console.log('Submitting: ' + JSON.stringify(updatedMilestones));
					$.ajax({
						type: 'POST',
						async: false,
						url: 'updateMilestoneSettings',
						data: {jsonData: JSON.stringify(updatedMilestones)}
					})
					.done(function(response) {
						if (!response.exception) {
							if (response.success) {
								showNotification("SUCCESS", response.message);
							} else {
								showNotification("INFO", response.message);
							}
						} else {
							var eid = btoa(response.message);
							window.location = "error.jsp?eid=" + eid;
						}
					})
					.fail(function(response) {
						$("#saveButton").button('reset');
						$("#addRowBtn").button('reset');
						console.log("Updating Milestone settings AJAX FAIL");
						showNotification("WARNING", "Oops.. something went wrong");
					});
					$('button').attr('disabled', false);
					return false;
				});
				
				/** Notification **/
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
				}
				
				function resetPlugins() {
					//Attendees Multiselect
					$('.attendeesMultiselect').multiselect('rebuild');
					$('.attendeesMultiselect').each(function(){
						if ($(this).attr('id')) {
							var milestoneName = $(this).attr('id').split('_')[1].toLowerCase();
							var attendees = milestones[milestoneName].attendees;
							for (var i = 0; i < attendees.length; i++) {
								$(this).multiselect('select', attendees[i].toLowerCase().replace(' ', ''));
							}
						}
					});

					//Duration Spinner
					$('.spinner').each(function(){
						$(this).spinner({
							min: 30,
							max: 180,
							step: 30
						});
						if (!$(this).attr('id')) {
							$(this).spinner('value', 30);
							return true;
						}
						var milestoneName = $(this).attr('id').split('_')[1].toLowerCase();
						var duration = parseInt(milestones[milestoneName].duration);
						$(this).spinner('value', duration);
					});
					$('.spinner').on('focusout', function(){
						var value = $(this).spinner('value');
						if (value % 30 !== 0) {
							$(this).spinner('value', value - (value % 30) === 0?30:value - (value % 30));
						}
					});
					
					//Drag and Drop milestones
					$('tbody').find('td.orderNum').each(function(i){
						$(this).empty();
						$(this).append(i + 1);
					});
					
					$("#milestoneConfigTable tbody").sortable({
						helper: function(e, $tr) {
							var $originals = $tr.children();
							var $helper = $tr.clone();
							$helper.children().each(function(index) {
								$(this).width($originals.eq(index).width());
							});
							return $helper;
						},
						stop: function(e, ui) {
							ui.item.parent().find('td.orderNum').each(function(i){
								$(this).empty();
								$(this).append(i + 1);
							});
						}
					}).disableSelection();
					
					$("#milestoneConfigTable tbody").sortable('refresh');
				}
				
			};
			
			addLoadEvent(milestoneConfigLoad);
        </script>
    </body> 
</html>
