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
				<form method="POST">
					<table class="selectTermTable">
						<tr>
							<td class="formLabelTd">Select Term</td>
							<td>
								<select class="termPicker" name="selectedTermId" onchange="this.form.submit()">
									<s:iterator var="term" value="termData">
										<s:if test="%{selectedTermId > 0 && selectedTermId == #term.termId}">
											<option value="<s:property value="#term.termId"/>" selected><s:property value="#term.termName"/></option>
										</s:if>
										<s:else>
											<option value="<s:property value="#term.termId"/>"><s:property value="#term.termName"/></option>
										</s:else>
									</s:iterator>
								</select>
							</td>
						</tr>
					</table>
				</form>
				<button type='button' id='add_team' class='addTeamBtn pull-right btn btn-primary'>
					<i class='fa fa fa-plus fa-white'></i> Add Team
				</button>
				<button type='button' id='add_student' class='addBtn pull-right btn btn-primary' style='display:none'>
					<i class='fa fa fa-plus fa-white'></i> Add Student
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
												<button type='button' title="Edit" class='editTeamBtn btn btn-info'>
													<i class='fa fa fa-pencil fa-white'></i>
												</button>
											</td>
											<td>
												<button type='button' title="Delete User" class='delTeamBtn btn btn-danger'>
													<i class='fa fa fa-trash-o fa-white'></i>
												</button>
											</td>
										</tr>
									</s:iterator>
								</s:if>
								<s:else>
									<tr><td colspan="8"><h3 class='noUsersMsg'>No Team set!</h3></td></tr>
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
												<a class='taSignupsLink' id='signups_<s:property value="id"/>' href='ta_<s:property value="username"/>'><s:property value="%{#ta.mySignups.size()}"/></a>
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
				var termId = parseInt('<s:property value="selectedTermId"/>') !== 0? parseInt('<s:property value="selectedTermId"/>') : parseInt("<%= ((Term) session.getAttribute("currentActiveTerm")).getId() %>");
				
				loadUsers();
				
				function loadUsers() {
					adminData = convertUserData(JSON.parse('<s:property escape="false" value="adminJson"/>'));
					ccData = convertUserData(JSON.parse('<s:property escape="false" value="ccJson"/>'));
					teamData = convertUserData(JSON.parse('<s:property escape="false" value="teamJson"/>'));
					studentData = convertUserData(JSON.parse('<s:property escape="false" value="studentJson"/>'));
					facultyData = convertUserData(JSON.parse('<s:property escape="false" value="facultyJson"/>'));
					taData = convertUserData(JSON.parse('<s:property escape="false" value="taJson"/>'));
					console.log('\n\nAdmin data: ' + JSON.stringify(adminData));
					console.log('\n\nCC data: ' + JSON.stringify(ccData));
					console.log('\n\nTeam data: ' + JSON.stringify(teamData));
					console.log('\n\nStudent data: ' + JSON.stringify(studentData));
					console.log('\n\nFaculty data: ' + JSON.stringify(facultyData));
					console.log('\n\nTA data: ' + JSON.stringify(taData));
				}
				
				function convertUserData(arrayData) {
					var jsonData = {};
					for (var i = 0; i < arrayData.length; i++) {
						jsonData[arrayData[i].id] = arrayData[i];
					}
					return jsonData;
				}
				
				function convertMultiselectOptionData(jsonData) {
					var multiselectOptionArray = new Array();
					for (var key in jsonData) {
						if (jsonData.hasOwnProperty(key)) {
							multiselectOptionArray.push({
								label: jsonData[key].name,
								value: jsonData[key].id
							});
						}
					}
					return multiselectOptionArray;
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
					if (userType !== 'cc' && userType !== 'team') {
						$('.addTeamBtn').hide();
						$('.editAdminBtn').hide();
						$('.addBtn').show();
						$('.addBtn')
								.attr('id', 'add_' + userType)
								.html(
									$('.addBtn').children('i').outerHTML() + ' Add ' + 
									(userType === 'ta'?userType.toUpperCase()
									:userType.charAt(0).toUpperCase() + userType.slice(1))
								);
					} else if (userType === 'cc') {
						$('.addBtn').hide();
						$('.addTeamBtn').hide();
						$('.editAdminBtn').show();
					} else if (userType === 'team') {
						$('.addTeamBtn').show();
						$('.addBtn').hide();
						$('.editAdminBtn').hide();
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
					var ta = taData[$(this).attr('id').split("_")[1]];
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
							user = studentData[id];
							if ($this.hasClass('editBtn')) {
								editableFields.push({order: 3, key: "Team", name:"teamId", value: user.teamId});
							}
							break;
						case 'faculty':
							user = facultyData[id];
							break;
						case 'ta':
							user = taData[id];
							break;
						case 'admin':
							user = adminData[id];
							break;
						default:
							showNotification("ERROR", 'Usertype not found!');
							return false;
					}
					editableFields.push({order: 1, key: "Username", name:"username", value: user.username});
					editableFields.push({order: 2, key: "Full Name", name:"fullName", value: user.name});
					editableFields.sort(compare);
					if ($this.hasClass('editBtn')) editUser(user, userType, editableFields);
					else if ($this.hasClass('delBtn')) {
						deleteUser(user, userType);
					}
				});
				
				$('body').on('click', '.addBtn', function(){
					var userType = $(this).attr('id').split('_')[1];
					var addableFields = [{key: "Username", name:"username", order: 1}, {key: "Full Name", name: "fullName", order: 2}];
					if (userType === 'student') addableFields.push({key: "Team", name: "teamId", order: 3});
					addableFields.sort(compare);
					addUser(userType, addableFields);
				});
				
				$('body').on('click', '.addTeamBtn, .editTeamBtn', function(){
					var action = $(this).hasClass('addTeamBtn')?'add':'edit';
					var team = null;
					var $tr = null;
					if (action === 'edit') {
						var $tr = $(this).closest('tr');
						team = teamData[$tr.attr('id').split('_')[1]];
					}
					bootbox.confirm({
						className: 'manageTeamModal',
						title: action.charAt(0).toUpperCase() + action.slice(1) + " Team",
						message: function() {
							return $(document.createElement('form')).addClass('modalForm').append($(document.createElement('table')).addClass('modalTable modalTeamTable').append(function(){
									var $trs = new Array();
									$trs.push(
										$(document.createElement('tr'))
											.append($(document.createElement('th'))
												.append($(document.createElement('i')).addClass('fa fa-group fa-black'))
												.append(' Team Name'))
											.append(
												$(document.createElement('td'))
												.append(
													$(document.createElement('input'))
														.attr('type', 'text')
														.attr('id', 'teamName')
														.attr('name', 'name')
														.val(action === 'edit'?team.teamName:'').change()
												)
											)
									);
									$trs.push(
										$(document.createElement('tr'))
											.append($(document.createElement('th'))
												.append($(document.createElement('i')).addClass('fa fa-vk fa-black'))
												.append(' Wiki'))
											.append(
												$(document.createElement('td'))
												.append(
													$(document.createElement('input'))
														.attr('type', 'text')
														.attr('id', 'wiki')
														.attr('name', 'wiki')
														.val(action === 'edit'?team.wiki:'').change()
												)
											)
									);
									$trs.push(
										$(document.createElement('tr'))
											.append($(document.createElement('th'))
												.append($(document.createElement('i')).addClass('fa fa-user fa-black'))
												.append(' Members'))
											.append(
												$(document.createElement('td'))
												.addClass('student')
												.append(
													$(document.createElement('select'))
														.addClass('membersMultiselect multiselect multiselect-search')
														.attr('multiple', 'multiple')
														.attr('id', 'members')
														.attr('name', 'members')
												)
											)
									);
									$trs.push(
										$(document.createElement('tr'))
											.append($(document.createElement('th'))
												.append($(document.createElement('i')).addClass('fa fa-briefcase fa-black'))
												.append(' Supervisor'))
											.append(
												$(document.createElement('td'))
												.addClass('faculty')
												.append(
													$(document.createElement('select'))
														.addClass('supervisorMultiSelect multiselect multiselect-search')
														.attr('multiple', 'multiple')
														.attr('id', 'supervisor')
														.attr('name', 'supervisor')
												)
											)
									);
									$trs.push(
										$(document.createElement('tr'))
											.append($(document.createElement('th'))
												.append($(document.createElement('i')).addClass('fa fa-briefcase fa-black'))
												.append(' Reviewer 1'))
											.append(
												$(document.createElement('td'))
												.addClass('faculty')
												.append(
													$(document.createElement('select'))
														.addClass('reviewer1MultiSelect multiselect multiselect-search')
														.attr('multiple', 'multiple')
														.attr('id', 'reviewer1')
														.attr('name', 'reviewer1')
												)
											)
									);
									$trs.push(
										$(document.createElement('tr'))
											.append($(document.createElement('th'))
												.append($(document.createElement('i')).addClass('fa fa-briefcase fa-black'))
												.append(' Reviewer 2'))
											.append(
												$(document.createElement('td'))
												.addClass('faculty')
												.append(
													$(document.createElement('select'))
														.addClass('reviewer2MultiSelect multiselect multiselect-search')
														.attr('multiple', 'multiple')
														.attr('id', 'reviewer2')
														.attr('name', 'reviewer2')
												)
											)
									);
									return $trs;
								})
							);
						},
						callback: function(result) {
							if (result) {
								var formData = $('.modalForm').serializeArray();
								var submitData = {
									action: action,
									termId: termId,
									teamId: action === 'edit'?$tr.attr('id').split('_')[1]:null
								};
								for (var i = 0; i < formData.length; i++) {
									if (formData[i].name === 'members') submitData['members'] = $('.multiselect').val();
									else submitData[formData[i].name] = formData[i].value;
								}
								console.log('Submitting ' + action + ' team data: ' + JSON.stringify(submitData));
								$.ajax({
									type: 'POST',
									url: 'manageTeam',
									data: {jsonData: JSON.stringify(submitData)},
									async: false,
									cache: false
								}).done(function(response) {
									if (response.success) {
										setTimeout(function(){showNotification("SUCCESS", action.charAt(0).toUpperCase() + action.slice(1) + 'ed successfully');}, 500);
//										updateTeamJsonData(action === 'add'?{id: response.teamId}:null, submitData);
										updateTeamPage(action === 'add'?{id: response.teamId}:null, submitData);
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
					//Multiselect options
					$('.multiselect').each(function(){
						var $this = $(this);
						if ($this.parent().hasClass('student')) {
							$this.multiselect({
								buttonText: function(options, select) {
									if (options.length === 0) {
										return 'Select Students <b class="caret"></b>';
									} else {
										var selected = '';
										options.each(function(){
											selected += $(this).text() + "<br/>";
										});
										return selected + ' <b class="caret"></b>';
									}
								},
								enableCaseInsensitiveFiltering: true,
								filterPlaceholder: 'Search',
								buttonClass: 'btn btn-link teamUserSelectBtn'
							});
							$this.multiselect('dataprovider', convertMultiselectOptionData(studentData));
							if (action === 'edit') {
								var valArray = new Array();
								for (var i = 0; i < team.members.length; i++) {
									valArray.push(parseInt(team.members[i].id));
								};
								$this.multiselect('select', valArray);
							}
						} else if ($this.parent().hasClass('faculty')) {
							$this.multiselect({
								buttonText: function(options, select) {
									if (options.length === 0) {
										return 'Select Faculty <b class="caret"></b>';
									} else {
										var selected = '';
										options.each(function(){
											selected += $(this).text();
										});
										return selected + ' <b class="caret"></b>';
									}
								},
								onChange: function($option, checked) {
									if ($this.val() !== null && checked) {
										var vals = $this.val();
										if (vals.length > 1) {
											for (var i = 0; i < vals.length; i++) {
												if (vals[i] !== $option.attr('value')) $this.multiselect('deselect', vals[i]);
											}
										}
									}
								},
								enableCaseInsensitiveFiltering: true,
								filterPlaceholder: 'Search',
								buttonClass: 'btn btn-link teamUserSelectBtn'
							});
							$this.multiselect('dataprovider', convertMultiselectOptionData(facultyData));
							if (action === 'edit') {
								switch ($this.attr('id')) {
									case 'supervisor':
										$this.multiselect('select', team.supervisor.id);
										break;
									case 'reviewer1':
										$this.multiselect('select', team.reviewer1.id);
										break;
									case 'reviewer2':
										$this.multiselect('select', team.reviewer2.id);
										break;
									default:
										showNotification('ERROR', 'Unknown faculty in multiselect');
										return false;
								}
							}
						} else {
							showNotification('ERROR', 'Multiselect error');
							return false;
						}
					});
					return false;
				});
				
				/*******************/
				/* ACTIONS    */
				/*******************/
				
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
										submitData['teamName'] = teamData[formData[i].value].teamName;
										break;
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
					bootbox.confirm({
						title: "Edit " + (userType === 'ta'?'TA':userType === 'cc'?'Course Coordinator':userType.charAt(0).toUpperCase() + userType.slice(1)),
						message: function() {
							return $(document.createElement('form')).addClass('modalForm').append($(document.createElement('table')).addClass('modalTable').append(function(){
									if (editableFields.length === 0) {
										return $(document.createElement('tr')).append($(document.createElement('td')).html('Not editable user!'));
									}
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
																		$options.push(
																			$(document.createElement('option'))
																				.attr('id', 'teamSelectHeader')
																				.attr('value', '-1')
																				.html('No Team')
																		);
																		for (var key in teamData) {
																			if (teamData.hasOwnProperty(key)) {
																				var team = teamData[key];
																				$options.push(
																					$(document.createElement('option'))
																						.attr('value', team.id)
																						.html(team.teamName)
																				);
																			}
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
									if (userType === 'student' && formData[i].name === 'teamId' && formData[i].value > 0) {
										submitData['teamName'] = teamData[formData[i].value].teamName;
										break;
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
							roleData[user.id] = {
								id: user.id,
								name: submitData.fullName, 
								username: submitData.username,
								teamId: submitData.teamId?submitData.teamId:false,
								teamName: submitData.teamName?submitData.teamName:false,
								myTeams: {},
								mySignups: {},
								label: submitData.fullName,
								value: user.id
							};
							//Add to team data
							if (submitData.teamName) {
								teamData[submitData.teamId].members.push({
									id: user.id,
									name: submitData.fullName,
									username: submitData.username
								});
							}
							break;
						case 'edit':
							//Modify teamData
							if (submitData.teamId && parseInt(user.teamId) !== parseInt(submitData.teamId)) {
								for (var key in teamData) {
									if (teamData.hasOwnProperty(key)) {
										var team = teamData[key];
										for (var j = 0; j < team.members.length; j++) {
											if (parseInt(user.id) === parseInt(team.members[j].id)) {
												//Remove from old team
												team.members.splice(team.members.indexOf(team.members[j]), 1);
												break;
											}
										}
									}
								}
								//Add to new team
								if (submitData.teamName) {
									teamData[submitData.teamId].members.push({
										id: user.id,
										name: user.name,
										username: user.username
									});
								}
							}
							//Edit Role-based Json
							roleData[user.id] = {
								name: submitData.fullName,
								username: submitData.username,
								teamId: submitData.teamId?submitData.teamId:false,
								teamName: submitData.teamName?submitData.teamName:false,
								label: submitData.fullName,
								value: user.id
							};
							//Edit Team-based Json
							for (var key in teamData) {
								if (teamData.hasOwnProperty(key)) {
									var team = teamData[key];
									for (var j = 0; j < team.members.length; j++) {
										if (parseInt(user.id) === parseInt(team.members[j].id)) {
											//Team member
											team.members[j].name = submitData.fullName;
											team.members[j].username = submitData.username;
										}
									}
									if (parseInt(team.supervisor.id) === user.id) {
										team.supervisor.name = submitData.fullName;
										team.supervisor.username = submitData.username;
									}
									if (parseInt(team.reviewer1.id) === user.id) {
										team.reviewer1.name = submitData.fullName;
										team.reviewer1.username = submitData.username;
									}
									if (parseInt(team.reviewer2.id) === user.id) {
										team.reviewer2.name = submitData.fullName;
										team.reviewer2.username = submitData.username;
									}
								}
							}
							break;
						case 'delete':
							//Delete Team-based Json
							for (var key in teamData) {
								if (teamData.hasOwnProperty(key)) {
									var team = teamData[key];
									for (var j = 0; j < team.members.length; j++) {
										if (parseInt(user.id) === parseInt(team.members[j].id)) {
											//Team member
											delete team.members[j];
										}
									}
									if (parseInt(team.supervisor.id) === user.id) {
										delete team.supervisor;
									}
									if (parseInt(team.reviewer1.id) === user.id) {
										delete team.reviewer1;
									}
									if (parseInt(team.reviewer2.id) === user.id) {
										delete team.reviewer2;
									}
								}
							}
							delete roleData[user.id]; //Delete from roleData
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
																.attr('id', 'signups_' + user.id)
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
							if (!submitData.teamName && submitData.teamId) {
								//Assign Team Link
								$tr.find('td.teamName').empty().append(
									$(document.createElement('a'))
										.addClass('assignTeamsLink')
										.attr('id', 'assignTeams_' + submitData.username)
										.attr('href', 'assignTeams_' + submitData.username)
										.html('Assign Teams')
								);
							}
							for (var key in submitData) {
								if (submitData.hasOwnProperty(key)) {
									var $td = $tr.find('td.' + key);
									if ($td.length) {
										if (key === 'teamName') $td.empty().append(
											$(document.createElement('a'))
												.addClass('studentTeamLink')
												.attr('id', 'teams_' + submitData.teamId)
												.attr('href', 'teams_' + submitData.teamName)
												.html(submitData[key])
										);
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
				
				function updateTeamJsonData(team, submitData) {
					if (submitData.action === 'add' || submitData.action === 'edit') {
						//Add/Edit to team and user data
						var teamId = submitData.action === 'add'?team.id:submitData.teamId;
						var newMemberArray = new Array();
						for (var i = 0; submitData.members.length; i++) {
							newMemberArray.push({id: submitData.members[i]});
						}
						var supervisor = facultyData[submitData.supervisor];
						supervisor.supervisorTeams.push({
							teamId: teamId,
							teamName: submitData.name
						});
						var reviewer1 = facultyData[submitData.reviewer1];
						reviewer1.reviewer1Teams.push({
							teamId: teamId,
							teamName: submitData.name
						});
						var reviewer2 = facultyData[submitData.reviewer2];
						reviewer2.reviewer2Teams.push({
							teamId: teamId,
							teamName: submitData.name
						});
						teamData[teamId] = {
							id: teamId,
							teamName: submitData.name,
							wiki: submitData.wiki,
							members: newMemberArray,
							supervisor: supervisor,
							reviewer1: reviewer1,
							reviewer2: reviewer2
						};
						console.log('New team data: ' + JSON.stringify(teamData[teamId]));
					} else if (submitData.action === 'delete') {
						delete teamData[submitData.teamId];
						for (var key in studentData) {
							if (studentData.hasOwnProperty(key)) {
								var student = studentData[key];
								if (parseInt(student.teamId) === submitData.teamId) {
									delete student.teamId;
									delete student.teamName;
								}
							}
						}
						for (var key in facultyData) {
							if (facultyData.hasOwnProperty(key)) {
								var faculty = facultyData[key];
								for (var i = 0; i < faculty.supervisorTeams.length; i++) {
									if (parseInt(faculty.supervisorTeams[i].teamId) === parseInt(submitData.teamId)) {
										delete faculty.supervisorTeams[i];
									}
								}
								for (var i = 0; i < faculty.reviewer1Teams.length; i++) {
									if (parseInt(faculty.reviewer1Teams[i].teamId) === parseInt(submitData.teamId)) {
										delete faculty.reviewer1Teams[i];
									}
								}
								for (var i = 0; i < faculty.reviewer2Teams.length; i++) {
									if (parseInt(faculty.reviewer2Teams[i].teamId) === parseInt(submitData.teamId)) {
										delete faculty.reviewer2Teams[i];
									}
								}
							}
						}
					} else {
						console.log('Action: ' + submitData.action);
						showNotification('ERROR', 'Unknown team action');
					}
					return false;
				}
				
				function updateTeamPage(team, submitData) {
					switch (submitData.action) {
						case 'add':
							//Remove members from other teams
							for (var i = 0; i < submitData.members.length; i++) {
								var student = studentData[submitData.members[i]];
								$('#teamsTable').find('#member_' + student.id).fadeOut('slow', function(){
									$(this).remove();
								});
							}
							//Add to team table
							var $lastTr = $('#teamsTable tr:last');
							$lastTr.after(
								$(document.createElement('tr'))
									.addClass('teamRow')
									.attr('id', 'team_' + team.id)
									.append(
										$(document.createElement('td'))
											.append(
												$(document.createElement('i')).addClass('fa fa-group fa-black')
											)
									)
									.append(
										$(document.createElement('td'))
											.addClass('teamName')
											.append(
												submitData.name
											)
									)
									.append(
										$(document.createElement('td'))
											.addClass('members')
											.append(
												$(document.createElement('div'))
													.addClass('memberList')
													.append(function(){
														var $spanArray = new Array();
														for (var i = 0; i < submitData.members.length; i++) {
															var student = studentData[submitData.members[i]];
															$spanArray.push(
																$(document.createElement('span'))
																	.addClass('memberName')
																	.attr('id', 'member_' + student.id)
																	.append(
																		$(document.createElement('a'))
																			.addClass('teamStudentLink')
																			.attr('id', 'teamStudent_' + student.id)
																			.attr('href', 'member_' + student.username)
																			.html(student.name)
																	)
															);
														}
														return $spanArray;
													})
											)
									)
									.append(
										$(document.createElement('td'))
											.addClass('supervisor')
											.append(function(){
												var supervisor = facultyData[submitData.supervisor];
												return $(document.createElement('a'))
													.addClass('teamFacultyLink')
													.attr('id', 'teamSupervisor_' + supervisor.id)
													.attr('href', 'supervisor_' + submitData.teamName)
													.html(supervisor.name);
											})
									)
									.append(
										$(document.createElement('td'))
											.addClass('reviewer1')
											.append(function(){
												var reviewer1 = facultyData[submitData.reviewer1];
												return $(document.createElement('a'))
													.addClass('teamFacultyLink')
													.attr('id', 'teamReviewer1_' + reviewer1.id)
													.attr('href', 'reviewer1_' + submitData.teamName)
													.html(reviewer1.name);
											})
									)
									.append(
										$(document.createElement('td'))
											.addClass('reviewer2')
											.append(function(){
												var reviewer2 = facultyData[submitData.reviewer2];
												return $(document.createElement('a'))
													.addClass('teamFacultyLink')
													.attr('id', 'teamReviewer2_' + reviewer2.id)
													.attr('href', 'reviewer2_' + submitData.teamName)
													.html(reviewer2.name);
											})
									)
									.append(
										$(document.createElement('td'))
											.append(
												$(document.createElement('button'))
													.attr('type', 'button')
													.attr('title', 'Edit')
													.addClass('editTeamBtn btn btn-info')
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
							//Add team to student table
							for (var i = 0; i < submitData.members.length; i++) {
								var student = studentData[submitData.members[i]];
								$('#studentUsersTable').find('#user_' + student.id)
									.children('.teamName')
									.empty()
									.append(
										$(document.createElement('a'))
											.addClass('studentTeamLink')
											.attr('id', 'teams_' + team.id)
											.attr('href', 'teams_' + submitData.name)
											.html(submitData.name)
									);
							}
							//Add team to faculty table
							console.log($('#facultyUsersTable').find('#user_' + submitData.supervisor));
							$('#facultyUsersTable').find('#user_' + submitData.supervisor)
								.find('.supervisorMyTeams > .memberList')
								.append(
									$(document.createElement('span'))
										.addClass('memberName')
										.attr('id', 'supervisorMyTeam_' + team.id)
										.append(
										$(document.createElement('a'))
											.addClass('studentTeamLink')
											.attr('id', 'teams_' + team.id)
											.attr('href', 'teams_' + submitData.name)
											.html(submitData.name)
										)
								);
							$('#facultyUsersTable').find('#user_' + submitData.reviewer1)
								.find('.reviewer1MyTeams > .memberList')
								.append(
									$(document.createElement('span'))
										.addClass('memberName')
										.attr('id', 'reviewer1MyTeam_' + team.id)
										.append(
										$(document.createElement('a'))
											.addClass('studentTeamLink')
											.attr('id', 'teams_' + team.id)
											.attr('href', 'teams_' + submitData.name)
											.html(submitData.name)
										)
								);
							$('#facultyUsersTable').find('#user_' + submitData.reviewer2)
								.find('.reviewer2MyTeams > .memberList')
								.append(
									$(document.createElement('span'))
										.addClass('memberName')
										.attr('id', 'reviewer2MyTeam_' + team.id)
										.append(
										$(document.createElement('a'))
											.addClass('studentTeamLink')
											.attr('id', 'teams_' + team.id)
											.attr('href', 'teams_' + submitData.name)
											.html(submitData.name)
										)
								);
							break;
						case 'edit':
							//Edit in team table
							var $teamTr = $('#teamsTable').find('#team_' + submitData.teamId);
							$teamTr.children('.teamName').empty().append(submitData.name);
							$teamTr.find('.memberList').empty().append(function(){
								var $spanArray = new Array();
								for (var i = 0; i < submitData.members.length; i++) {
									var student = studentData[submitData.members[i]];
									$spanArray.push(
										$(document.createElement('span'))
											.addClass('memberName')
											.attr('id', 'member_' + student.id)
											.append(
												$(document.createElement('a'))
													.addClass('teamStudentLink')
													.attr('id', 'teamStudent_' + student.id)
													.attr('href', 'member_' + student.username)
													.html(student.name)
											)
									);
								}
								return $spanArray;
							});
							$teamTr.children('.supervisor').empty().append(function(){
								var supervisor = facultyData[submitData.supervisor];
								return $(document.createElement('a'))
									.addClass('teamFacultyLink')
									.attr('id', 'teamSupervisor_' + supervisor.id)
									.attr('href', 'supervisor_' + submitData.teamName)
									.html(supervisor.name);
							});
							$teamTr.children('.reviewer1').empty().append(function(){
								var reviewer1 = facultyData[submitData.reviewer1];
								return $(document.createElement('a'))
									.addClass('teamFacultyLink')
									.attr('id', 'teamSupervisor_' + reviewer1.id)
									.attr('href', 'supervisor_' + submitData.teamName)
									.html(reviewer1.name);
							});
							$teamTr.children('.reviewer2').empty().append(function(){
								var reviewer2 = facultyData[submitData.reviewer2];
								return $(document.createElement('a'))
									.addClass('teamFacultyLink')
									.attr('id', 'teamSupervisor_' + reviewer2.id)
									.attr('href', 'supervisor_' + submitData.teamName)
									.html(reviewer2.name);
							});
							//Edit in student table
							//Edit in faculty table
							break;
						case 'delete':
							//Delete from student table
							//Delete from faculty table
							//Delete from team table
							break;
						default:
							console.log('Action: ' + submitData.action);
							showNotification('ERROR', 'Unknown action');
							return false;
					}
				}
				
				/*******************/
				/* PLUGINS          */
				/*******************/
				
                /* TOKEN INPUT */
                function appendTokenInput($input, userType, action){
                    $input.tokenInput('destroy');
                    var opts = {
                        preventDuplicates: true,
                        theme: "facebook",
                        allowTabOut: true,
                        propertyToSearch: "fullName",
                        resultsLimit: 4,
                        hintText: "Enter name...",
                        noResultsText: function(){
							return userType.toUpperCase() + ' does not exist';
						}
                    };
                    switch (action) {
						case 'add':
							break;
						case 'edit':
							//Prepopulate
							break;
						default:
							showNotification('Team action does not exist');
							return false;
					}
                    $input.tokenInput(function() {
						switch (userType.toUpperCase()) {
							case 'STUDENT':
								return studentData;
								break;
							case 'FACULTY':
								return facultyData;
								break;
							default:
								showNotification('Team userType does not exist');
								return false;
						}
					}, opts);
                }

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
