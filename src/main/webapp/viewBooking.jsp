<%-- 
    Document   : viewBooking
    Created on : Jul 23, 2013, 8:06:44 PM
    Author     : Tarlochan
--%>

<%@page import="com.opensymphony.xwork2.ActionContext"%>
<%@page import="com.opensymphony.xwork2.util.ValueStack"%>
<%@page contentType="text/html" pageEncoding="windows-1252"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
    <head>
        <%@include file="footer.jsp"%>
        <meta http-equiv="Content-Type" content="text/html; charset=windows-1252">
        <title>View Booking</title>


    </head>
    <body>
        <h2>View booking for:</h2>

        <!-- SECTION: Timeslot Table -->
        <div>
            <form id="viewBookingForm" onsubmit="showHide();
                    return true;">
               
                <br />
                <select name="timeslotId" onchange="submitForm()">
                    <option>--Choose a timeslot ID--</option>
                    <option value="1">1</option>
                    <option value="2">2</option>
                    <option value="3">3</option>
                    <option value="4">4</option>
                    <option value="5">5</option>
                    <option value="6">6</option>
                    <option value="7">7</option>
                    <option value="11">11</option>
					<option value="33">33</option>
                </select> 
                <input id="viewBookingFormBtn" type="submit" class="btn btn-primary" value="View" data-loading-text="Loading..." style="visibility:hidden" />
            </form>
            
           
            
            
            

            <!-- SECTION: Response Banner -->
            
            <div id="hidden_div" style="display:none">
                  <a class="popup-link-1">View</a>
            </div>

            <div class="popup-box" id="popup-box-1"><div class="close">X</div><div class="top"><h2>Booking Details:</h2>
                </div>
                    <div class="bottom"> 
                    <div id="responseBanner" class="alert">
                        <span id="responseMessage" style="font-weight: bold"></span>
       
                    </div>
                </div>
            </div>
            <div id="blackout"></div>              
            
            <script>
                $(document).ready(function() {
                    var boxWidth = 400;

                    function centerBox() {

                        /* Preliminary information */
                        var winWidth = $(window).width();
                        var winHeight = $(document).height();
                        var scrollPos = $(window).scrollTop();
                        /* auto scroll bug */

                        /* Calculate positions */

                        var disWidth = (winWidth - boxWidth) / 2
                        var disHeight = scrollPos + 150;

                        /* Move stuff about */
                        $('.popup-box').css({'width': boxWidth + 'px', 'left': disWidth + 'px', 'top': disHeight + 'px'});
                        $('#blackout').css({'width': winWidth + 'px', 'height': winHeight + 'px'});

                        return false;
                    }


                    $(window).resize(centerBox);
                    $(window).scroll(centerBox);
                    centerBox();
                    
                    $('[class*=popup-link]').click(function(e) {

                        /* Prevent default actions */
                        e.preventDefault();
                        e.stopPropagation();

                        /* Get the id (the number appended to the end of the classes) */
                        var name = $(this).attr('class');
                        var id = name[name.length - 1];
                        var scrollPos = $(window).scrollTop();

                        /* Show the correct popup box, show the blackout and disable scrolling */
                        $('#popup-box-' + id).show();
                        $('#blackout').show();
                        $('html,body').css('overflow', 'hidden');

                        /* Fixes a bug in Firefox */
                        $('html').scrollTop(scrollPos);
                    });
 
                    $('[class*=popup-box]').click(function(e) {
                        /* Stop the link working normally on click if it's linked to a popup */
                        e.stopPropagation();
                    });
                    $('html').click(function() {                      
                        
                        
                        var scrollPos = $(window).scrollTop();
                        /* Hide the popup and blackout when clicking outside the popup */
                        $('[id^=popup-box-]').hide();
                        $('#blackout').hide();
                        $("html,body").css("overflow", "auto");
                        $('html').scrollTop(scrollPos);
                    });
                    $('.close').click(function() {
                        var scrollPos = $(window).scrollTop();
                        /* Similarly, hide the popup and blackout when the user clicks close */
                        $('[id^=popup-box-]').hide();
                        $('#blackout').hide();
                        $("html,body").css("overflow", "auto");
                        $('html').scrollTop(scrollPos);
                    });
                });

            </script>

            <style>
                .popup-box {
                    position: absolute;
                    border-radius: 5px;
                    background: #fff;
                    display: none;
                    box-shadow: 1px 1px 5px rgba(0,0,0,0.2);
                    font-family: Arial, sans-serif;
                    z-index: 9999999;
                    font-size: 14px;
                }

                .popup-box .close {
                    position: absolute;
                    top: 0px;
                    right: 0px;
                    font-family: Arial, Helvetica, sans-serif;	
                    font-weight: bold;
                    cursor: pointer;
                    color: #434343;
                    padding: 20px;
                    font-size: 20px;
                }

                .popup-box .close:hover {
                    color: #000;
                }

                .popup-box h2 {
                    padding: 0;
                    margin: 0;
                    font-size: 18px;
                }
                .popup-box .top {
                    padding: 20px;
                }

                .popup-box .bottom {
                    background: #eee;
                    border-top: 1px solid #e5e5e5;
                    padding: 20px;
                    border-bottom-left-radius: 5px;
                    border-bottom-right-radius: 5px;
                }

                #blackout {
                    background: rgba(0,0,0,0.3);
                    position: absolute;
                    top: 0;
                    overflow: hidden;
                    z-index: 9999;
                    left: 0;
                    display: none;
                }


            </style>     


            <script type="text/javascript">
                //document.getElementById("viewBookingForm").submit();
                function showHide() {
                    var div = document.getElementById("hidden_div");
                    //if (div.style.display == 'none') {
                    div.style.display = '';
                    
                    //}
                    //else {
                    //div.style.display = 'none';
                    // }
                }

                function submitForm() {
                    
                    var selectButton = document.getElementById('viewBookingFormBtn');
                    selectButton.click();
                     
                        //window.open('', 'popup-box-1', 'width=400,height=400,resizeable,scrollbars');
                        //this.target = 'popup-box-1';
                   
                }
                


                $("#viewBookingForm").bind('submit', function() {
                    $("#viewBookingFormBtn").button('loading');
                    console.log("Submit function called");
                    var formData = $("#viewBookingForm").serialize();
                    $.ajax({
                        type: 'GET',
                        url: 'viewBookingJson',
                        data: formData,
                        dataType: 'json'
                    }).done(function(response) {
                        $("#viewBookingFormBtn").button('reset');
                        console.log(response);
                        $("#responseBanner").show();
                        if (response.success) {
                            //$("#responseBanner").removeClass("alert-error").addClass("alert-success");
                            $("#responseMessage").text(response.message);
                        } else {
                            //$("#responseBanner").removeClass("alert-success").addClass("alert-error");
                            $("#responseMessage").text(response.message);
                        }
                    }).fail(function(response) {

                        $("#viewBookingFormBtn").button('reset');
                        console.log(response);
                        $("#responseBanner").show();
                        //$("#responseBanner").removeClass("alert-success").addClass("alert-error");
                        $("#responseMessage").text("Oops. Something went wrong. Please try again!");
                    });
                    return false;
                });
            </script>

            <%@include file="navbar.jsp" %>

    </body>
</html>
