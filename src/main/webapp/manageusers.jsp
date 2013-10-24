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
												<td class='fullName'><s:property value="name"/></td>
												<td class='username'><s:property value="username"/></td>
												<td class='mobileNumber'><s:property value="mobileNumber"/></td>
												<td class='teamName'><a class='studentTeamLink' id='teams_<s:property value="teamId"/>' href='team_<s:property value="teamName"/>'><s:property value="teamName"/></a></td>
												<td class='editTd'>
													<button type='button' title="Edit" class='modBtn editBtn btn btn-info'>
														<i class='icon-pencil icon-white'></i>
													</button>
												</td>
												<td class='deleteTd'>
													<button type='button' title="Delete User" class='modBtn delBtn btn btn-danger'>
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
									<tr><th></th><th>Name</th><th>Username</th><th>Phone</th><th>Teams</th><th>Edit</th><th>Delete</th></tr>
								</thead>
								<tbody>
									<s:if test="%{facultyData != null && facultyData.size() > 0}">
										<s:iterator var="faculty" value="facultyData">
											<tr id='user_<s:property value="id"/>' class='facultyRow'>
												<td><i class='icon-user icon-black'></i></td>
												<td class='fullName'><s:property value="name"/></td>
												<td class='username'><s:property value="username"/></td>
												<td class='mobileNumber'><s:property value="mobileNumber"/></td>
												<td>
													<a class='facultyTeamsLink' id='teams_<s:property value="username"/>' href='faculty_<s:property value="username"/>'><s:property value="%{#faculty.myTeams.size()}"/></a>
												</td>
												<td>
													<button type='button' title="Edit" class='modBtn editBtn btn btn-info'>
														<i class='icon-pencil icon-white'></i>
													</button>
												</td>
												<td>
													<button type='button' title="Delete User" class='modBtn delBtn btn btn-danger'>
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
									<tr><th></th><th>Name</th><th>Username</th><th>Phone</th><th>Signups</th><th>Edit</th><th>Delete</th></tr>
								</thead>
								<tbody>
									<s:if test="%{taData != null && taData.size() > 0}">
										<s:iterator var="ta" value="taData">
											<tr id='user_<s:property value="id"/>' class='taRow'>
												<td><i class='icon-user icon-black'></i></td>
												<td class='fullName'><s:property value="name"/></td>
												<td class='username'><s:property value="username"/></td>
												<td class='mobileNumber'><s:property value="mobileNumber"/></td>
												<td>
													<a class='taSignupsLink' id='signups_<s:property value="username"/>' href='ta_<s:property value="username"/>'><s:property value="%{#ta.mySignups.size()}"/></a>
												</td>
												<td>
													<button type='button' title="Edit" class='modBtn editBtn btn btn-info'>
														<i class='icon-pencil icon-white'></i>
													</button>
												</td>
												<td>
													<button type='button' title="Delete User" class='modBtn delBtn btn btn-danger'>
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
					<!-- Teams -->
					<h4 id='teamTitle'>Teams</h4>
					<table id='teamsTable' class='subUsersTable table table-hover zebra-striped'>
						<thead>
							<tr><th>Team</th></tr>
						</thead>
						<tr>
							<td class='teamItemKey'>Name</td>
							<td class='teamItemValue'>
								<s:if test="%{teamData != null && teamData.size() > 0}">
									<select name='teamSelect' id='teamSelect'>
										<option id='teamSelectHeader' value='-1' selected>Select Team</option>
										<s:iterator var="team" value="teamData">
											<option value='<s:property value="teamName"/>'><s:property value="teamName"/></option>
										</s:iterator>
									</select>
								</s:if>
								<s:else>
									<b>No Teams Set!</b>
								</s:else>
							</td>
							<td>
								<button type='button' title='Add New Team' id='addTeamBtn' class='teamBtn btn btn-primary'>
									<i class='icon-plus icon-white'></i>
								</button>
							</td>
						</tr>
						<tr>
							<td class='teamItemKey'>Members</td>
							<td class='teamItemValue teamMembers'>
								<div class='memberList'>
									<span class='memberName'>Abhilash Murthy</span>
									<span class='memberName'>Suresh Subramaniam</span>
								</div>
							</td>
							<td class='teamBtns'>
								<button type='button' title="Edit Team" class='teamBtn btn btn-info'>
									<i class='icon-pencil icon-white'></i>
								</button>
								<button type='button' title="Delete Team" class='modBtn delBtn teamBtn btn btn-danger'>
									<i class='icon-trash icon-white'></i>
								</button>
							</td>
						</tr>
						<tr>
							<td class='teamItemKey'>Supervisor</td>
							<td class='teamItemValue teamSupervisor'>Richard Davis</td>
						</tr>
						<tr>
							<td class='teamItemKey'>Reviewer 1</td>
							<td class='teamItemValue teamReviewer1'>Youngsoo Kim</td>
						</tr>
						<tr>
							<td class='teamItemKey'>Reviewer 2</td>
							<td class='teamItemValue teamReviewer2'>Cheok Lai Tee</td>
						</tr>
					</table>
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
						<td class='fullName'><s:property value="name"/></td>
						<td class='username'><s:property value="username"/></td>
						<td class='mobileNumber'><s:property value="mobileNumber"/></td>
						<td>
							<button type='button' title="Edit" class='modBtn editBtn btn btn-info'>
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
						<td class='fullName'><s:property value="name"/></td>
						<td class='username'><s:property value="username"/></td>
						<td class='mobileNumber'><s:property value="mobileNumber"/></td>
						<td>
							<button type='button' title="Edit" class='modBtn editBtn btn btn-info'>
								<i class='icon-pencil icon-white'></i>
							</button>
						</td>
						<td>
							<button type='button' title="Delete User" class='modBtn delBtn btn btn-danger'>
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
				/*  NAVIGATION */
				/***************/
				
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
				
				//Team Select Dropdown
				$('#teamSelect').on('change', function(){
					var selectedTeam = $(this).val();
					$('.teamBtns').hide();
					$('.teamMembers').empty();
					$('.teamSupervisor').empty();
					$('.teamReviewer1').empty();
					$('.teamReviewer2').empty();
					for (var i = 0; i < teamData.length; i++) {
						if (teamData[i].teamName === selectedTeam) {
							var team = teamData[i];
							$('.teamMembers').append(
								$(document.createElement('div'))
									.addClass('memberList')
									.append(function() {
											var memberSpans = new Array();
											for (var j = 0; j < team.members.length; j++) {
												memberSpans.push($(document.createElement('span')).addClass('memberName').html(team.members[j].name));
											}
											return memberSpans;
										}
									)
							);
							$('.teamSupervisor').append(team.supervisor.name);
							$('.teamReviewer1').append(team.reviewer1.name);
							$('.teamReviewer2').append(team.reviewer2.name);
							$('.teamBtns').show();
							return false;
						}
					}
					return false;
				});
				
				//Team Name Link
				$('body').on('click', '.studentTeamLink', function(){
					$('.modal').modal('hide');
					$('#teamSelect').closest('tr').effect('highlight', {}, 1500);
					$('#teamSelect').val($(this).text()).change();
					return false;
				});
				
				//Faculty Teams Link
				$('body').on('click', '.facultyTeamsLink', function(){
					var username = $(this).attr('id').split("_")[1];
					var faculty = null;
					for (var i = 0; i < facultyData.length; i++) {
						if (facultyData[i].username === username) {
							faculty = facultyData[i];
							break;
						}
					}
					bootbox.alert({
						title: 'Teams of ' + faculty.name,
						message: function() {
							return $(document.createElement('table')).addClass('modalTable').append(function(){
								if ($.isEmptyObject(faculty.myTeams)) {
									return $(document.createElement('tr')).append($(document.createElement('td')).html('No teams assigned!'));
								}
								var $trs = new Array();
								$trs.push(
									$(document.createElement('tr'))
										.append($(document.createElement('th')).html('Team'))
										.append($(document.createElement('th')).html('Role'))
								);
								for (var key in faculty.myTeams) {
									if (faculty.myTeams.hasOwnProperty(key)) {
										$trs.push(
											$(document.createElement('tr'))
												.append(
													$(document.createElement('td')).append(
														$(document.createElement('a'))
															.addClass('studentTeamLink')
															.html(key)
													)
												)
												.append(
													$(document.createElement('td')).append(function(){
														var roles = '';
														var myRoles = faculty.myTeams[key].myRoles;
														for (var i = 0; i < myRoles.length; i++) {
															roles += myRoles[i] + ', ';
														};
														return roles.substring(0, roles.length - 2);
													})
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
				/*  BUTTONS    */
				/***************/
				
				$('.modBtn').on('click', function(){
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
						case 'cc':
							for (var i = 0; i < ccData.length; i++) {
								if (parseInt(ccData[i].id) === parseInt(id)) {
									user = ccData[i];
									break;
								}
							}
							break;
						default:
							showNotification("ERROR", 'User not found!');
							return false;
					}
					editableFields.push({order: 1, key: "Username", name:"username", value: user.username});
					editableFields.push({order: 2, key: "Full Name", name:"fullName", value: user.name});
					if ($this.hasClass('editBtn')) editUser(user, userType, editableFields);
					else if ($this.hasClass('delBtn')) {
						deleteUser(user, userType);
					}
				});
				
				/*******************/
				/*  ACTIONS        */
				/*******************/
				
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
						title: "Edit User",
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
								var submitData  = {
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
										updateJsonData(user, userType.toUpperCase(), submitData);
										updatePage(user, userType.toUpperCase(), submitData);
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
										updateJsonData(user, userType.toUpperCase(), submitData);
										updatePage(user, userType.toUpperCase(), submitData);
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
				
				function updateJsonData(user, userType, submitData) {
					var roleData = null;
					switch (userType) {
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
							break;
						case 'edit':
							//Modify teamData
							if (submitData.teamId && parseInt(user.teamId) !== parseInt(submitData.teamId)) {
								console.log('changed team');
								for (var i = 0; i < teamData.length; i++) {
									var members = teamData[i].members;
									for (var j = 0; j < members.length; j++) {
										if (parseInt(user.id) === parseInt(members[j].id)) {
											//Remove from old team
											members.splice(members.indexOf(members[j]), 1);
											console.log('remove from ' + teamData[i].teamName);
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
										console.log('add to ' + teamData[i].teamName);
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
							showNotification('ERROR', 'Unkown userType');
							return false;
					}
				}
				
				function updatePage(user, userType, submitData) {
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
					$('#teamSelect').val(-1).change();
				}
				
				/*******************/
				/*  NOTIFICATIONS  */
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
