<%@page import="model.Term"%>
<%@page import="constant.Role"%>
<%@page import="model.Team"%>
<%@page import="util.MiscUtil"%>
<%@ taglib prefix= "s" uri= "/struts-tags" %>

<!DOCTYPE html>
<html>
    <head>
        <%@include file="header.jsp" %>
        <title>IS480 Scheduling System | Welcome</title>
        <% 
			if (session.getAttribute("user") != null && !((User) session.getAttribute("user")).getUsername().equals("_")) {
				response.sendRedirect("index");
				return;
			}
			Term activeTerm = (Term) session.getAttribute("currentActiveTerm");
		%>
		<%@include file="imports.jsp" %>
        <style type="text/css">
			
			.testBtn {
				margin-right: 20px;
			}
			
			.legend {
				margin-bottom: 10px;
			}
			
			.loginTitle {
				width: 100%;
				text-align: center;
			}
			
			.page {
				border-radius: 10px;
				background-color: #fafafa;
				/*padding: 20px;*/
			}
			
			.scheduleContainer {
				margin-top: 0;
			}
			
			#milestoneTab {
				margin-top: 20px;
			}

        </style>
    </head>
    <body>
        <!-- Navigation Login -->
        <div class="navbar navbar-inverse navbar-fixed-top">
            <div class="navbar-inner">
                <div class="container">
					<a class="brand" href="index">
						<img src="img/IS480-navbar.png" style="height:21px; width:100px; display:inline-block;"/>
					</a>
					<button class="ssoBtn btn btn-primary pull-right" data-loading-text="Logging in..." type="submit">SMU Login</button>
					<button class="testBtn btn btn-inverse pull-right muted" data-loading-text="Logging in..." type="submit">Administrator Login</button>
                </div>
            </div>
        </div>
		
        <div class="container page">
			<div class="loginTitle muted"><h3>IS480 Scheduling - Please <a class="ssoBtn" href="#">login</a> to use the system</h3></div>
			<div class='termPicker'>
				<div class="btn-group" style="float: left;">
					<s:iterator var="term" value="termData">
						<s:if test= "%{selectedTermId == #term.termId}">
							<a class="btn btn-large dropdown-toggle" data-toggle="dropdown" href="#">
								<b><s:property value= "#term.termName"/></b> <span class="caret"></span>
							</a>
						</s:if>
					</s:iterator>
					<ul class="dropdown-menu">
						<s:iterator var= "term" value= "termData">
							<s:if test= "%{selectedTermId != #term.termId}">
								<li>
									<button type="submit" class="selectTermBtn btn btn-link" name="termId" value="<s:property value="#term.termId"/>">
										<s:property value="#term.termName"/>
									</button>
								</li>
							</s:if>
						</s:iterator>
					</ul>
				</div>
			</div>
        </div>

        <!-- Main schedule navigation -->
		<div class="scheduleContainer container page">
			<!-- To display legend for the calendar -->
			<table class="legend">
				<tr>
					<td class="legendBox unbookedTimeslot" style="border-width:1px!important;width:17px;"></td><td>&nbsp;Available</td>
					<td style="width:15px"></td>
					<td class="legendBox pendingBooking" style="border-width:1px!important;width:17px;"></td><td>&nbsp;Pending</td> 
					<td style="width:15px"></td>
					<td class="legendBox approvedBooking" style="border-width:1px!important;width:17px;"></td><td>&nbsp;Approved</td> 
				</tr>
			</table>
			<ul id="milestoneTab" class="nav nav-tabs">
				<!-- milestone tabs populated dynamically -->
			</ul>
			<div id="milestoneTabContent" class="tab-content" hidden="">
				<!-- milestone tables populated dynamically -->
			</div>
			<div id="scheduleProgressBar" class="progress progress-striped active">
				<div class="bar" style="width: 100%;"></div>
			</div>
		</div>
		
        <%@include file="footer.jsp"%>
        <script type="text/javascript">
			var welcomeLoad = function() {
				/* SCHEDULELOAD */
                var milestone = "ACCEPTANCE";
                var year = "<%= activeTerm.getAcademicYear()%>";
                var semester = "<%= activeTerm.getSemester()%>";
                var scheduleData = null; //This state shall be stored here
                var weekView = null;
				var maxWeekView = null;
				var self = null;
				
				populateMilestones();
                function populateMilestones() {
                    var milestones = getScheduleData(null, year, semester).milestones;
					//Sort milestones by order
                    function compare(a, b) {
                        if (a.milestoneOrder < b.milestoneOrder) {
                            return -1;
                        } else if (a.milestoneOrder > b.milestoneOrder) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
					milestones.sort(compare);
					var setAsActive = true;
                    for (var i = 0; i < milestones.length; i++) {
                        var thisMilestone = milestones[i];
						if (!thisMilestone.bookable) continue;
						$('ul#milestoneTab') //Add the milestone tab
							.append(
								$(document.createElement('li'))
									.addClass(setAsActive?'active':'')
									.append(
										$(document.createElement('a'))
											.attr('id', thisMilestone.name.toLowerCase())
											.attr('href', '#' + thisMilestone.name.toLowerCase())
											.attr('data-toggle', 'tab')
											.html(thisMilestone.name)
									)
							);
						$('div#milestoneTabContent') //Add the milestone table
							.append(
								$(document.createElement('div'))
									.attr('id', thisMilestone.name.toLowerCase() + "Content")
									.addClass('tab-pane fade')
									.addClass(setAsActive?'active in':'')
									.append(
										$(document.createElement('table'))
											.attr('id', thisMilestone.name.toLowerCase() + "ScheduleTable")
											.addClass('scheduleTable table-condensed table-hover table-bordered')
									)
							);
						milestone = setAsActive?thisMilestone.name.toUpperCase():milestone;
						setAsActive = false;
                    }
                }
				
				populateSchedule(milestone, year, semester);
                function getScheduleData(milestone, year, semester) {
                    var toReturn = null;
                    var data = {
						milestone: milestone,
                        year: year,
                        semester: semester
                    };
                    $.ajax({
                        type: 'GET',
                        data: data,
                        async: false,
                        url: 'getSchedule',
                        cache: false,
                        dataType: 'json'
                    }).done(function(response) {
                        if (response.success) {
                            toReturn = response;
                        } else {
                            var eid = btoa(response.message);
                            window.location = "error.jsp?eid=" + eid;
                        }
                    }).fail(function(error) {
                        toReturn = "AJAX fail";
                    });
                    return toReturn;
                }
				
                function populateSchedule(milestone, year, semester) {
                    $(".scheduleTable").empty();
					$(".timeslotCell").remove();
                    $("#milestoneTabContent").hide();
                    $("#scheduleProgressBar").show();
                    scheduleData = getScheduleData(milestone, year, semester);
                    if (scheduleData.success) {
                        convertScheduleData();
                        renderSchedule();
						setTimeout(function(){renderTimeslots();}, 0);
						setTimeout(function(){appendPopovers();}, 0);	
                        setTimeout(function(){setupMouseEvents();}, 0);
                    } else {
                        var eid = btoa(scheduleData.message);
                        window.location = "error.jsp?eid=" + eid;
                    }
                    $("#scheduleProgressBar").hide();
                    $("#milestoneTabContent").show();
                }
				
                function convertScheduleData() {
                    var timeslots = scheduleData.timeslots;
                    var newTimeslots = {};
                    for (var i = 0; i < timeslots.length; i++) {
                        var timeslot = timeslots[i];
                        var key = timeslot.datetime;
                        newTimeslots[key] = timeslot;
                    }
                    scheduleData["timeslots"] = newTimeslots;
                }
				
				function renderSchedule() {
                    var tableClass = "scheduleTable:first";
                    var minTime = scheduleData.dayStartTime;
                    var maxTime = scheduleData.dayEndTime;

                    var timesArray = new Array();
                    for (var i = minTime; i < maxTime; i++) {
                        var timeVal = Date.parse(i + ":00:00");
                        timesArray.push(timeVal.toString("HH:mm:ss"));
                        timeVal.addMinutes(30);
                        timesArray.push(timeVal.toString("HH:mm:ss"));
                    }
                    
                    var datesArray = getDateArrayBetween(scheduleData.startDate, scheduleData.endDate, weekView);
                    //Append header names
					$("." + tableClass)
						.append(
							$(document.createElement('thead'))
								.append(
									$(document.createElement('tr'))
										.attr('id', 'scheduleHeader')
										.append($(document.createElement('td')))
										.append(function(){
											var $tdCollection = new Array();
											for (var i = 0; i < datesArray.length; i++) {
												$tdCollection.push(
													$(document.createElement('td'))
														.addClass('dateCol')
														.attr('id', 'col_' + (i + 1))
														.html(new Date(datesArray[i]).toString('dd MMM') + "<br/>" + new Date(datesArray[i]).toString('ddd'))
												);
											}
											return $tdCollection;
										})
								)
						);
					
					//Append body data 2.0
					$("." + tableClass)
						.append(function(){
							var $trCollection = new Array();
							for (var i = 0; i < timesArray.length; i++) {
								$trCollection.push(
									$(document.createElement('tr'))
										.append(i%2 === 0?
											$(document.createElement('td')) //Time display cell
											.addClass('timeDisplayCell')
											.attr('rowspan', '2')
											.html(timesArray[i].substring(0, 5))
											:false
										)
										.append(function(){
											var $tdCollection = new Array();
											for (var j = 0; j < datesArray.length; j++) {
												var datetime = new Date(datesArray[j]).toString("yyyy-MM-dd") + " " + timesArray[i];
												$tdCollection.push(
													$(document.createElement('td'))
														.addClass('tdCell')
														.addClass(i%2 === 0?'tdUpper':'tdLower')
														.attr('value', datetime)
												);
											}
											return $tdCollection;
										})
								);
							}
							return $trCollection;
						});
					return false;
                }
				
				function renderTimeslots() {
					var timeslots = scheduleData.timeslots;
					for (var key in timeslots) {
						if (timeslots.hasOwnProperty(key)) {
							var timeslot = timeslots[key];
							var $tdCell = $('body').find('td.tdCell[value="' + timeslot.datetime + '"]');
							var $timeslot = $(document.createElement('div'))
								.addClass('timeslotCell')
								.attr('id', 'timeslot_' + timeslot.id)
								.attr('align', 'center')
								.attr('value', timeslot.datetime)
								.css ({
									height: ($tdCell.innerHeight() / 1.1 * (scheduleData.duration / 30)),
									width: $tdCell.outerWidth() / 1.1
								})
								.offset({
									top: $tdCell.offset().top,
									left: $tdCell.offset().left
								})
								.addClass(timeslot.team?'bookedTimeslot':'unbookedTimeslot')
								.append(timeslot.team?
									$(document.createElement('div'))
										.addClass('booking pendingBooking myTeamBooking')
										.addClass(timeslot.status.toLowerCase() + 'Booking')
										.css ({
											height: ($tdCell.innerHeight() / 1.1 * (scheduleData.duration / 30)),
											width: $tdCell.outerWidth() / 1.1
										})
										.html(timeslot.team)
								:false);
							$('body').append($timeslot);
						}
					}
				}
				
                function getDateArrayBetween(startDate, stopDate, weekNum) {
                    var dateArray = new Array();
                    startDate = Date.parse(startDate);
                    stopDate = Date.parse(stopDate);
					var diffDays = Math.ceil(Math.abs(stopDate.getTime() - startDate.getTime()) / (1000 * 3600 * 24));
					maxWeekView = diffDays%7 === 0?(diffDays/7) + 1:Math.ceil(diffDays/7);
                    if (weekNum !== null) {
                        startDate.addWeeks(weekNum);
                        stopDate = startDate.clone().addWeeks(1).addDays(-1);
                    }
                    var currentDate = startDate;
                    while (currentDate <= stopDate) {
                        dateArray.push(currentDate);
                        currentDate = new Date(currentDate).addDays(1);
                    }
                    return dateArray;
                }

                function appendPopovers() {
                    $('#milestoneTabContent').hide();
                    $('#scheduleProgressBar').show();
                    $('.timeslotCell').trigger('mouseleave').popover('destroy');

                    //Add View Booking popovers
                    $('.bookedTimeslot').each(function() {
                        appendViewBookingPopover($(this));
                    });

                    //Show progress bar
                    $('#scheduleProgressBar').hide();
                    $('#milestoneTabContent').show();
                }

                function setupMouseEvents() {
					$('.timeslotCell').mouseleave(function() {
                        $(this).removeClass('clickedCell');
                    });
                    
                    $('body').off('click', '.timeslotCell, .booking');
                    $('body').on('click', '.timeslotCell, .booking', function(e) {
						self = $(this).children('.booking').length?$(this).children('.booking'):$(this)
						$('.timeslotCell, .booking').not(self).not(self.parents()).find('#updateBookingBtn').attr('disabled', true);
						$('.timeslotCell, .booking').not(self).not(self.parents()).find('#updateTimeslotBtn').attr('disabled', true);
						$('.timeslotCell, .booking').not(self).popover('hide');
						$(".hasDatepicker").datepicker('destroy');
                        return false;
                    });

                    $('body').off('click', '.bookedTimeslot:not(.unavailableTimeslot), .bookedTimeslot > .booking');
                    $('body').on('click', '.bookedTimeslot:not(.unavailableTimeslot), .bookedTimeslot > .booking', function(e) {
						if (e.target === this) {
							self = ($(this).is('.booking')) ? $(this) : $(this).children('.booking');
							var timeslot = scheduleData.timeslots[self.closest('.timeslotCell').attr('value')];
							self.popover('show');
							if (self.find('tr:last').length && self.find('tr:last').offset().top - $(window).scrollTop() > window.innerHeight){
								$('body').animate({scrollTop: self.find('tr:last').offset().top - $(window).scrollTop()}, 500);
							}
							self.find("#updateFormDate").val(timeslot.startDate).change();
							self.find("#updateFormStartTime").val(timeslot.time).change();
							self.find('ul').remove();
						} else if ($(e.target).attr('id') === 'wikiLink') {
							window.open($(e.target).attr('href'), '_blank');
						}
                        return false;
                    });
					
                    $('body').off('click', '.unbookedTimeslot');
                    $('body').on('click', '.unbookedTimeslot', function(e) {
                        $('button.ssoBtn').effect('highlight', {color: "#ffff99 !important"}, 1500);
						return false;
                    });

                    $('.timeslotCell').off('click', '.close');
                    $('.timeslotCell').on('click', '.close', function(e) {
                        e.stopPropagation();
                        self.popover('hide');
                        self.trigger('mouseleave');
                        return false;
                    });
                }
                
                function appendViewBookingPopover($td) {
                    var timeslot = scheduleData.timeslots[$td.attr('value')];					
                    var $bookingDetailsTable = $(document.createElement('table'));
                    $bookingDetailsTable.attr('id', 'viewTimeslotTable');
					$bookingDetailsTable.addClass('table-condensed table-hover table-bordered');
					var outputData = {
						Team: timeslot.wiki ? '<a id="wikiLink" href="' + timeslot.wiki + '">' + timeslot.team + '</a>':timeslot.team,
						Status: timeslot.status,
						Date: timeslot.startDate,
						Time: timeslot.time,
						Venue: timeslot.venue,
						Students: function(){
							var studentList = '';
							for (var i = 0; i < timeslot.students.length; i++) {
								studentList += (timeslot.students[i].name + "<br/>");
							}
							return studentList;
						},
						Faculty: function(){
							var facultyList = '';
							for (var i = 0; i < timeslot.faculties.length; i++) {
								facultyList += (timeslot.faculties[i].name + " " + (timeslot.faculties[i].status.toLowerCase() ==='pending'?$(document.createElement('i')).addClass('fa fa-cog fa-spin muted').outerHTML():$(document.createElement('i')).addClass('fa fa-check').css('color', '#A9DBA9').outerHTML()) + "<br/>");
							}
							return facultyList;
						},
						TA: timeslot.TA
					};

                    //Append all fields
					for (var key in outputData) {
						if (outputData.hasOwnProperty(key)) {
							$bookingDetailsTable
								.append(
									$(document.createElement('tr'))
										.append($(document.createElement('td')).html(key))
										.append($(document.createElement('td')).html(outputData[key]))
								);
						}
					}

                    //Popover
                    makePopover($td.children('.booking'), "<b>Team Booking</b>", $bookingDetailsTable);
                }
				
				/* BUTTONS */
				
				function getParameterByName(name) {
					name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
					var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
						results = regex.exec(location.search);
					return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
				}
				
                //Function to change schedule based on selected milestone tab
                $('#milestoneTab a').on('click', function(e) {
                    $("#milestoneTab").removeClass('active in');
                    $(this).tab('show');
                    milestone = $(this).attr('id').toUpperCase();
                    year = "<%= activeTerm.getAcademicYear()%>";
                    semester = "<%= activeTerm.getSemester()%>";
                    populateSchedule(milestone, year, semester);
					$("#weekView").bootstrapSwitch('setState', true);
                    return false;
                });

				$(".testBtn").click(function() {
					window.location = "adminlogin.jsp";
					return false;
				});

				$(".ssoBtn").click(function() {
					if ($(this).is('button')) $(this).button('loading');
					//blink(this);
					window.location = 'https://elearntools.smu.edu.sg/Tools/SSO/login.ashx?id=IS480PSAS';
					return false;
				});

				$('.selectTermBtn').on('click', function(){
					window.location = "welcome?t=" + btoa($(this).val());
					return false;
				});
				
				$('body').on('click', function(e) {
					$('.popover.in').each(function(e){
						var self = $(this);
						self.parent().find('#updateBookingBtn').attr('disabled', true);
						self.parent().find('#updateTimeslotBtn').attr('disabled', true);
						self.parent().popover('hide');
						$(".hasDatepicker").datepicker('destroy');
					});
				});    
				
				/* PLUGINS */
				
				/* POPOVER */
				function makePopover(container, title, content) {
					container.popover({
						container: container,
						trigger: 'manual',
						html: true,
						title: function(){
							if (!title) return false;
							return title + $(document.createElement('button')).addClass('close').append($(document.createElement('i')).addClass('fa fa-times fa-black')).outerHTML();

						},
						content: content,
						placement: function(){
							if (container.parents("tr").children().index(container.closest(".timeslotCell")) > 7) {
								return 'left';
							} else {
								return 'right';
							}
						}
					});
				}
				
			};
			
			addLoadEvent(welcomeLoad);
        </script>
    </body>
</html>