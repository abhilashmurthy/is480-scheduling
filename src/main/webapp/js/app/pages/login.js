define([
    'jquery',
    'bootstrap'
], function($, bootstrap) {
    return {
        //Initialize function
        init: function() {
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
        }
    };
});