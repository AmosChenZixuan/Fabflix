var slider = document.getElementById("myRange");
//var output = document.getElementById("demo");
//output.innerHTML = "select movie limit: "+slider.value ;
$("#set-slider-btn").text("Set to "+ slider.value); //Display the default slider value

// Update the current slider value (each time you drag the slider handle)
slider.oninput = function() {
    //output.innerHTML = "select movie limit: "+this.value;
    $("#set-slider-btn").text("Set to "+ this.value);
};