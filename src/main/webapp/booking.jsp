<%-- 
    Document   : Booking
    Created on : Jun 30, 2013, 5:45:00 PM
    Author     : Prakhar
--%>

<%@page contentType="text/html" pageEncoding="windows-1252"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Create Booking</title>
        <!--		<script type="text/javascript" src="js/plugins/jquery-ui/js/jquery-ui-1.10.3.custom.min.js"></script>
                        <script type="text/javascript" src="js/plugins/jquery-2.0.2.js"></script>-->
    </head>
    <body>
        <!-- Navigation -->
        <%@include file="navbar.jsp" %>
        <div class="container page">
            <h2>Create Booking</h2>

            <!-- SECTION: Timeslot Table -->
            <div>
                <form id="createBookingForm">
                    Date: <input type="text" class="input-medium datepicker" name="date" /> &nbsp;
                    Start Time:
                    <input type="text" class="input-medium" name="startTime" id="timepicker"/> &nbsp;<br />
                    <select name="termId">
                        <option value="2013,Term 1">2013-14 Term 1</option>
                        <option value="2013,Term 2">2013-14 Term 2</option>
                    </select> &nbsp;
                    <select name="milestoneStr">
                        <option value="acceptance">Acceptance</option>
                        <option value="midterm">Midterm</option>
                        <option value="final">Final</option>
                    </select> <br /> <br />
                    <input id="createBookingFormBtn" type="submit" class="btn btn-primary" value="Create" data-loading-text="Waiting..."/>
                </form>
            </div>
            <!-- SECTION: Response Banner -->
            <div id="responseBanner" class="alert" hidden>
                <span id="responseMessage" style="font-weight: bold"></span>
            </div>
        </div>
        <%@include file="footer.jsp"%>
        <script type="text/javascript">
            $(".datepicker").datepicker({
                beforeShowDay: $.datepicker.noWeekends,
                dateFormat: "yy-mm-dd"
            });

            $("#timepicker").timepicker({
                timeFormat: 'H:i:s',
                minTime: '9:00am',
                maxTime: '18:00pm'
            });
            $("#createBookingForm").bind('submit', function() {
                $("#createBookingFormBtn").button('loading');
                console.log("Submit function called");
                var formData = $("#createBookingForm").serialize();
                $.ajax({
                    type: 'GET',
                    url: 'createBookingJson',
                    data: formData,
                    dataType: 'json'
                }).done(function(response) {
                    $("#createBookingFormBtn").button('reset');
                    console.log(response);
                    $("#responseBanner").show();
                    if (response.success) {
                        $("#responseBanner").removeClass("alert-error").addClass("alert-success");
                        $("#responseMessage").text(response.message);
                    } else {
                        $("#responseBanner").removeClass("alert-success").addClass("alert-error");
                        $("#responseMessage").text(response.message);
                    }
                }).fail(function(response) {
                    $("#createBookingFormBtn").button('reset');
                    console.log(response);
                    $("#responseBanner").show();
                    $("#responseBanner").removeClass("alert-success").addClass("alert-error");
                    $("#responseMessage").text("Oops. Something went wrong. Please try again!");
                });
                return false;
            });
        </script>
    </body>
</html>
