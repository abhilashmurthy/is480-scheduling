<%@page import="sun.misc.BASE64Decoder"%>
<%@page import="java.io.ByteArrayInputStream"%>
<%@page import="javax.xml.bind.DatatypeConverter"%>
<!DOCTYPE html>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html lang="en">
    <head>
        <%@include file="header.jsp" %>
        <title>IS480 Scheduling System | Error</title>
        <% 
            String eid = request.getParameter("eid");
            if (eid != null) {
                BASE64Decoder decoder = new BASE64Decoder(); 
                String errorMsg = new String(decoder.decodeBuffer(eid.trim()), "UTF-8");
                request.setAttribute("error", errorMsg);
            } else if (request.getAttribute("error") == null) {
                request.setAttribute("error", "Oops, something went wrong.<br/>");
            }
        %>
    </head>
    <body>
        <%@include file="navbar.jsp"%>

        <div class="container" style="position: relative; text-align: center">
            <div class="content">
                <div class="row">
                    <h3><%= request.getAttribute("error")%></h3><br/>
                    <button class="btn btn-warning btn-large" onclick="window.location = 'index'">Back to Home</button>
                </div>
            </div>
        </div> <!-- /container -->
        <%@include file="footer.jsp"%>
    </body>
</html>