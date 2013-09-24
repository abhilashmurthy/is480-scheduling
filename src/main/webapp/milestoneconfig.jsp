<%-- 
    Document   : milestoneconfig
    Created on : Aug 23, 2013, 10:09:25 PM
    Author     : Prakhar
--%>

<!-- Booking History page -->
<%@page import="com.opensymphony.xwork2.ActionContext"%>
<%@page import="com.opensymphony.xwork2.util.ValueStack"%>
<%@page import="model.*"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@page contentType="text/html" pageEncoding="windows-1252"%>
<!DOCTYPE html>
<html>
    <head>
        <%@include file="header.jsp" %>
        <title>IS480 Scheduling System | Milestones </title>

        <%@include file="footer.jsp"%>
    </head>
    <body>
        <%@include file="navbar.jsp" %>
        <div class="container">
            <h3>Milestone Configuration</h3>

            <!-- Kick unauthorized user -->
            <%	if (!activeRole.equals(Role.ADMINISTRATOR) && !activeRole.equals(Role.COURSE_COORDINATOR)) {
                    request.setAttribute("error", "Oops. You are not authorized to access this page!");
                    RequestDispatcher rd = request.getRequestDispatcher("error.jsp");
                    rd.forward(request, response);
                }

                int counter = 1;
                int counter2 = counter;
            %>

            <!-- SECTION: Booking History -->
            <s:if test="%{data.size() > 0 && data != null}"> 
                <table id="milestoneConfigTable" class="table table-hover zebra-striped" cellspacing="0">
                    <thead>
                        <tr>
                            <th style="width: 5px;">Order</th>
                            <th style="width: 10px;">Milestone</th>
                            <th style="width: 5px;">Duration (Mins)</th>
                            <th style="width: 100px;">Required Attendees</th>
                            <th style="width: 10px;"></th>
                            <th style="width: 10px;"></th>
                        </tr>
                    </thead>
                    <tbody> 
                        <s:iterator value="data">
                            <tr id="id<%=counter%>">
                                <td>
                                    <div class="input-append">
                                        <input id="orderNumber<%=counter%>" style="width: 18px;height: 20px" type="text" name="orderNumber" value="<s:property value="order"/>" disabled />
                                        <div class="btn-group">
                                            <button class="btn" type="button" onclick="upOne(document.getElementById('orderNumber<%=counter%>'));" >&#9650;</button>
                                            <button class="btn" type="button" onclick="downOne(document.getElementById('orderNumber<%=counter%>'));" >&#9660;</button>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <input type='text' id="milestone<%=counter%>" style="width:90px; height:20px" value='<s:property value="milestone"/>' />

                                </td>
                                <td style="width:90px">

                                    <div class="input-append">
                                        <input type='text' style="width:30px; height:20px" cellspacing='0' id='duration<%=counter%>' value='<s:property value="duration" />' disabled/>
                                        <button class="btn" type="button" onclick="upOne(document.getElementById('duration<%=counter%>'));" >&#9650;</button>
                                        <button class="btn" type="button" onclick="downOne(document.getElementById('duration<%=counter%>'));" >&#9660;</button>
                                    </div>
                                </td>
                                <td>
                                    <div id="textarea<%=counter%>" class="input-append">
                                        <s:iterator value="attendees">
                                            <div>
                                                <input type='text' style="width: 80px;height: 20px;" cellspacing='0' id='name<%=counter2%>' value='<s:property value="attendee" />' disabled/>

                                                <button id="delete" title="Delete Attendee" style="width: 40px;height: 30px;" class="btn" onclick="deleteInput(name<%=counter2%>);
                                                    $(this).remove();"><i class="icon-black icon-minus-sign"></i></button>
                                            </div>

                                            <% counter2++;%>
                                        </s:iterator>
                                    </div> 

                                    <div>
                                        <button class="btn" title="Add New Attendee" style="width:40px; height:25px;" onclick="createInput(document.getElementById('textarea<%=counter%>'));">
                                            <i class="icon-black icon-plus-sign"></i> 
                                        </button>
                                    </div>
                                </td>
                                <td>
                                    <button type="button" title="Delete Milestone" class="btn btn-danger" id="<%=counter%>" onclick='deleteRow(<%=counter%>);'>
                                        <i class='icon-trash icon-white'></i>
                                    </button>                                                                             
                                </td>
                            </tr>
                            <% counter++;%>
                        </s:iterator>
                    </tbody>
                </table>

                <table>
                    <tr>
                        <td>
                            <button type="button" id="saveButton" class="btn btn-primary" style="width:80px; height:30px;" id="save<%=counter%>">
                                <strong>Save</strong>
                            </button>
                        </td>
                        <td style="width:20px"></td>
                        <td>
                            <button class="btn btn-warning" id="addRowBtn" style="width:160px; height:30px"><i class="icon-black icon-plus-sign"></i><b>&nbsp;Add Milestone</b> </button>
                        </td>
                        <td style="width:600px"></td>
                        <td>
                            <h4 id="milestoneSettingsUpdateMessage"></h4>
                        </td>
                    </tr>
                </table>
                <br/><br/><br/>
            </s:if><s:else>
                <h4>No default milestones to set!</h4>
            </s:else>
        </div>

        <script type="text/javascript">

            var count = <%=counter%>;
            count++;
            var newCount = <%=counter%>;

            $("td").on('click', '#addRowBtn', function(e){
                
                var number = newCount;
                //alert(number);
                var onumber = "orderNumber" + number;
                var myTextArea = document.getElementById(milestoneConfigTable.id);
                var newOrderNumber = "<tr id='id" + number + "'><td><div class='input-append'>"
                        + "<input id='orderNumber" + number + "'style='width: 18px;height: 20px' type='text' name='orderNumber' value='" + number + "' disabled/>'"
                        + "<div class='btn-group'>"
                        + "<button class='btn' type='button' onclick='upOne(" + onumber + ");' >&#9650;</button>"
                        + "<button class='btn' type='button' onclick='downOne(" + onumber + ");' >&#9660;</button>"
                        + "</div></div></td>";
                var milestoneNumber = "milestone" + number;
                var newMilestoneId = "<td><input type='text' id='" + milestoneNumber + "' style='width:90px; height:20px'></input></td>";

                var durationNumber = "duration" + number;
                var newDuration = " <td>"
                        + "<div class='input-append'>"
                        + "<input type='text' style='width: 30px; height: 20px' cellspacing='0' id='" + durationNumber + "' value='60' disabled/>"
                        + "<button class='btn' type='button' onclick='upOne(" + durationNumber + ");'>&#9650;</button>"
                        + "<button class='btn' type='button' onclick='downOne(" + durationNumber + ");' >&#9660;</button>"
                        + "</div>" + "</td>";
                var textNumber = "textarea" + number;

                var attendees = "<td>"
                        + "<div id='" + textNumber + "' class='input-append'></div>"

                        + "<div><button class='btn' title='Add New Attendee' style='width: 40px;height: 25px;' onclick='createInput(" + textNumber + ");'><i class='icon-black icon-plus-sign'></i></button></div>"

                        + "</td>";
                //var saveNumber = "save" + number;                      
                var buttons = "<td>"
                        + "<button type='button' title='Delete Milestone' class='btn btn-danger' id='" + number + "' onclick='deleteRow(" + number + ");'><i class='icon-trash icon-white'></i></button>"
                        + "</td></tr>";
               
               
                
                var area = '#' + myTextArea.id;
                
                $(area).append(newOrderNumber + newMilestoneId + newDuration + attendees + buttons);
                
                
                //number++;
                newCount++;
                return false;
            });       


            function deleteRow(number) {
                //var id = this.id;
                //alert(id);
                var idToPass = "#id" + number;
                $(idToPass).remove();
            }
			
			$("#saveButton").on('click', function(){
                //get total length (here length is +1)
                //var count2 = $('#milestoneConfigTable :last td');
                var rows = document.getElementsByTagName("table")[0].rows;
                //var last = rows[rows.length - 1];
                //var cell = last.cells[0];
                //var value = cell.innerHTML;

                var numberChecker = "";

                for (var i = 0; i < rows.length; i++) {
                    //console.log(rows[i].id.substring(2));
                    if (i === rows.length - 1) {
                        numberChecker = rows[i].id.substring(2);
                    }
                }
                //console.log(rows);
                //var count = 99;
                var milestoneJson = new Array();
                var allOrders = "";

                //for each row, get the details
                for (var number = 1; number <= numberChecker; number++) {

                    var checker = "#" + number;
                    //check if the row exists
                    if ($(checker).length) {

                        //get the new order number
                        var newOrderNumber = document.getElementById('orderNumber' + number).value;
                        
                        
                        if(allOrders.indexOf(newOrderNumber)!==-1){
                            
                            showNotification("ERROR", "Please ensure order numbers are not the same!");
                            return true;
                        }
                        
                        allOrders += newOrderNumber + ",";
                        //get the new milestone name
                        var newMilestoneName = document.getElementById('milestone' + number).value;

                        if (newMilestoneName.length < 1) {

                            newMilestoneName = document.getElementById('milestone' + number).placeholder;

                        }

                        if (newMilestoneName === "") {
                            showNotification("ERROR" , "Please ensure that all milestone names are entered!");
                            return true;
                        }

                        //get the new duration number
                        var newDuration = document.getElementById('duration' + number).value;

                        //get the new attendees for this milestone
                        var attendees = document.getElementById('textarea' + number).getElementsByTagName('input');

                        //new attendees is a string representative of who the new attendees are
                        var newAttendees = "";
                        for (var i = 0; i < attendees.length; i++) {
                            var eachAttendee = attendees[i].id;
                            var toAdd = document.getElementById(eachAttendee).value + ",";

                            if (toAdd.length === 1) {
                                toAdd = document.getElementById(eachAttendee).placeholder + ",";
                            }

                            newAttendees += toAdd;
                        }
                        
                        if(newAttendees.length < 1){
                            showNotification("ERROR", "Please ensure that there is at least one required attendee for each milestone!");
                            return true;
                            
                        }
						
						//Creating object and storing in in array
						var milestone = {};
						milestone["newOrderNumber"] = newOrderNumber;
						milestone["newMilestoneName"] = newMilestoneName;
						milestone["newDuration"] = newDuration;
						milestone["newAttendees"] = newAttendees;
						//alert(jsonData);
						milestoneJson.push(milestone);
                    }
                }
				
                //code to send the update to backend. url corresponds to action class name defined in struts
                //uncomment this part
                $.ajax({
                 type: 'POST',
                 async: false,
                 url: 'updateMilestoneSettings',
                 data: {jsonData: JSON.stringify(milestoneJson)}
                 }).done(function(response) {
					if (!response.exception) {
						if (response.success) {
							showNotification("SUCCESS", response.message);
						} else {
							showNotification("INFO", response.message);
						}
						timedRefresh(2000);
					} else {
						var eid = btoa(response.message);
						window.location = "error.jsp?eid=" + eid;
					}
                 }).fail(function(response) {
					$("#saveButton").button('reset');
					$("#addRowBtn").button('reset');
					console.log("Updating Milestone settings AJAX FAIL");
					showNotification("WARNING", "Oops.. something went wrong");
                 });
				 return false;
			});

			function timedRefresh(timeoutPeriod) {
				setTimeout("location.reload(true);", timeoutPeriod);
			}
			
            function createInput(id) {
                //alert(id.id);
                //alert(id.id);
                var textArea = "#" + id.id;
                //var param = textArea ;
                //alert(textArea);
                var count2 = $(textArea + ' :button').length;
                //alert(count2);
                var totalAttendees = 0;

                for (var a = 1; a <= count2; a++) {

                    var nameArea = "#name" + a;

                    //var nameArea2 = "textarea" + nameArea;

                    //var nameArea2 = "name" + a;
                    if ($(nameArea).is(':hidden')) {

                    } else {
                        totalAttendees++;
                        //alert(nameArea);
                    }
                }

                count++;

                if (totalAttendees < 3) {
                    var name = "name" + count;

                    var text = "#" + id.id;
                    var nameUpdated = "'name" + count + "'";

                    var myTextArea = document.getElementById(id.id);

                    var innerText = document.getElementById(id.id).innerHTML;

                    var newInput = "";
                    var newButton = "";

                    //if supervisor is not found
                    if (innerText.indexOf("Supervisor") === -1) {
                        newInput = "<div><input style='width: 80px; height: 20px' type='text' id='" + name + "' value='Supervisor' disabled/>";
                        newButton = "<button id='delete' title='Delete Attendee' style='width: 40px;height: 30px;' class='btn' onclick='deleteInput(" + name + ");  $(this).remove();'><i class='icon-black icon-minus-sign'></i></button></div>";
                    } else if (innerText.indexOf("Reviewer1") === -1) {
                        newInput = "<div><input style='width: 80px; height: 20px' type='text' id='" + name + "' value='Reviewer1' disabled/>";
                        newButton = "<button id='delete' title='Delete Attendee' style='width: 40px;height: 30px;' class='btn' onclick='deleteInput(" + name + ");  $(this).remove();'><i class='icon-black icon-minus-sign'></i></button></div>";
                    } else if (innerText.indexOf("Reviewer2") === -1) {
                        newInput = "<div><input style='width: 80px; height: 20px' type='text' id='" + name + "' value='Reviewer2' disabled/>";
                        newButton = "<button id='delete' title='Delete Attendee' style='width: 40px;height: 30px;' class='btn' onclick='deleteInput(" + name + ");  $(this).remove();'><i class='icon-black icon-minus-sign'></i></button></div>";
                    }

                    myTextArea.innerHTML += newInput + newButton;
                    //alert(document.getElementById(name).id);
                    //$(text).append(newInput);

                    //var div = document.createElement("div");div.id = "generatedDiv" + count;
                    // document.getElementById(id.id).appendChild(div);
                    //div.innerHTML += newInput;

                    //alert(document.getElementById(name).id);
                    return false;
                    //window.attachEvent("onload", func);
                } else {
                    showNotification("ERROR" , "There are already 3 required attendees for this milestone!");
                }
            }

            function deleteInput(id) {
                //alert(id.id);
                //var text = "#" + id;
                var parsing = parseInt(id, 10);
                //alert(id.id);
                jQuery(id).remove();

                //jQuery(text).remove(text);
            }

            function upOne(id) {
                //alert(id.id);
                var formId = id.id;
                
                if (formId.indexOf('duration') !== -1) {
                    var num = id.value;
                    var newNum = parseInt(num, 10) + parseInt(30, 10);
                    if (parseInt(id.value, 10) !== 240) {
                        id.value = newNum;
                    }
                } else {
                    //var currentNum = parseInt($(formId).val());
                    //$(formId).attr('value', (currentNum + 1));
                    if (parseInt(id.value, 10) !== 240) {
                        id.value++;
                    }
                    //alert(id.id);
                }
            }

            function downOne(id) {
                formId = id.id;

                if (formId.indexOf('duration') !== -1) {
                    var num = id.value;
                    var newNum = parseInt(num, 10) - parseInt(30, 10);
                    if (parseInt(id.value, 10) !== 30) {
                        id.value = newNum;
                    }
                } else {
                    if (parseInt(id.value, 10) !== 1) {
                        id.value--;
                    }
                }
            }

            //Display Message
            function displayMessage(id, msg, fade) {
                //Dislay result
                var e = $("#" + id);
                $(e).fadeTo(3000, 0);
                $(e).css('color', 'darkgreen').html(msg);
                if (fade) {
                    $(e).css('color', 'darkred').html(msg).fadeTo(5000, 0);
                }
            }

			$(function(){
				var $btn = $('#saveButton');
				$btn.click(function(){
					var $this = $(this);
					var addRowBtn = document.getElementById('addRowBtn');
					addRowBtn.disabled = true;
					$this.attr('disabled', 'disabled').html("Saving...");
					setTimeout(function () {
						$this.removeAttr('disabled').html('<b>Save</b>');
						$(addRowBtn).removeAttr('disabled');
					}, 2000);
				});
			});
			
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
			}

        </script>
    </body> 
</html>
