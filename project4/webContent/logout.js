function submitLogout(event){
    event.preventDefault();
    $.ajax("api/index", {
        method:"POST",
        data: {logout: true}
    });
    window.location.replace("index.html");
}