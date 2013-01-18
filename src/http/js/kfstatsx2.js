function goto(id, t){   
    //animate to the div id.
    $(".contentbox-wrapper").animate({"left": -($(id).position().left)}, 600);
    
    // remove "active" class from all links inside #nav
    $('#nav a').removeClass('active');
    
    // add active class to the current link
    $(t).addClass('active');    
}

function getParameterByName(name) {
    name= name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");

    var regexS = "[\\?&]" + name + "=([^&#]*)";
    var regex = new RegExp(regexS);
    var results = regex.exec(window.location.search);

    if(results == null)
        return "";
    return decodeURIComponent(results[1].replace(/\+/g, " "));
} 
