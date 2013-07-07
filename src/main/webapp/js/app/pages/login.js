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
                //$(".loadingContainer").append("<p>Logging in</p>");
                $(this).button('loading');
                //blink(this);
                window.location = 'https://elearntools.smu.edu.sg/Tools/SSO/login.ashx?id=IS480PSAS';
            });
        }
    };
});