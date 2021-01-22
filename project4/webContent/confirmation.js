function handleConfirmation(resultData){
    if (!resultData["isPaid"])
        window.location.replace("main-page.html");

    $("#userInfo").text("welcome, "+resultData['userName']);
    let cart = resultData['cart_array'];
    let res = "";
    for (let i = 0; i < cart.length; i++) {
        // each item will be in a bullet point
        res += "<li class=\"list-group-item d-flex justify-content-between\">";
        res += "<span class=\"my-0\">"+cart[i]["sale_id"]+"</span>";
        res += "<span class='col-4'><h6 class=\"my-0\"><a href = 'single-movie.html?id="+ cart[i]['movie_id'] +"'>" + cart[i]['movie_title'] +"</a></h6></span>";
        res += "<span class=\"text-muted\">$"+cart[i]["movie_price"]+"</span>";
        res += "<span class=\"text-muted\">"+cart[i]["quantity"]+"</span>";
        res += "</li>";
    }
    res+= "<li class=\"list-group-item d-flex justify-content-between\"><span>Total (USD)</span> <strong>$"+resultData['cart_total']+"</strong></li>";
    $("#total_item_num").append(resultData['cart_size']);
    $("#cart").append(res);
}

$.ajax("api/confirmation", {
    method:"GET",
    success: handleConfirmation
});