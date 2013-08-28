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
        <meta http-equiv="Content-Type" content="text/html; charset=windows-1252">
        <title>Milestone Configuration</title>

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
                                    <input type='text' id="milestone<%=counter%>" style="width:90px; height:20px" value='<s:property value="name"/>'></input>

                                </td>
                                <td style="width:90px">
                                    <input type='text' style="width:30px; height:20px" cellspacing='0' id='duration<%=counter%>' value='<s:property value="duration" />' disabled/>

                                    <div class="btn-group">
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
                            <button type="button" class="btn btn-primary" style="width:80px; height:30px;" id="save<%=counter%>" class="btn-info" onclick="edited();">
                                <b>Save</b>
                            </button>
                        </td>
                        <td style="width:20px"></td>
                        <td>
                            <button class="btn btn-warning" style="width:160px; height:30px" onclick="addRow(<%=counter%>)"><i class="icon-black icon-plus-sign"></i><b>&nbsp;Add Milestone</b> </button>
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

                                                function addRow() {

                                                    var number = newCount;
                                                    //alert(number);
                                                    var onumber = "orderNumber" + number;
                                                    var myTextArea = document.getElementById(milestoneConfigTable.id);
                                                    var newOrderNumber = "<tr id='id" + number + "'><td><div class='input-append'>"
                                                            + "<input id='orderNumber" + number + "'style='width: 18px;height: 20px' type='text' name='orderNumber' value='0' disabled/>'"
                                                            + "<div class='btn-group'>"
                                                            + "<button class='btn' type='button' onclick='upOne(" + onumber + ");' >&#9650;</button>"
                                                            + "<button class='btn' type='button' onclick='downOne(" + onumber + ");' >&#9660;</button>"
                                                            + "</div></div></td>";
                                                    var milestoneNumber = "milestone" + number;
                                                    var newMilestoneId = "<td><input type='text' id='" + milestoneNumber + "' style='width:90px; height:20px'></input></td>";

                                                    var durationNumber = "duration" + number;
                                                    var newDuration = " <td>"
                                                            + "<input type='text' style='width: 30px; height: 20px' cellspacing='0' id='" + durationNumber + "' value='60' disabled/>"
                                                            + "<div class='btn-group'>"
                                                            + "<button class='btn' type='button' onclick='upOne(" + durationNumber + ");'>&#9650;</button>"
                                                            + "<button class='btn' type='button' onclick='downOne(" + durationNumber + ");' >&#9660;</button>"
                                                            + "</div>" + "</td>";
                                                    var textNumber = "textarea" + number;

                                                    var attendees = "<td>"
                                                            + "<div id='" + textNumber + "'>"
                                                            + "<div>"
                                                            + "<button class='btn' title='Add New Attendee' style='width: 40px;height: 25px;' onclick='createInput(" + textNumber + ");'><i class='icon-black icon-plus-sign'></i></button>"
                                                            + "</div>"
                                                            + "</div></td>";
                                                    //var saveNumber = "save" + number;                      
                                                    var buttons = "<td>"
                                                            + "<button type='button' title='Delete Milestone' class='btn btn-danger' id='" + number + "' onclick='deleteRow(" + number + ");'><i class='icon-trash icon-white'></i></button>"
                                                            + "</td></tr>";

                                                    myTextArea.innerHTML += newOrderNumber + newMilestoneId + newDuration + attendees + buttons;

                                                    //number++;
                                                    newCount++;
                                                }

                                                function deleteRow(number) {
                                                    //var id = this.id;
                                                    //alert(id);
                                                    var idToPass = "#id" + number;
                                                    $(idToPass).remove();
                                                }

                                                function edited() {

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

                                                    var data = "";

                                                    //for each row, get the details
                                                    for (var number = 1; number <= numberChecker; number++) {

                                                        var checker = "#" + number;
                                                        //check if the row exists
                                                        if ($(checker).length) {
                                                            //get the past order of this milestone
                                                            var pastOrder = number;

                                                            //get the new order number
                                                            var newOrderNumber = document.getElementById('orderNumber' + number).value;

                                                            //get the new milestone name
                                                            var newMilestoneName = document.getElementById('milestone' + number).value;

                                                            if (newMilestoneName.length < 1) {

                                                                newMilestoneName = document.getElementById('milestone' + number).placeholder;

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

                                                            data += "pastOrderNumber:" + pastOrder + ",newOrderNumber:" + newOrderNumber + ",newMilestoneName:" + newMilestoneName
                                                                    + ",newDuration:" + newDuration + ",newAttendees:" + newAttendees;
                                                        }
                                                    }
                                                    alert(data);

                                                    //code to send the update to backend. url corresponds to action class name defined in struts
                                                    //uncomment this part
                                                    /*$.ajax({
                                                     type: 'POST',
                                                     async: false,
                                                     url: 'updateMilestoneSettings',
                                                     data: data,
                                                     cache: false,
                                                     dataType: 'json'
                                                     
                                                     }).done(function(response) {
                                                     if (response.success) {
                                                     console.log("Milestones updated successfully!");
                                                     displayMessage("milestoneSettingsUpdateMessage", response.message, false);
                                                     } else {
                                                     var eid = btoa(response.message);
                                                     console.log(response.message);
                                                     window.location = "error.jsp?eid=" + eid;
                                                     }
                                                     }).fail(function(error) {
                                                     console.log("Updating Milestone settings AJAX FAIL");
                                                     displayMessage("milestoneSettingsUpdateMessage", "Oops.. something went wrong", true);
                                                     });*/
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
                                                        alert("There are already 3 required attendees for this milestone");
                                                    }
                                                }

                                                function deleteInput(id) {
                                                    //alert(id.id);
                                                    //var text = "#" + id;
                                                    var parsing = parseInt(id, 10);
                                                    alert(id.id);
                                                    jQuery(id).remove();

                                                    //jQuery(text).remove(text);
                                                }

                                                function upOne(id) {
                                                    //alert(id.id);
                                                    var formId = id.id;
                                                    //alert(formId);
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
                                                        if (parseInt(id.value, 10) !== 0) {
                                                            id.value = newNum;
                                                        }
                                                    } else {
                                                        if (parseInt(id.value, 10) !== 0) {
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
                                                        $(e).css('color', 'darkred').html(msg).fadeTo(3000, 0);
                                                    }
                                                }

        </script>
    </body> 
</html>
