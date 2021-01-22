var slider = document.getElementById("myRange");
var output = document.getElementById("demo");
output.innerHTML = "num per page:"+slider.value ; // Display the default slider value

// Update the current slider value (each time you drag the slider handle)
slider.oninput = function() {
    output.innerHTML = "num per page:"+this.value;
}