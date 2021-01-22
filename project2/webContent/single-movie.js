function add_to_cart(event){
    event.preventDefault();
    alert($("#movie_title").val() + " has been added to your cart");
    $.ajax("api/shopping-cart",{
        method:"POST",
        data:{action:"add", movie_id:movieId, movie_title:$("#movie_title").val()}
    });
    let total_item_num = document.getElementById("total_item_num");
    let num = parseInt(total_item_num.innerHTML) + 1;
    total_item_num.innerHTML = num+"";
}

function checkNull(text){
    return (text == null)? "N/A" : text;
}

function browse_search(linkID){
    event.preventDefault();
    $.ajax("api/main-page", {
        dataType: "json",
        method:"POST",
        data:{"action":"browse_genre", "genre":$("#"+linkID).val()},
        success:function(data){}
    });
    window.location.replace("main-page.html");
}

function genre_link(gener_array, movie_id){
    let html = "<div>";
    for (let i = 0; i < gener_array.length; i++){
        let g = gener_array[i];
        html += "<button class=\"m-0 p-0 btn btn-link\" id=\"Genre"+i+"_id_\" onclick=\"browse_search('Genre"+i+"_id_')\" value=\""+g+"\">"+g+"</button><br>"
    }
    return html;
}

function handleMovieResult(resultData) {
    //console.log("handleMovieResult: populating movie info from resultData");
    if (resultData.length < 1)
        return;
    $("#userInfo").text("welcome, "+resultData['userName']);
    $("#total_item_num").append(resultData['cart_size']);

    let movie_title = jQuery("#movie_title");
    movie_title.val(resultData["movie_title"]);
    movie_title.append("<p><b>Title:</b> " + resultData["movie_title"] + "</p>");
    let movie_year = jQuery("#movie_year");
    movie_year.append("<p><b>Release Year:</b> " + resultData["movie_year"] + "</p>");
    let movie_director = jQuery("#movie_director");
    movie_director.append("<p><b>Director:</b> " + resultData["movie_director"] + "</p>");
    let movie_rating = jQuery("#movie_rating");
    movie_rating.append("<p><b>Rating:</b> " + checkNull(resultData["movie_rating"]) + "</p>");
    let movie_genre = jQuery("#movie_genre");
    //movie_genre.append("<p><b>Genres:</b> " + resultData["movie_genre"].join(" & ") + "</p>");
    movie_genre.append("<b>Genres:</b>" + genre_link(resultData["movie_genre"], movieId));

    //console.log("handleStarResult: populating star table from resultData");
    let movieTableBodyElement = jQuery("#star_table_body");
    let stars = resultData["movie_star"];
    for (let i = 0; i < stars.length; i++) {
        let rowHTML = "<tr>";
        rowHTML += "<th>" +
            "<a href = 'single-star.html?id=" + stars[i]['star_id'] + "' title='Birth Year: "+ checkNull(stars[i]["star_birth_year"]) +"'>" +
            stars[i]['star_name'] + "</a>" +
            "</th>";
        //rowHTML += "<th>" + checkNull(stars[i]["star_birth_year"]) + "</th>";
        rowHTML += "</tr>";
        movieTableBodyElement.append(rowHTML);
    }
}


function handleFail(resultData) {
    //console.log("We got a problem");
    let errmsgElement = jQuery("#errorMessage");
    errmsgElement.append("<p> Error Message:" + resultData["errorMessage"] + "</p>");
}

let movieId = new URLSearchParams(window.location.search).get('id');

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/single-movie?id=" + movieId,
    success: (resultData) => handleMovieResult(resultData),
    error: (resultData) => handleFail(resultData)
});