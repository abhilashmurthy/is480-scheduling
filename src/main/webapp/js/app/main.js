//Config
require.config({
    baseUrl: 'js/',
    paths: {
        jquery: 'plugins/jquery-2.0.2'
    }
});

//Main logic
require(['jquery', 'bootstrap'], function($, bootstrap) {
    
    $(".dropdown-toggle").on('click', function() {
        this.dropdown;
    });
    
    $("#logoutLink").on('click', function() {
       document.location.href = '/is480-scheduling/logout';
    });
    
    function blink(selector) {
        $(selector).fadeOut('slow', function() {
            $(this).fadeIn('slow', function() {
                blink(this);
            });
        });
    }
    
    $("#ssoBtn").on('click', function() {
        //Send an AJAX call to the IS480PSAS website
       $(".loadingContainer").append("<p>Logging in</p>");
       blink('p');
       window.location = 'https://elearntools.smu.edu.sg/Tools/SSO/login.ashx?id=IS480PSAS';
    });
    
    $(".nav li").on('click', function(){
       $(".nav li").removeClass("active");
       this.addClass("active");
    });
 
});