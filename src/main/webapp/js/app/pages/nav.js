define([
    'jquery',
    'bootstrap'
], function($, bootstrap) {
    return {
        //Initialize function
        init: function() {
            //Nav specific
            $(".dropdown-toggle").on('click', function() {
                this.dropdown;
            });
            $("#logoutLink").on('click', function() {
                document.location.href = '/is480-scheduling/logout';
            });
            $(".nav li").on('click', function() {
                $(".nav li").removeClass("active");
                $(this).addClass("active");
            });
        }
    };
});