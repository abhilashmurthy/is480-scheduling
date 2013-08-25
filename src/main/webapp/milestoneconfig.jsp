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
                            <tr>
                                <td>
                                    <div class="input-append">
                                        <input id="orderNumber<%=counter%>" style="width: 18px;height: 20px" type="text" name="orderNumber" value="<s:property value="order"/>" disabled/>
                                        <div class="btn-group"> 
                                            <button class="btn" type="button" onclick="upOne(document.getElementById('orderNumber<%=counter%>'));" >&#9650;</button>
                                            <button class="btn" type="button" onclick="downOne(document.getElementById('orderNumber<%=counter%>'));" >&#9660;</button>
                                        </div>     
                                    </div>  
                                </td>
                                <td><input type='text' style="width: 100px;height: 20px" cellspacing='0' id='name' placeholder='<s:property value="name"/>'></input></td>
                                <td>
                                    <div class="input-append">
                                        <input type='text' style="width: 18px; height: 20px" cellspacing='0' id='duration<%=counter%>' value='<s:property value="duration" />' disabled/>
                                        <div class="btn-group">
                                            <button class="btn" type="button" onclick="upOne(document.getElementById('duration<%=counter%>'));" >&#9650;</button>
                                            <button class="btn" type="button" onclick="downOne(document.getElementById('duration<%=counter%>'));" >&#9660;</button>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <% int counter2 = counter; %>
                                    
                                    <s:iterator value="attendees">
                                        <div id="textarea<%=counter2%>">
                                            <div>
                                                <input type='text' style="width: 80px;height: 20px;" cellspacing='0' id='name<%=counter2%>' placeholder='<s:property value="attendee"/>'> </input> 

                                                <button id="delete" style="" class="btn" onclick="deleteInput(document.getElementById('name<%=counter2%>'));
                                                    $(this).hide();"><i class="icon-black icon-minus-sign"></i></button>
                                            </div>
                                        </div> 
                                        <% counter2++;%>
                                    </s:iterator>
                                    
                                    <button class="btn-info" onclick="createInput(document.getElementById('textarea<%=counter%>'));"><i class="icon-black icon-plus-sign"></i>  Add </button>
                                </td>

                                <td>

                                    <button type="button" style="width: 70px;"  class="btn-info"><i class='icon-edit icon-white'></i> Save </button>                                                                             
                                    <button type="button" class="btn-danger"><i class='icon-trash icon-white'></i> Delete </button>                                                                             
                                    <!-- <button class="btn" onClick="window.location.reload()"><i class="icon-black icon-refresh"></i> Reset </button>
                                        -->
                                </td>
                            </tr>

                            <% counter++;%>


                        </s:iterator>

                    </tbody>
                </table>
            </s:if><s:else>
                <h4>No default milestones!</h4>
            </s:else>
        </div>

    </body>      
    <script type="text/javascript">

        var count = <%=counter%>

        function createInput(id) {
            //alert(id.id);
            count++;
            var name = "name" + count;

            var text = "#" + id.id;
            var nameUpdated = "'name" + count + "'";
            //alert(nameUpdated);
            var newInput = "<div><input style='width: 80px; height: 20px' type='text' id='" + name + "' />";
            var newButton = "<button id='delete' class='btn' onclick='deleteInput(" + name + ");$(this).hide();  '><i class='icon-black icon-minus-sign'></i></button></div>";

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
            alert(id);
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
