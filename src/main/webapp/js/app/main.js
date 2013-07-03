//Config
require.config({
    baseUrl: 'js/',
    paths: {
        jquery: 'plugins/jquery-2.0.2',
    }
});

//Main logic
require(['jquery', 'plugins/jquery.oauth'], function($, oauth) {
    $("#ssoBtn").on('click', function() {
        //Problem: Cross domain policy
        //Send an AJAX call to the IS480PSAS website
//       document.domain = "https://www.facebook.com";
    
//        $.oauth({
//            type: 'GET',
//            url: 'https://elearntools.smu.edu.sg/Tools/SSO/login.ashx?id=IS480PSAS'
//        }).done(function(response){
//            alert("Done");
//        }).fail(function(error){
//            alert("Fail");
//        });
    
//       $.ajax({
//          type: 'GET',
//          beforeSend: function (request) {
//              request.setRequestHeader('Access-Control-Allow-Origin', '*');
//          },
//          url: 'https://elearntools.smu.edu.sg/Tools/SSO/login.ashx?id=IS480PSAS'
//       }).done(function(response) {
//           for (var i in response) {
//               alert("Response is: [" + i + ", " + response[i] + "]");
//           }
//       }).fail(function(error){
//           alert("failure");
//       });
//       
//        window.postMessage("Tools/SSO/login.ashx?id=IS480PSAS", "https://elearntools.smu.edu.sg");
//        function receiveMessage(response) {
//            alert("window response: " + response);
//        }
//        window.addEventListener("message", receiveMessage, false);

//        function createCORSRequest(method, url) {
//            var xhr = new XMLHttpRequest();
//            if ("withCredentials" in xhr) {
//                xhr.open(method, url, true);
//            } else if (typeof XDomainRequest !== "undefined") {
//                xhr = new XDomainRequest();
//                xhr.open(method, url);
//            } else {
//                xhr = null;
//            }
//            return xhr;
//        }
//
//        var request = createCORSRequest("get", "https://elearntools.smu.edu.sg/Tools/SSO/login.ashx?id=IS480PSAS");
//        if (request) {
//            request.onload = function() {
//                alert("Request sending");
//            };
////            request.onreadystatechange = handler;
//            request.send();
//            alert("Request sent");
//        }

    });
});