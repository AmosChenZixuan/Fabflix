function submitForm(event, id){
    event.preventDefault();
    $.ajax("api/shopping-cart", {
        method:"POST",
        data:$("#"+id).serialize(),
    });
    window.location.reload();
}

function submitEmptyCart(submitEvent){
    submitEvent.preventDefault();
    alert("Your cart is currently empty!");
}

function handleReadCartResult(resultData){
    //console.log("populating cart");
    $("#userInfo").text("welcome, "+resultData['userName']);

    let total_item_num = document.getElementById("total_item_num");
    let cart = resultData['cart_array'];
    // change it to html list
    let res = "";
    for (let i = 0; i < cart.length; i++) {
        // each item will be in a bullet point
        res += "<li class=\"list-group-item d-flex justify-content-between\">";
        res += "<div class='col-3'><h6 class=\"my-0\"><a href = 'single-movie.html?id="+ cart[i]['movie_id'] +"'>" + cart[i]['movie_title'] +"</a></h6></div>";
        res += "<span class=\"text-muted\">$"+cart[i]["movie_price"]+"</span>";
        res += "<form style=\"text-align: center\" id='update_form_"+i+"' onsubmit='submitForm(event, this.id)'>";
        res += "<input type='hidden' name='action' value='update'/>";
        res += "<input type='hidden' name='index' value='"+i+"'/>";
        res += "<input type='number' name='quantity' class=\"w-25\" value='"+ cart[i]['quantity']+"'/>";
        res += "<input class=\"btn btn-secondary btn-sm\" type='submit' value='Update'/> "+"</form>";
        res += "<form class=\"ml-0\"  id='delete_form_"+i+"' onsubmit='submitForm(event, this.id)'>";
        res += "<input type='hidden' name='action' value='delete'/>";
        res += "<input type='hidden' name='index' value='"+i+"'/>";
        res += "<input class=\"btn btn-secondary btn-sm\" type='submit' value='Delete'/></form></li>";
    }
    // print summary
    if (resultData['cart_total'] == 0){
        res += "<p style='color: red; margin-top: auto'>Your Cart is Empty</p>";
        $("#payment").submit(submitEmptyCart);
    }

    res+= "<li class=\"list-group-item d-flex justify-content-between\"><span>Total (USD)</span> <strong>$"+resultData['cart_total']+"</strong></li>";
    total_item_num.innerHTML = resultData['cart_size'];
    $("#cart").append(res);
}

$.ajax("api/shopping-cart", {
    method: "GET",
    success: handleReadCartResult
});