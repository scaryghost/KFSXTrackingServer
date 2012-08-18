/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


var index= 0;
function toggle(direction) {
    var elems= document.getElementsByName("item")

    if (direction == "right") {
        index= (index+1) % elems.length;
    } else {
        index= (index-1+ elems.length) % elems.length;
    }
    
    var elem= elems[index];

    for(var x=0; x<elems.length; x++) {
            elems[x].style.display= "none";
    }
    elem.style.display = "block";
} 