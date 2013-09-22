<%-- 
    Document   : adminconfig
    Created on : Sep 21, 2013, 9:21:45 PM
    Author     : Prakhar
--%>

<%@page contentType="text/html" pageEncoding="windows-1252"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=windows-1252">
        <title>Admin Config</title>
		 <style type="text/css">
            table {
                table-layout: fixed;
            }
            
            #createTermTable, #createScheduleTable, #createTimeslotsTable {
                margin-left: 20px;
            }

            th {
                font-size: 18px;
                height: 25px;
                padding: 10px;
                text-align: center;
                /*border-bottom: 1px solid black;*/
            }

            td {
                padding: 10px;
                text-align: left;
            }

            .formLabelTd {
                font-size: 16px;
                color: darkblue;
                padding-bottom: 20px;
            }

            .submitBtnRow {
                border-bottom: none;
            }

            #allChkRow {
                border-bottom: 1px solid black;
            }

            #timeColumn {
                border-right: 1px solid black;
            }
			
            .adminConfigTabList {
                position: relative;
                /*padding-top: 50px;*/
                height: 100%;
            }
            
            .createScheduleTab {
                width: 180px;
            }
            
            .adminConfigTabList li a, .adminConfigTabList li p {
                font-size: 20px;
                font-weight: bold;
                padding: 20px 10px 20px 10px !important;
            }
            
            .adminPanel {
                padding-left: 5%;
            }
            
            html, body, .container {
                height: 100%;
            }
            
            .adminLeftNav {
                height: 100%;
            }
            
            .tab-content {
                padding-top: 50px;
            }
			
			input {
				margin-top: -5px;
			}
		</style>
    </head>
    <body>
		<!-- Navbar -->
        <%@include file="navbar.jsp" %>

        <!-- Kick unauthorized user -->
        <%
            if (activeRole != Role.ADMINISTRATOR && activeRole != Role.COURSE_COORDINATOR) {
                request.setAttribute("error", "You need administrator privileges for this page");
                RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
                rd.forward(request, response);
            }
        %>
        
        <div id="adminConfigPage" class="container">
            
            <div class="adminConfigTabList tabbable tabs-left">
                <ul class="adminLeftNav nav nav-tabs">
					<li class="emptyHiddenTab">
                        <p></p>
                    </li>
                    <li class="manageMilestones active">
                        <a href="#manageMilestonesTab" data-toggle="tab">Manage Milestones</a>
                    </li>
                    <li class="manageTerms">
                        <a href="#manageTermsTab" data-toggle="tab">Manage Terms</a>
                    </li>
					<li class="csvUpload">
                        <a href="#csvUploadTab" data-toggle="tab">CSV Upload</a>
                    </li>
					<li class="manageNotifications">
                        <a href="#manageNotificationsTab" data-toggle="tab">Manage Notifications</a>
                    </li>
                </ul>
				
                <div class="tab-content">
					<!-- For Manage Milestones -->
                    <div class="tab-pane active" id="manageMilestonesTab">
						<div id="manageMilestonesPanel" class="adminPanel">
							<h3 id="createTimeslotsTitle">Manage Milestone Settings</h3>
						</div>
                    </div>
					<!-- For Manage Active Terms -->
                    <div class="tab-pane" id="manageTermsTab">
                        <!-- Create Timeslots -->
                        <div id="createTimeslotsPanel" class="adminPanel">
                            <h3 id="createTimeslotsTitle">Manage Active Terms</h3>
                        </div>
                    </div>
					<!-- CSV Upload -->
					<div class="tab-pane" id="csvUploadTab">
                        <!-- Create Timeslots -->
                        <div id="createTimeslotsPanel" class="adminPanel">
                            <h3 id="createTimeslotsTitle">CSV Upload</h3>
                        </div>
                    </div>
					<!-- Manage Notifications -->
					<div class="tab-pane" id="manageNotificationsTab">
                        <!-- Create Timeslots -->
                        <div id="createTimeslotsPanel" class="adminPanel">
                            <h3 id="createTimeslotsTitle">Manage Notifications</h3>
                        </div>
                    </div>
                </div>
            </div>

        </div>
        <%@include file="footer.jsp" %>
        <script type="text/javascript">
				/**********************/
				/*   NOTIFICATIONS    */
				/**********************/
				
				function showNotification(action, message) {
					var opts = {
					   title: "Note",
					   text: message,
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
						case "WARNING":
							opts.type = "warning";
							opts.title = "Note";
							break;
						case "SUCCESS":
						   opts.type = "success";
						   opts.title = "Created";
						   break;
						case "ERROR":
						   opts.type = "error";
						   opts.title = "Warning";
						   break;
						default:
							alert("Something went wrong");
					}
				   $.pnotify(opts);
				}
				
            };
            
            addLoadEvent(createScheduleLoad);
        </script>
    </body>
</html>