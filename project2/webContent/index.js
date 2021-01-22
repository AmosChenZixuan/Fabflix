let login_form = $("#login_form");


function handleLoginResult(resultData) {
    //resultData = JSON.parse(resultData);
    //console.log("login status:" + resultData['status']);
    //console.log(resultData['message']);
    if (resultData['status'] > 0)
        window.location.replace("main-page.html");
    else
        $("#login_error_message").text(resultData["message"]);
}

function handleError(resultData){
    //console.log("handle login error");
    $("#login_error_message").text("Error!: " + resultData["message"]);
}

function submitLoginForm(formSubmitEvent) {
    //console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/index", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: login_form.serialize(),
            success: handleLoginResult,
            error: handleError
        }
    );
}

// Bind the submit action of the form to a handler function
login_form.submit(submitLoginForm);