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
                                        <input id="orderNumber<%=counter%>" style="width: 18px;height: 20px" type="text" name="orderNumber" value="<s:property value="order"/>" disabled/>
                                        <div class="btn-group"> 
                                            <button class="btn" type="button" onclick="upOne(document.getElementById('orderNumber<%=counter%>'));" >&#9650;</button>
                                            <button class="btn" type="button" onclick="downOne(document.getElementById('orderNumber<%=counter%>'));" >&#9660;</button>
                                        </div>     
                                    </div>  
                                </td>
                                <td><input type='text' id="milestone<%=counter%>" style="width: 100px;height: 20px" placeholder='<s:property value="name"/>'></input></td>
                                <td>
                                    <div class="input-append">
                                        <input type='text' style="width: 28px; height: 20px" cellspacing='0' id='duration<%=counter%>' value='<s:property value="duration" />' disabled/>
                                        <div class="btn-group">
                                            <button class="btn" type="button" onclick="upOne(document.getElementById('duration<%=counter%>'));" >&#9650;</button>
                                            <button class="btn" type="button" onclick="downOne(document.getElementById('duration<%=counter%>'));" >&#9660;</button>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    
                                    <div id="textarea<%=counter%>">
                                    <s:iterator value="attendees">
                                        
                                            <div id="eachOne<%=counter%>">
                                                <input type='text' style="width: 80px;height: 20px;" cellspacing='0' id='name<%=counter2%>' placeholder='<s:property value="attendee"/>'></input>

                                                <button id="delete" style="width: 40px;height: 25px;" class="btn" onclick="deleteInput(document.getElementById('name<%=counter2%>'));
                                                    $(this).hide();"><i class="icon-black icon-minus-sign"></i></button>
                                            </div>
                                        
                                        <% counter2++;%>
                                    </s:iterator>
                                    </div> 
                                    <button class="btn-info" style="width: 40px;height: 25px;" onclick="createInput(document.getElementById('textarea<%=counter%>'));"><i class="icon-black icon-plus-sign"></i> </button>
                                </td>

                                <td>

                                                                                                                 
                                    
                                    <button type="button" class="btn-danger" id="<%=counter%>" onclick='deleteRow(<%=counter%>);'><i class='icon-trash icon-white'></i> Delete </button>                                                                             
                                    <!-- <button class="btn" onClick="window.location.reload()"><i class="icon-black icon-refresh"></i> Reset </button>
                                        -->
                                      
                                </td>
                            </tr>

                            <% counter++;%>


                        </s:iterator>

                    </tbody>
                    
                      
                </table>
                <br/>
                    <button class="btn-info" style="position:absolute;right: 100px;width:140px; height:70px" onclick="addRow(<%=counter%>)"><i class="icon-black icon-plus-sign"></i>  <b>Add new milestone</b> </button>
                    <button type="button" style="position:absolute;right: 240px;width:140px; height:70px" id="save<%=counter%>"style="width: 70px;"  class="btn-info" onclick="edited();"><i class='icon-edit icon-black'></i><b>Save </b> </button>
                
            </s:if><s:else>
                <h4>No default milestones!</h4>
            </s:else>
        </div>
          
    </body>      
    <script type="text/javascript">

        var count = <%=counter%>;
        count++;
        var newCount = <%=counter%>;
        
        function addRow(){
            
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
            var newMilestoneId = "<td><input type='text' id='" + milestoneNumber +"' style='width: 100px;height: 20px'></input></td>";
            
            var durationNumber = "duration" + number;
            var newDuration =  " <td>"
                                  +  "<div class='input-append'>" 
                                    +  "<input type='text' style='width: 28px; height: 20px' cellspacing='0' id='" + durationNumber + "' value='60' disabled/>'"
                                       + "<div class='btn-group'>"
                                         +   "<button class='btn' type='button' onclick='upOne(" + durationNumber + ");' >&#9650;</button>"
                                            +  "<button class='btn' type='button' onclick='downOne(" + durationNumber + ");' >&#9660;</button>"
                                                + "</div></div></td>";
             var textNumber = "textarea" + number;
             var attendees = "<td>"    
                               + "<div id='" + textNumber + "'>"
                                 + "<div>" 
                                    + "</div>" 
                                        + "<button class='btn-info' style='width: 40px;height: 25px;' onclick='createInput(" + textNumber + ");'><i class='icon-black icon-plus-sign'></i>  Add </button>"           
                                           + "</div></td>";
             //var saveNumber = "save" + number;                      
             var buttons = "<td>"                                                                             
                                + "<button type='button' class='btn-danger' id='" + number + "' onclick='deleteRow(" + number + ");'><i class='icon-trash icon-white'></i> Delete </button>"                                                                                                            
                                  +"</td></tr>";
                                    
             myTextArea.innerHTML += newOrderNumber + newMilestoneId + newDuration + attendees + buttons;
             
            //number++;
            newCount++;                           
        }
        
        function deleteRow(number){
             //var id = this.id;
             //alert(id);
             var idToPass = "#id" + number;
             $(idToPass).remove();
        }
        
        function edited(){
            
            //get total length (here length is +1)
            var count2 = $('#milestoneConfigTable :last td');
            var rows = document.getElementsByTagName("table")[0].rows;
            //var last = rows[rows.length - 1];
            //var cell = last.cells[0];
            //var value = cell.innerHTML;
            
            var numberChecker = "";
            
            for(var i=0;i<rows.length;i++){
                //console.log(rows[i].id.substring(2));
                if(i===rows.length-1){
                    numberChecker = rows[i].id.substring(2);
                }
            }
            //console.log(rows);
            //var count = 99;
            
            var data = "";
            
            //for each row, get the details
            for(var number=1;number<=numberChecker;number++){
                
                var checker = "#" + number;
                //check if the row exists
                if($(checker).length){
                    //get the past order of this milestone
                    var pastOrder = number;

                    //get the new order number
                    var newOrderNumber = document.getElementById('orderNumber'+number).value;

                    //get the new milestone name
                    var newMilestoneName = document.getElementById('milestone'+number).value;

                    if(newMilestoneName.length < 1){

                        newMilestoneName = document.getElementById('milestone'+number).placeholder;

                    }

                    //get the new duration number
                    var newDuration = document.getElementById('duration'+number).value;

                    //get the new attendees for this milestone
                    var attendees = document.getElementById('textarea' + number).getElementsByTagName('input');

                    //new attendees is a string representative of who the new attendees are
                    var newAttendees = "";
                    for(var i=0;i<attendees.length;i++){
                        var eachAttendee = attendees[i].id;
                        var toAdd = document.getElementById(eachAttendee).value + ",";

                        if(toAdd.length === 1){
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
                url: 'milestoneUpdateJson',
                data: data,
                cache: false,
                dataType: 'json'

            }).done(function(response) {
                if (!response.exception) {
                    console.log('Destroying C');
                    self.popover('destroy');
                    var msg = response.message + "";
                    console.log(msg);

                    if (msg === ('Booking updated successfully! Update email has been sent to all attendees. (Coming soon..)')) {
                        response.message;
                    } else {
                        

                    }
                } else {
                    var eid = btoa(response.message);
                    window.location = "error.jsp?eid=" + eid;
                }
            }).fail(function(error) {
                alert("Oops. There was an error: " + error);
            });*/
        }

        function createInput(id) {
            //alert(id.id);
            count++;
            var name = "name" + count;

            var text = "#" + id.id;
            var nameUpdated = "'name" + count + "'";
            //alert(nameUpdated);
            var newInput = "<div><input style='width: 80px; height: 20px' type='text' id='" + name + "' />";
            var newButton = "<button id='delete' style='width: 40px;height: 25px;' class='btn' onclick='deleteInput(" + name + ");$(this).hide();  '><i class='icon-black icon-minus-sign'></i></button></div>";

            myTextArea = document.getElementById(id.id);
            myTextArea.innerHTML += newInput + newButton;
            //alert(document.getElementById(name).id);
            //$(text).append(newInput);

            //var div = document.createElement("div");div.id = "generatedDiv" + count;
            // document.getElementById(id.id).appendChild(div);
            //div.innerHTML += newInput;

            //alert(document.getElementById(name).id);
            return false;
            //window.attachEvent("onload", func);

        }

        function deleteInput(id) {
            //alert(id.id);
            //var text = "#" + id;
            jQuery(id).remove();
            
            //jQuery(text).remove(text);
        }

        function upOne(id) {
            //alert(id.id);
            formId = id.id;
            //var currentNum = parseInt($(formId).val());
            //$(formId).attr('value', (currentNum + 1));
            id.value++;
            //alert(id.id);
        }

        function downOne(id) {
            formId = id.id;
            id.value--;
        }

    </script>

</html>
