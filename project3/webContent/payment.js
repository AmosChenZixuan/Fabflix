function handleOrderInfo(resultData){
    if (resultData['cart_size'] === 0)
        window.location.replace("main-page.html")
    $("#userInfo").text("welcome, "+resultData['userName']);

    let last_result = resultData['last_result'];
    if (last_result['status'] != null){
        $("#payment_failed_message").text(last_result['message']);
    }

    let total_item_num = document.getElementById("total_item_num");
    let cart = resultData["cart_array"];
    let cart_list = document.getElementById("shopping_cart_list");
    for(let i = 0; i< cart.length; ++i){
        let m_title = cart[i]["movie_title"];
        let price = cart[i]["movie_price"];
        let quantity = cart[i]["quantity"];
        cart_list.innerHTML+= gen_shoppingHTML(m_title,price,quantity);
    }
    cart_list.innerHTML+="<li class=\"list-group-item d-flex justify-content-between\"><span>Total (USD)</span> <strong>$"+resultData['cart_total']+"</strong></li>"
    total_item_num.innerHTML = resultData['cart_size'];
}
function gen_shoppingHTML(product_name,price,quantity){
    let ret = "<li class=\"list-group-item d-flex justify-content-between lh-condensed\"><div>";
    ret += "<h6 class=\"my-0\">"+product_name+" * "+quantity+"</h6></div>";
    ret += "<span class=\"text-muted\">$"+price+"</span></li>";
    return ret;
}

function handleOrderResult(resultData){
    if (resultData['status'] > 0)
        window.location.replace("confirmation.html");
    else
        window.location.reload();
}

function handlePlaceOrder(event, form_id){
    event.preventDefault();
    let form = $("#"+form_id);
    $.ajax("api/payment", {
        method:"POST",
        data: form.serialize(),
        success: handleOrderResult
    });
}

$.ajax("api/payment", {
    method:"GET",
    success: handleOrderInfo
});