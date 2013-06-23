<%-- 
    Document   : login_process
    Created on : Apr 27, 2012, 8:24:34 PM
    Author     : xiangrui
--%>

<%@page import="java.util.Calendar"%>
<%@page import="javax.crypto.SecretKey"%>
<%@page import="org.apache.catalina.util.Base64"%>
<%@page import="java.math.BigInteger"%>
<%@page import="javax.crypto.spec.SecretKeySpec"%>
<%@page import="javax.crypto.Mac"%>
<%@page import="java.net.URLEncoder"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Process Login</title>
    </head>
    <body>
        <%
            String uri = "POST&" + URLEncoder.encode("http://" + request.getServerName() + request.getRequestURI() , "UTF-8") + "&" + 
                    URLEncoder.encode(
                    "oauth_callback=" + URLEncoder.encode(request.getParameter("oauth_callback"), "UTF-8") + "&" 
                    + "oauth_consumer_key=" + URLEncoder.encode(request.getParameter("oauth_consumer_key"), "UTF-8") + "&"
                    + "oauth_nonce=" + URLEncoder.encode(request.getParameter("oauth_nonce"), "UTF-8") + "&" 
                    + "oauth_signature_method=" + URLEncoder.encode(request.getParameter("oauth_signature_method"),"UTF-8") + "&"
                    + "oauth_timestamp=" + URLEncoder.encode(request.getParameter("oauth_timestamp"), "UTF-8") + "&"
                    + "oauth_version=" + URLEncoder.encode(request.getParameter("oauth_version"), "UTF-8") + "&" 
                    + "smu_domain=" + URLEncoder.encode(request.getParameter("smu_domain"), "UTF-8") + "&"
                    + "smu_fullname=" + URLEncoder.encode(request.getParameter("smu_fullname"), "UTF-8") + "&"
                    + "smu_groups=" + URLEncoder.encode(request.getParameter("smu_groups"), "UTF-8") + "&"
                    + "smu_username=" + URLEncoder.encode(request.getParameter("smu_username"), "UTF-8")
                    , "UTF-8");
            
      
    Mac mac = Mac.getInstance("HmacSHA1");
    SecretKey secretKey = null;
    String keyString = "test12345678&";
    byte[] keyBytes = keyString.getBytes();
    secretKey = new SecretKeySpec(keyBytes, "HmacSHA1");
    mac.init(secretKey);
    byte[] text = uri.getBytes();
    String encodedString = Base64.encode(mac.doFinal(text)).toString().trim();
    String signature = (request.getParameter("oauth_signature"));
    
    Calendar calendar = Calendar.getInstance();
    String timeStampStr = request.getParameter("oauth_timestamp");
    long timeStamp = Long.parseLong(timeStampStr);
    long time = calendar.getTimeInMillis() / 1000; 
    
    if(encodedString.equals(signature) && (Math.abs(timeStamp - time) <= 10))  {
        session.setAttribute("username", request.getParameter("smu_username"));
        session.setAttribute("fullName", request.getParameter("smu_fullname"));
        
        String redirectURL = "question_page.jsp";
        response.sendRedirect(redirectURL);
    } else {
        out.println("There is an error in the login process. Please try again.");
    }
            
            %>
    </body>
</html>
