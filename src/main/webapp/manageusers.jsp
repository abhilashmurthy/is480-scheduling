<%-- 
  Document  : manageusers
  Created on : Oct 24, 2013, 4:49:25 PM
  Author   : Abhilash
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
		<div class="container usersPage">
			<!-- Kick unauthorized user -->
			<% if (!activeRole.equals(Role.ADMINISTRATOR) && !activeRole.equals(Role.COURSE_COORDINATOR)) {
					request.setAttribute("error", "Oops. You are not authorized to access this page!");
					RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
					rd.forward(request, response);
				}%>		
			<!-- USERS AND TEAMS -->

			<section id='users'>
				<h3>Users</h3>
				<table class="selectTermTable">
					<tr>
						<td class="formLabelTd">Select Term</td>
						<td>
							<select class="termPicker" name="termId" onchange="this.form.submit()">
								<option value='<%= ((Term) session.getAttribute("currentActiveTerm")).getId()%>'><%= ((Term) session.getAttribute("currentActiveTerm")).getDisplayName()%></option>
								<s:iterator value="termData">
									<option value="<s:property value="termId"/>"><s:property value="termName"/></option>
								</s:iterator>
							</select>
						</td>
					</tr>
				</table>
				<button type='button' id='add_team' class='addBtn pull-right btn btn-primary'>
					<i class='fa fa fa-plus fa-white'></i> Add Team
				</button>
				<button type='button' title="Edit Course Coordinator" class='editAdminBtn pull-right editBtn btn btn-info' style='display:none'>
					<i class='fa fa fa-pencil fa-white'></i> Edit Course Coordinator
				</button>
				<ul class='usersNav nav nav-tabs' id='myTab'>
					<li class='teams active'><a href='#teams'><h4>Teams</h4></a></li>
					<li class='students'><a href='#students'><h4>Students</h4></a></li>
					<li class='faculty'><a href='#faculty'><h4>Faculty</h4></a></li>
					<li class='tas'><a href='#tas'><h4>TAs</h4></a></li>
					<li class='admins'><a href='#admins'><h4>Administrators</h4></a></li>
					<li class='cc'><a href='#cc'><h4>Course Coordinator</h4></a></li>
				</ul>
				<div class='tab-content'>
					<div class='tab-pane active' id='teams'>
						<!-- Teams -->
						<table id='teamsTable' class='subUsersTable table zebra-striped'>
							<thead>
								<tr><th></th><th>Name</th><th>Members</th><th>Supervisor</th><th>Reviewer 1</th><th>Reviewer 2</th><th>Edit</th><th>Delete</th></tr>
							</thead>
							<tbody>
								<s:if test="%{teamData != null && teamData.size() > 0}">
									<s:iterator var="team" value="teamData">
										<tr id='team_<s:property value="id"/>' class='teamRow'>
											<td><i class='fa fa fa-group fa-black'></i></td>
											<td class='teamName'><s:property value="teamName"/></td>
											<td class='members'>
												<div class='memberList'>
													<s:iterator var="member" value="members">
														<span id='member_<s:property value="#member.id"/>' class='memberName'>
															<a class='teamStudentLink' id='teamStudent_<s:property value="#member.id"/>' href='member_<s:property value="#member.username"/>'><s:property value="#member.name"/></a>
														</span>
													</s:iterator>
												</div>
											</td>
											<td class='supervisor'><a class='teamFacultyLink teamSupervisor_<s:property value="#team.supervisor.id"/>' href='supervisor_<s:property value="#team.teamName"/>'><s:property value="supervisor.name"/></a></td>
											<td class='reviewer1'><a class='teamFacultyLink teamReviewer1_<s:property value="#team.reviewer1.id"/>' href='reviewer1_<s:property value="#team.teamName"/>'><s:property value="reviewer1.name"/></a></td>
											<td class='reviewer2'><a class='teamFacultyLink teamReviewer2_<s:property value="#team.reviewer2.id"/>' href='reviewer2_<s:property value="#team.teamName"/>'><s:property value="reviewer2.name"/></a></td>
											<td>
												<button type='button' title="Edit" class='modBtn editBtn btn btn-info'>
													<i class='fa fa fa-pencil fa-white'></i>
												</button>
											</td>
											<td>
												<button type='button' title="Delete User" class='modBtn delBtn btn btn-danger'>
													<i class='fa fa fa-trash-o fa-white'></i>
												</button>
											</td>
										</tr>
									</s:iterator>
								</s:if>
								<s:else>
									<tr><h3 class='noUsersMsg'>No Team set!</h3></tr>
								</s:else>
							</tbody>
						</table>
					</div>
					<div class='tab-pane' id='students'>
						<!-- Students -->
						<table id='studentUsersTable' class='subUsersTable table zebra-striped'>
							<thead>
								<tr><th></th><th>Name</th><th>Username</th><th>Phone</th><th>Team</th><th>Edit</th><th>Delete</th></tr>
							</thead>
							<tbody>
								<s:if test="%{studentData != null && studentData.size() > 0}">
									<s:iterator var="student" value="studentData">
										<tr id='user_<s:property value="id"/>' class='studentRow'>
											<td><i class='fa fa fa-user fa-black'></i></td>
											<td class='fullName'><s:property value="name"/></td>
											<td class='username'><s:property value="username"/></td>
											<td class='mobileNumber'><s:property value="mobileNumber"/></td>
											<s:if test="%{#student.teamName!=null}">
												<td class='teamName'><a class='studentTeamLink' id='teams_<s:property value="teamId"/>' href='team_<s:property value="teamName"/>'><s:property value="teamName"/></a></td>
												</s:if>
												<s:else>
												<td class='teamName'><a class='assignTeamsLink' id='assignTeams_<s:property value="id"/>' href='assignTeams_<s:property value="id"/>'>Assign Team</a></td>
												</s:else>
											<td class='editTd'>
												<button type='button' title="Edit" class='modBtn editBtn btn btn-info'>
													<i class='fa fa fa-pencil fa-white'></i>
												</button>
											</td>
											<td class='deleteTd'>
												<button type='button' title="Delete User" class='modBtn delBtn btn btn-danger'>
													<i class='fa fa fa-trash-o fa-white'></i>
												</button>
											</td>
										</tr>
									</s:iterator>
								</s:if>
								<s:else>
									<tr><h3 class='noUsersMsg'>No students set!</h3></tr>
								</s:else>
							</tbody>
						</table>
					</div>
					<div class='tab-pane' id='faculty'>
						<!-- Faculty -->
						<table id='facultyUsersTable' class='subUsersTable table zebra-striped'>
							<thead>
								<tr><th></th><th>Name</th><th>Username</th><th>Phone</th><th>Supervisor</th><th>Reviewer 1</th><th>Reviewer 2</th><th>Edit</th><th>Delete</th></tr>
							</thead>
							<tbody>
								<s:if test="%{facultyData != null && facultyData.size() > 0}">
									<s:iterator var="faculty" value="facultyData">
										<tr id='user_<s:property value="id"/>' class='facultyRow'>
											<td><i class='fa fa-briefcase fa-black'></i></td>
											<td class='fullName'><s:property value="name"/></td>
											<td class='username'><s:property value="username"/></td>
											<td class='mobileNumber'><s:property value="mobileNumber"/></td>
											<td class='supervisorMyTeams'>
												<s:if test="%{#faculty.supervisorTeams.size() > 0}">
													<div class='memberList'>
														<s:iterator var="supervisorTeam" value="supervisorTeams">
															<span id='supervisorMyTeam_<s:property value="#supervisorTeam.teamId"/>' class='memberName'>
																<a class='studentTeamLink' id='supervisorMyTeam_<s:property value="#supervisorTeam.teamId"/>' href='team_<s:property value="#supervisorTeam.teamId"/>'><s:property value="#supervisorTeam.teamName"/></a>
															</span>
														</s:iterator>
													</div>
												</s:if>
												<s:else>
													<a class='assignTeamsLink' id='assignTeams_<s:property value="username"/>' href='assignTeams_<s:property value="username"/>'>Assign Team</a>
												</s:else>
											</td>
											<td class='reviewer1MyTeams'>
												<s:if test="%{#faculty.reviewer1Teams.size() > 0}">
													<div class='memberList'>
														<s:iterator var="reviewer1Team" value="reviewer1Teams">
															<span id='reviewer1Team_<s:property value="#reviewer1Team.teamId"/>' class='memberName'>
																<a class='studentTeamLink' id='reviewer1MyTeam_<s:property value="#reviewer1Team.teamId"/>' href='team_<s:property value="#reviewer1Team.teamId"/>'><s:property value="#reviewer1Team.teamName"/></a>
															</span>
														</s:iterator>
													</div>
												</s:if>
												<s:else>
													<a class='assignTeamsLink' id='assignTeams_<s:property value="username"/>' href='assignTeams_<s:property value="username"/>'>Assign Team</a>
												</s:else>
											</td>
											<td class='reviewer2MyTeams'>
												<s:if test="%{#faculty.reviewer2Teams.size() > 0}">
													<div class='memberList'>
														<s:iterator var="reviewer2Team" value="reviewer2Teams">
															<span id='reviewer2Team_<s:property value="#reviewer2Team.teamId"/>' class='memberName'>
																<a class='studentTeamLink' id='reviewer2MyTeam_<s:property value="#reviewer2Team.teamId"/>' href='team_<s:property value="#reviewer2Team.teamId"/>'><s:property value="#reviewer2Team.teamName"/></a>
															</span>
														</s:iterator>
													</div>
												</s:if>
												<s:else>
													<a class='assignTeamsLink' id='assignTeams_<s:property value="username"/>' href='assignTeams_<s:property value="username"/>'>Assign Team</a>
												</s:else>
											</td>
											<td>
												<button type='button' title="Edit" class='modBtn editBtn btn btn-info'>
													<i class='fa fa fa-pencil fa-white'></i>
												</button>
											</td>
											<td>
												<button type='button' title="Delete User" class='modBtn delBtn btn btn-danger'>
													<i class='fa fa fa-trash-o fa-white'></i>
												</button>
											</td>
										</tr>
									</s:iterator>
								</s:if>
								<s:else>
									<tr><h3 class='noUsersMsg'>No faculty set!</h3></tr>
								</s:else>
							</tbody>
						</table>
					</div>
					<div class='tab-pane' id='tas'>
						<!-- TAs -->
						<table id='taUsersTable' class='subUsersTable table zebra-striped'>
							<thead>
								<tr><th></th><th>Name</th><th>Username</th><th>Phone</th><th>Signups</th><th>Edit</th><th>Delete</th></tr>
							</thead>
							<tbody>
								<s:if test="%{taData != null && taData.size() > 0}">
									<s:iterator var="ta" value="taData">
										<tr id='user_<s:property value="id"/>' class='taRow'>
											<td><i class='fa fa-video-camera fa-black'></i></td>
											<td class='fullName'><s:property value="name"/></td>
											<td class='username'><s:property value="username"/></td>
											<td class='mobileNumber'><s:property value="mobileNumber"/></td>
											<td>
												<a class='taSignupsLink' id='signups_<s:property value="username"/>' href='ta_<s:property value="username"/>'><s:property value="%{#ta.mySignups.size()}"/></a>
											</td>
											<td>
												<button type='button' title="Edit" class='modBtn editBtn btn btn-info'>
													<i class='fa fa fa-pencil fa-white'></i>
												</button>
											</td>
											<td>
												<button type='button' title="Delete User" class='modBtn delBtn btn btn-danger'>
													<i class='fa fa fa-trash-o fa-white'></i>
												</button>
											</td>
										</tr>
									</s:iterator>
								</s:if>
								<s:else>
									<tr><h3 class='noUsersMsg'>No TA set!</h3></tr>
								</s:else>
							</tbody>
						</table>
					</div>
					<div class='tab-pane' id='admins'>
						<!-- Admins -->
						<table id='adminUsersTable' class='subUsersTable table zebra-striped'>
							<thead>
								<tr><th></th><th>Name</th><th>Username</th><th>Phone</th><th>Edit</th><th>Delete</th></tr>
							</thead>
							<tbody>
								<s:if test="%{adminData != null && adminData.size() > 0}">
									<s:iterator var="admin" value="adminData">
										<tr id='user_<s:property value="id"/>' class='adminRow'>
											<td><i class='fa fa fa-eye fa-black'></i></td>
											<td class='fullName'><s:property value="name"/></td>
											<td class='username'><s:property value="username"/></td>
											<td class='mobileNumber'><s:property value="mobileNumber"/></td>
											<td>
												<button type='button' title="Edit" class='modBtn editBtn btn btn-info'>
													<i class='fa fa fa-pencil fa-white'></i>
												</button>
											</td>
											<td>
												<button type='button' title="Delete User" class='modBtn delBtn btn btn-danger'>
													<i class='fa fa fa-trash-o fa-white'></i>
												</button>
											</td>
										</tr>
									</s:iterator>
								</s:if>
								<s:else>
									<tr><h3 class='noUsersMsg'>No Admin set!</h3></tr>
								</s:else>
							</tbody>
						</table>
					</div>
					<div class='tab-pane' id='cc'>
						<!-- Course Coordinator -->
						<table id='ccUsersTable' class='subUsersTable table zebra-striped'>
							<thead>
								<tr><th></th><th>Name</th><th>Username</th><th>Phone</th></tr>
							</thead>
							<tbody>
								<s:if test="%{ccData != null && ccData.size() > 0}">
									<s:iterator var="cc" value="ccData">
										<tr id='user_<s:property value="id"/>' class='ccRow'>
											<td><i class='fa fa fa-coffee fa-black'></i></td>
											<td class='fullName'><s:property value="name"/></td>
											<td class='username'><s:property value="username"/></td>
											<td class='mobileNumber'><s:property value="mobileNumber"/></td>
										</tr>
									</s:iterator>
								</s:if>
								<s:else>
									<tr><h3 class='noUsersMsg'>No Course Coordinator set!</h3></tr>
								</s:else>
							</tbody>
						</table>
					</div>
				</div>
			</section>
		</div>

		<%@include file="footer.jsp"%>
		<script type="text/javascript">
			manageUsersLoad = function() {
				var adminData = null;
				var ccData = null;
				var teamData = null;
				var studentData = null;
				var facultyData = null;
				var taData = null;
				var termId = parseInt('<s:property value="termId"/>') !== 0? parseInt('<s:property value="termId"/>') : parseInt("<%= ((Term) session.getAttribute("currentActiveTerm")).getId() %>");
				
				loadUsers();
				
				function loadUsers() {
					adminData = JSON.parse('<s:property escape="false" value="adminJson"/>');
					ccData = JSON.parse('<s:property escape="false" value="ccJson"/>');
					teamData = JSON.parse('<s:property escape="false" value="teamJson"/>');
					studentData = JSON.parse('<s:property escape="false" value="studentJson"/>');
					facultyData = JSON.parse('<s:property escape="false" value="facultyJson"/>');
					taData = JSON.parse('<s:property escape="false" value="taJson"/>');
					console.log('\n\nAdmin data: ' + JSON.stringify(adminData));
					console.log('\n\nCC data: ' + JSON.stringify(ccData));
					console.log('\n\nTeam data: ' + JSON.stringify(teamData));
					console.log('\n\nStudent data: ' + JSON.stringify(studentData));
					console.log('\n\nFaculty data: ' + JSON.stringify(facultyData));
					console.log('\n\nTA data: ' + JSON.stringify(taData));
				}
				
				/****************/
				/* NAVIGATION */
				/***************/
				
				 //Manual navigation because of struts URL
				$('body').on('click', '.usersNav li a', function(){
					var href = $(this).attr('href').split('#')[1];
					$(".tab-pane, .nav-tabs li").removeClass('active');
					$(".tab-pane").hide();
					$(".tab-pane #" + href).addClass('active');
					$(".nav-tabs ." + href).addClass('active');
					$("#" + href).show();
					var userType = (href.slice(-1) === 's'?href.slice(0, -1):href);
					if (userType !== 'cc') {
						$('.addBtn').show();
						$('.editAdminBtn').hide();
						$('.addBtn')
								.attr('id', 'add_' + userType)
								.html(
									$('.addBtn').children('i').outerHTML() + ' Add ' + 
									(userType === 'ta'?userType.toUpperCase()
									:userType.charAt(0).toUpperCase() + userType.slice(1))
								);
					} else {
						$('.addBtn').hide();
						$('.editAdminBtn').show();
					}
					return false;
				});
				
				//Assign Team Link
				$('body').on('click', '.assignTeamsLink', function(){
					$('.usersNav li.teams').children('a').trigger('click');
					$('body').animate({scrollTop: 0}, 500);
					return false;
				});
				
				//Team Name Link
				$('body').on('click', '.studentTeamLink', function(){
					$('.modal').modal('hide');
					$('.usersNav li.teams').children('a').trigger('click');
					var $tr = getTrFromTable('teamsTable', 'teamName', $(this).text());
					$('body').animate({scrollTop: $tr.offset().top - $tr.height()}, 500);
					$tr.effect('highlight', {}, 1500);
					return false;
				});
				
				//Student Name Link
				$('body').on('click', '.teamStudentLink', function(){
					$('.modal').modal('hide');
					$('.usersNav li.students').children('a').trigger('click');
					var $tr = getTrFromTable('studentUsersTable', 'fullName', $(this).text());
					$('body').animate({scrollTop: $tr.offset().top - $tr.height()}, 500);
					$tr.effect('highlight', {}, 1500);
					return false;
				});
				
				//Team Name Link
				$('body').on('click', '.teamFacultyLink', function(){
					$('.modal').modal('hide');
					$('.usersNav li.faculty').children('a').trigger('click');
					var $tr = getTrFromTable('facultyUsersTable', 'fullName', $(this).text());
					$('body').animate({scrollTop: $tr.offset().top - $tr.height()}, 500);
					$tr.effect('highlight', {}, 1500);
					return false;
				});
				
				function getTrFromTable(tableId, tdClass, tdText) {
					var toReturn = false;
					$('#' + tableId + ' tr').each(function(){
						var $tr = $(this);
						var $td = $(this).children('td.' + tdClass);
						if ($td.text().indexOf(tdText) !== -1) {
							toReturn = $tr;
							return false;
						}
					});
					 return toReturn;
				}
				
				//TA Signups Link
				$('body').on('click', '.taSignupsLink', function(){
					var username = $(this).attr('id').split("_")[1];
					var ta = null;
					for (var i = 0; i < taData.length; i++) {
						if (taData[i].username === username) {
							ta = taData[i];
							break;
						}
					}
					bootbox.alert({
						title: 'Signups of ' + ta.name,
						message: function() {
							return $(document.createElement('table')).addClass('modalTable taSignupsTable').append(function(){
								if ($.isEmptyObject(ta.mySignups)) {
									return $(document.createElement('tr')).append($(document.createElement('td')).html('No registered signups!'));
								}
								var $trs = new Array();
								$trs.push(
									$(document.createElement('tr'))
										.append($(document.createElement('th')).html('Milestone'))
										.append($(document.createElement('th')).html('Date'))
										.append($(document.createElement('th')).html('Time'))
										.append($(document.createElement('th')).html('Team'))
								);
								for (var key in ta.mySignups) {
									if (ta.mySignups.hasOwnProperty(key)) {
										$trs.push(
											$(document.createElement('tr'))
												.append(
													$(document.createElement('td')).append(ta.mySignups[key].milestone)
												)
												.append(
													$(document.createElement('td')).append(Date.parse(ta.mySignups[key].datetime).toString('dd MMM yyyy'))
												)
												.append(
													$(document.createElement('td')).append(Date.parse(ta.mySignups[key].datetime).toString('HH:mm'))
												)
												.append(
													$(document.createElement('td'))
														.append(ta.mySignups[key].teamName?
														$(document.createElement('a'))
															.addClass('studentTeamLink')
															.html(ta.mySignups[key].teamName)
															:'-'
														)
												)
										);
									}
								}
								return $trs;
							});
						}
					});
					return false;
				});
				
				/****************/
				/* BUTTONS  */
				/***************/
				
				$('body').on('click', '.editAdminBtn', function(){
					var $this = $(this);
					var userType = 'cc';
					var id = $('#ccUsersTable tr:last').attr('id').split('_')[1];
					var user = ccData[0];
					var editableFields = new Array();
					editableFields.push({order: 1, key: "Username", name:"username", value: user.username});
					editableFields.push({order: 2, key: "Full Name", name:"fullName", value: user.name});
					editUser(user, userType, editableFields);
				});
				
				$('body').on('click', '.modBtn', function(){
					var $this = $(this);
					var userType = $this.closest('tr').attr('class').split('Row')[0];
					var id = $this.closest('tr').attr('id').split('_')[1];
					var user = null;
					var editableFields = new Array();
					switch (userType) {
						case 'student':
							for (var i = 0; i < studentData.length; i++) {
								if (parseInt(studentData[i].id) === parseInt(id)) {
									user = studentData[i];
									if ($this.hasClass('editBtn')) {
										editableFields.push({order: 3, key: "Team", name:"teamId", value: user.teamId});
									}
									break;
								}
							}
							break;
						case 'faculty':
							for (var i = 0; i < facultyData.length; i++) {
								if (parseInt(facultyData[i].id) === parseInt(id)) {
									user = facultyData[i];
									break;
								}
							}
							break;
						case 'ta':
							for (var i = 0; i < taData.length; i++) {
								if (parseInt(taData[i].id) === parseInt(id)) {
									user = taData[i];
									break;
								}
							}
							break;
						case 'admin':
							for (var i = 0; i < adminData.length; i++) {
								if (parseInt(adminData[i].id) === parseInt(id)) {
									user = adminData[i];
									break;
								}
							}
							break;
						default:
							showNotification("ERROR", 'Usertype not found!');
							return false;
					}
					editableFields.push({order: 1, key: "Username", name:"username", value: user.username});
					editableFields.push({order: 2, key: "Full Name", name:"fullName", value: user.name});
					if ($this.hasClass('editBtn')) editUser(user, userType, editableFields);
					else if ($this.hasClass('delBtn')) {
						deleteUser(user, userType);
					}
				});
				
				$('body').on('click', '.addBtn', function(){
					var userType = $(this).attr('id').split('_')[1];
					var addableFields = [{key: "Username", name:"username"}, {key: "Full Name", name: "fullName"}];
					if (userType === 'student') addableFields.push({key: "Team", name: "teamId"});
					addUser(userType, addableFields);
				});
				
				/*******************/
				/* ACTIONS    */
				/*******************/
				
				function addUser(userType, addableFields) {
					bootbox.confirm({
						title: "Add " + (userType === 'ta'?'TA':userType.charAt(0).toUpperCase() + userType.slice(1)),
						message: function() {
							return $(document.createElement('form')).addClass('modalForm').append($(document.createElement('table')).addClass('modalTable').append(function(){
									if (addableFields.length === 0) {
										return $(document.createElement('tr')).append($(document.createElement('td')).html('Not addable user!'));
									}
									var $trs = new Array();
									for (var i = 0; i < addableFields.length; i++) {
											$trs.push(
												$(document.createElement('tr'))
													.append(
														$(document.createElement('th')).append(addableFields[i].key)
													)
													.append(
														$(document.createElement('td')).append(function(){
															if (addableFields[i].key === 'Team') {
																return $(document.createElement('select'))
																	.attr('id', 'editTeamSelect')
																	.attr('name', addableFields[i].name)
																	.append(function(){
																		var $options = new Array();
																		$options.push(
																			$(document.createElement('option'))
																				.attr('id', 'teamSelectHeader')
																				.attr('value', '-1')
																				.html('No Team')
																		);
																		for (var i = 0; i < teamData.length; i++) {
																			$options.push(
																				$(document.createElement('option'))
																					.attr('value', teamData[i].id)
																					.html(teamData[i].teamName)
																			);
																		}
																		return $options;
																	});
															} else {
																return $(document.createElement('input'))
																	.attr('type', 'text')
																	.attr('name', addableFields[i].name);
															}
														})
													)
											);
									}
									return $trs;
								})
							);
						},
						callback: function(result) {
							if (result) {
								var formData = $('.modalForm').serializeArray();
								var submitData = {
									action: 'add',
									type: userType === 'cc'?'COURSE_COORDINATOR':userType === 'admin'?'ADMINISTRATOR':userType.toUpperCase(),
									termId: termId
								};
								for (var i = 0; i < formData.length; i++) {
									submitData[formData[i].name] = formData[i].value;
									if (userType === 'student' && formData[i].name === 'teamId') {
										for (var j = 0; j < teamData.length; j++) {
											if (parseInt(teamData[j].id) === parseInt(formData[i].value)) {
												submitData['teamName'] = teamData[j].teamName;
												break;
											}
										}
									}
								}
								console.log('Submitting add user data: ' + JSON.stringify(submitData));
								$.ajax({
									type: 'POST',
									url: 'manageUser',
									data: {jsonData: JSON.stringify(submitData)},
									async: false,
									cache: false
								}).done(function(response) {
									if (response.success) {
										setTimeout(function(){showNotification("SUCCESS", 'Added successfully');}, 500);
										updateUserJsonData({id: response.userId}, userType, submitData);
										updateUserPage({id: response.userId}, userType, submitData);
									} else {
										setTimeout(function(){showNotification("ERROR", response.message);}, 500);
									}
									return true;
								}).fail(function(error){
									var eid = btoa(response.message);
									window.location = "error.jsp?eid=" + eid;
								});
							}
						}
					});
				}
				
				function editUser(user, userType, editableFields) {
					//Order comparator
					function compare(a, b) {
						if (a.order < b.order) {
							return -1;
						} else if (a.order > b.order) {
							return 1;
						} else {
							return 0;
						}
					}
					bootbox.confirm({
						title: "Edit " + (userType === 'ta'?'TA':userType.charAt(0).toUpperCase() + userType.slice(1)),
						message: function() {
							return $(document.createElement('form')).addClass('modalForm').append($(document.createElement('table')).addClass('modalTable').append(function(){
									if (editableFields.length === 0) {
										return $(document.createElement('tr')).append($(document.createElement('td')).html('Not editable user!'));
									}
									editableFields.sort(compare); //Sort by order first
									var $trs = new Array();
									for (var i = 0; i < editableFields.length; i++) {
											$trs.push(
												$(document.createElement('tr'))
													.append(
														$(document.createElement('th')).append(editableFields[i].key)
													)
													.append(
														$(document.createElement('td')).append(function(){
															if (editableFields[i].key === 'Team') {
																return $(document.createElement('select'))
																	.attr('id', 'editTeamSelect')
																	.attr('name', editableFields[i].name)
																	.append(function(){
																		var $options = new Array();
																		for (var i = 0; i < teamData.length; i++) {
																			$options.push(
																				$(document.createElement('option'))
																					.attr('value', teamData[i].id)
																					.html(teamData[i].teamName)
																			);
																		}
																		return $options;
																	})
																	.val(editableFields[i].value).change();
															} else {
																return $(document.createElement('input'))
																	.attr('type', 'text')
																	.attr('name', editableFields[i].name)
																	.val(editableFields[i].value).change();
															}
														})
													)
											);
									}
									return $trs;
								})
							);
						},
						callback: function(result) {
							if (result) {
								var formData = $('.modalForm').serializeArray();
								var submitData = {
									action: 'edit',
									type: userType === 'cc'?'COURSE_COORDINATOR':userType === 'admin'?'ADMINISTRATOR':userType.toUpperCase(),
									userId: user.id,
									termId: termId
								};
								for (var i = 0; i < formData.length; i++) {
									submitData[formData[i].name] = formData[i].value;
									if (userType === 'student' && formData[i].name === 'teamId') {
										for (var j = 0; j < teamData.length; j++) {
											if (parseInt(teamData[j].id) === parseInt(formData[i].value)) {
												submitData['teamName'] = teamData[j].teamName;
												break;
											}
										}
									}
								}
								console.log('Submitting edit user data: ' + JSON.stringify(submitData));
								$.ajax({
									type: 'POST',
									url: 'manageUser',
									data: {jsonData: JSON.stringify(submitData)},
									async: false,
									cache: false
								}).done(function(response) {
									if (response.success) {
										setTimeout(function(){showNotification("SUCCESS", 'Updated successfully');}, 500);
										updateUserJsonData(user, userType.toUpperCase(), submitData);
										updateUserPage(user, userType.toUpperCase(), submitData);
									} else {
										setTimeout(function(){showNotification("ERROR", response.message);}, 500);
									}
									return true;
								}).fail(function(error){
									var eid = btoa(response.message);
									window.location = "error.jsp?eid=" + eid;
								});
							}
						}
					});
				}
				
				function deleteUser(user, userType) {
					bootbox.confirm({
						title: "Remove User",
						message: "Are you sure you want to remove " + user.name + "?",
						callback: function(result) {
							if (result) {
								var submitData = {
									action: 'delete',
									userId: user.id
								};
								$.ajax({
									type: 'POST',
									url: 'manageUser',
									data: {jsonData: JSON.stringify(submitData)},
									async: false,
									cache: false
								}).done(function(response) {
									if (response.success) {
										setTimeout(function(){showNotification("SUCCESS", 'Deleted successfully');}, 500);
										updateUserJsonData(user, userType.toUpperCase(), submitData);
										updateUserPage(user, userType.toUpperCase(), submitData);
									} else {
										setTimeout(function(){showNotification("ERROR", response.message);}, 500);
									}
									return true;
								}).fail(function(error){
									var eid = btoa(response.message);
									window.location = "error.jsp?eid=" + eid;
								});
							}
						}
					});
				}
				
				function updateUserJsonData(user, userType, submitData) {
					var roleData = null;
					switch (userType.toUpperCase()) {
						case 'STUDENT':
							roleData = studentData;
							break;
						case 'FACULTY':
							roleData = facultyData;
							break;
						case 'TA':
							roleData = taData;
							break;
						case 'ADMIN':
							roleData = adminData;
							break;
						case 'CC':
							roleData = ccData;
							break;
						default:
							console.log('Role ' + userType);
							showNotification('ERROR', 'Unkown userType');
							return false;
					}
					switch(submitData.action) {
						case 'add':
							//Add to role data
							roleData.push({
								id: user.id,
								name: submitData.fullName, 
								username: submitData.username,
								teamId: submitData.teamId?submitData.teamId:false,
								teamName: submitData.teamName?submitData.teamName:false,
								myTeams: {},
								mySignups: {}
							});
							//Add to team data
							if (submitData.teamName) {
								for (var i = 0; i < teamData.length; i++) {
									if (parseInt(teamData[i].id) === parseInt(submitData.teamId)) {
										teamData[i].members.push({
											id: user.id,
											name: submitData.fullName,
											username: submitData.username
										});
										break;
									}
								}
							}
							break;
						case 'edit':
							//Modify teamData
							if (submitData.teamId && parseInt(user.teamId) !== parseInt(submitData.teamId)) {
								for (var i = 0; i < teamData.length; i++) {
									var members = teamData[i].members;
									for (var j = 0; j < members.length; j++) {
										if (parseInt(user.id) === parseInt(members[j].id)) {
											//Remove from old team
											members.splice(members.indexOf(members[j]), 1);
											break;
										}
									}
									if (parseInt(submitData.teamId) === parseInt(teamData[i].id)) {
										//Add to new team
										teamData[i].members.push({
											id: user.id,
											name: user.name,
											username: user.username
										});
									}
								}
							}
							//Edit Role-based Json
							for (var i = 0; i < roleData.length; i++) {
								if (parseInt(roleData[i].id) === parseInt(user.id)) {
									roleData[i].name = submitData.fullName;
									roleData[i].username = submitData.username;
									roleData[i].teamId = submitData.teamId?submitData.teamId:false;
									roleData[i].teamName = submitData.teamName?submitData.teamName:false;
									break;
								}
							}
							//Edit Team-based Json
							for (var i = 0; i < teamData.length; i++) {
								var members = teamData[i].members;
								for (var j = 0; j < members.length; j++) {
									if (parseInt(members[j].id) === parseInt(user.id)) {
										members[j].name = submitData.fullName;
										members[j].username = submitData.username;
										break;
									}
								}
								if (parseInt(teamData[i].supervisor.id) === user.id) {
									teamData[i].supervisor.name = submitData.fullName;
									teamData[i].supervisor.username = submitData.username;
								}
								if (parseInt(teamData[i].reviewer1.id) === user.id) {
									teamData[i].reviewer1.name = submitData.fullName;
									teamData[i].reviewer1.username = submitData.username;
								}
								if (parseInt(teamData[i].reviewer2.id) === user.id) {
									teamData[i].reviewer2.name = submitData.fullName;
									teamData[i].reviewer2.username = submitData.username;
								}
							}
							break;
						case 'delete':
							//Delete from teamData
							for (var i = 0; i < teamData.length; i++) {
								var members = teamData[i].members;
								for (var j = 0; j < members.length; j++) {
									if (parseInt(members[j].id) === parseInt(user.id)) {
										members.splice(members.indexOf(members[j]), 1);
										break;
									}
								}
								if (parseInt(teamData[i].supervisor.id) === user.id) {
									delete teamData[i].supervisor;
									//Replace with CC
									var cc = ccData[0];
									teamData[i].supervisor = cc;
								}
								if (parseInt(teamData[i].reviewer1.id) === user.id) {
									delete teamData[i].reviewer1;
									//Replace with CC
									var cc = ccData[0];
									teamData[i].reviewer1 = cc;
								}
								if (parseInt(teamData[i].reviewer2.id) === user.id) {
									delete teamData[i].reviewer2;
									//Replace with CC
									var cc = ccData[0];
									teamData[i].reviewer2 = cc;
								}
							}
							//Delete from roleData
							for (var i = 0; i < roleData.length; i++) {
								if (parseInt(roleData[i].id) === parseInt(user.id)) {
									delete roleData[i];
									break;
								}
							}
							break;
						default:
							console.log('Action: ' + submitData.action);
							showNotification('ERROR', 'Unknown action');
							return false;
					}
				}
				
				function updateUserPage(user, userType, submitData) {
					switch (submitData.action) {
						case 'add':
							//Add to role table
							var $lastTr = $('#' + userType + 'UsersTable tr:last');
							$lastTr.after(
								$(document.createElement('tr'))
									.addClass(userType + 'Row')
									.attr('id', 'user_' + user.id)
									.append(
										$(document.createElement('td'))
											.append(
												$(document.createElement('i')).addClass('fa fa-user fa-black')
											)
									)
									.append(
										$(document.createElement('td'))
											.addClass('fullName')
											.append(
												submitData.fullName
											)
									)
									.append(
										$(document.createElement('td'))
											.addClass('username')
											.append(
												submitData.username
											)
									)
									.append(
										$(document.createElement('td'))
											.addClass('mobileNumber')
											.append(
												'-'
											)
									)
									.append(function() {
										var $roleSpecificTds = new Array();
										switch (userType) {
											case 'student':
													$roleSpecificTds.push(
														$(document.createElement('td'))
															.addClass('teamName')
															.append(function() {
																if (submitData.teamName) {
																	return $(document.createElement('a'))
																		.addClass('studentTeamLink')
																		.attr('id', 'teams_' + submitData.teamId)
																		.attr('href', 'teams_' + submitData.teamName)
																		.html(submitData.teamName);
																} else {
																	return $(document.createElement('a'))
																		.addClass('assignTeamsLink')
																		.attr('id', 'assignTeams_' + submitData.username)
																		.attr('href', 'assignTeams_' + submitData.username)
																		.html('Assign Team');
																}
															})
													);
												break;
											case 'faculty':
												$roleSpecificTds.push(
													$(document.createElement('td'))
														.append(
															$(document.createElement('a'))
																.addClass('assignTeamsLink')
																.attr('id', 'assignTeams_' + submitData.username)
																.attr('href', 'assignTeams_' + submitData.username)
																.html('Assign Teams')
														)
												);
												$roleSpecificTds.push(
													$(document.createElement('td'))
														.append(
															$(document.createElement('a'))
																.addClass('assignTeamsLink')
																.attr('id', 'assignTeams_' + submitData.username)
																.attr('href', 'assignTeams_' + submitData.username)
																.html('Assign Teams')
														)
												);
												$roleSpecificTds.push(
													$(document.createElement('td'))
														.append(
															$(document.createElement('a'))
																.addClass('assignTeamsLink')
																.attr('id', 'assignTeams_' + submitData.username)
																.attr('href', 'assignTeams_' + submitData.username)
																.html('Assign Teams')
														)
												);
												break;
											case 'ta':
												$roleSpecificTds.push(
													$(document.createElement('td'))
														.append(
															$(document.createElement('a'))
																.addClass('taSignupsLink')
																.attr('id', 'signups_' + submitData.username)
																.attr('href', 'ta_' + submitData.username)
																.html('0')
														)
												);
												break;
											default:
												return false;
										};
										return $roleSpecificTds;
									})
									.append(
										$(document.createElement('td'))
											.append(
												$(document.createElement('button'))
													.attr('type', 'button')
													.attr('title', 'Edit')
													.addClass('modBtn editBtn btn btn-info')
													.append(
														$(document.createElement('i')).addClass('fa fa-pencil fa-white')
													)
											)
									)
									.append(
										$(document.createElement('td'))
											.append(
												$(document.createElement('button'))
													.attr('type', 'button')
													.attr('title', 'Delete')
													.addClass('modBtn delBtn btn btn-danger')
													.append(
														$(document.createElement('i')).addClass('fa fa-trash-o fa-white')
													)
											)
									)
							);
							//Add student to Team table
							if (submitData.teamName) {
								var $newTr = getTrFromTable('teamsTable', 'teamName', submitData.teamName);
								$newTr.find('.memberList').append(
									$(document.createElement('span'))
										.addClass('memberName')
										.attr('id', 'member_' + user.id)
										.append(
											$(document.createElement('a'))
												.addClass('teamStudentLink')
												.attr('id', 'teamStudent_' + user.id)
												.attr('href', 'member_' + submitData.username)
												.html(submitData.fullName)
										)
								);
							}
							break;
						case 'edit':
							var $tr = $('tr#user_' + user.id);
							for (var key in submitData) {
								if (submitData.hasOwnProperty(key)) {
									var $td = $tr.find('td.' + key);
									if ($td.length) {
										if (key === 'teamName') $td.children('a').html(submitData[key]);
										else $td.html(submitData[key]);
									}
								}
							}
							if (submitData.teamName) {
								//Change member in Teams Table
								$('#teamsTable').find('#member_' + submitData.userId).fadeOut('slow', function(){
									$(this).remove();
								});
								var $newTr = getTrFromTable('teamsTable', 'teamName', submitData.teamName);
								$newTr.find('.memberList').append(
									$(document.createElement('span'))
										.addClass('memberName')
										.attr('id', 'member_' + submitData.userId)
										.append(
											$(document.createElement('a'))
												.addClass('teamStudentLink')
												.attr('id', 'teamStudent_' + submitData.userId)
												.attr('href', 'member_' + submitData.username)
												.html(submitData.fullName)
										)
								);
							} else if (userType.toUpperCase() === 'FACULTY'){
								$('#teamsTable').find('.teamSupervisor_' + submitData.userId).each(function(){
									$(this).html(
										$(document.createElement('a'))
											.addClass('teamFacultyLink')
											.attr('id', 'teamSupervisor_' + submitData.userId)
											.attr('href', 'teamSupervisor_' + submitData.username)
											.html(submitData.fullName)
									);
								});
								$('#teamsTable').find('.teamReviewer1_' + submitData.userId).each(function(){
									$(this).html(
										$(document.createElement('a'))
											.addClass('teamFacultyLink')
											.attr('id', 'teamReviewer1_' + submitData.userId)
											.attr('href', 'teamReviewer1_' + submitData.username)
											.html(submitData.fullName)
									);
								});
								$('#teamsTable').find('.teamReviewer2_' + submitData.userId).each(function(){
									$(this).html(
										$(document.createElement('a'))
											.addClass('teamFacultyLink')
											.attr('id', 'teamReviewer2_' + submitData.userId)
											.attr('href', 'teamReviewer2_' + submitData.username)
											.html(submitData.fullName)
									);
								});
							} else if (userType.toUpperCase() === 'CC'){
								$('#ccUsersTable tr').each(function(){
									$(this).children('td.fullName').html(submitData.fullName);
									$(this).children('td.username').html(submitData.username);
								});
							}
							break;
						case 'delete':
							break;
						default:
							console.log('Action: ' + submitData.action);
							showNotification('ERROR', 'Unknown action');
							return false;
					}
				}
				
				/*******************/
				/* NOTIFICATIONS */
				/*******************/

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
				
			};
			
			addLoadEvent(manageUsersLoad);
    </script>
  </body> 
</html>
