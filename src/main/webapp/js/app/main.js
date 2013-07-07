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
        'bootstrap': 'jquery',
        'knockout': 'jquery'
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
    
    //Initalize
    login.init();
    nav.init();
    index.init();
    
    //Pages
    createbooking.init(); 
    knockoutapp.init();

});
