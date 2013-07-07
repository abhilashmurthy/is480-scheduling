//Config
require.config({
    baseUrl: 'js/',
    //default paths
    paths: {
        jquery: 'plugins/jquery-2.0.2',
        bootstrap: 'plugins/bootstrap',
        knockout: 'plugins/knockout',
        pages: 'app/pages'
    },
    //js dependencies
    shim: {
        jQuery: {
            exports: 'jquery'
        },
        bootstrap: {
            deps: ['jquery'],
            exports : 'jquery'
        },
        ko: {
            deps: ['jquery'],
            exports :'jquery'
        },
        enforceDefine: true
    }
});

//Main logic
require([
    'pages/login',
    'pages/index',
    'pages/nav',
    'pages/createbooking',
    'pages/knockoutapp'
], function(login, index, nav, createbooking, knockoutapp) {
    
    $(function(){
        //Initalize
        nav.init();
        login.init();
        index.init();

        //Pages
        createbooking.init();
        knockoutapp.init();
    })

});
