
function submitForm(event, id, title){
    event.preventDefault();
    alert(title + " has been added to your cart");
    $.ajax("api/shopping-cart", {
        method:"POST",
        data:$("#"+id).serialize()
    });
    let total_item_num = document.getElementById("total_item_num");
    let num = parseInt(total_item_num.innerHTML) + 1;
    total_item_num.innerHTML = num+"";
}
//-----------------------------------------------search-------------------------------------------------

function click_search(){
    // is not empty form post the request
    event.preventDefault();
    let data_sent = $("#main_form").serializeArray();
    data_sent.push({name:"action",value:"search"});
    //if(!is_empty_form()){
        $.ajax("api/main-page", {
            method:"POST",
            data:data_sent,
            success:function(data){}
        })
    //}
    window.location.reload();
}

function click_nxt(){
    let data_sent = {"action":"next"};
     $.ajax("api/main-page", {
        method:"POST",
        data:data_sent,
        success:function(data){}
    });
    window.location.reload();
}

function click_pre(){
    let data_sent = {"action":"pre"};
    $.ajax("api/main-page", {
        method:"POST",
        data:data_sent,
        success:function(data){}
    });
    window.location.reload();
}
function sortBy(){
    let e = document.getElementById("sort_form");
    let data_sent = {"action":"sort", "sortType":e.options[e.selectedIndex].value}
    $.ajax("api/main-page", {
        method:"POST",
        data:data_sent,
        success:function(data){}
    });
    window.location.reload();
}
function set_movie_num(){
    let slider = document.getElementById("myRange");
    // slider.innerHTML = "num per page:"+slider.value;
    let data_sent = {"action":"dis_num", "num":slider.value};
    $.ajax("api/main-page", {
        dataType: "json",
        method:"POST",
        data:data_sent,
        success:function(data){}
    });
    window.location.reload();
}

function browse_search(linkID){
    let browse_info = $("#"+linkID).val();
    let data_sent = {"action":"browse_genre", "genre":browse_info};
    event.preventDefault();
    //console.log(data_sent);
    //console.log(linkID);

    $.ajax("api/main-page", {
        dataType: "json",
        method:"POST",
        data:data_sent,
        success:function(data){}
    });
    window.location.reload();
}


function is_empty_form(){
    let t = $("#mo_t").val();
    let d = $("#mo_d").val();
    let s = $("#mo_s").val();
    let y = $("#mo_y").val();
    return t == "" && d=="" && s==""&&y=="";
}
function genre_link(array_of_genre, movie_id){
    let n = array_of_genre.length;
    let html ="";

    for(let i=0; i<n; ++i){
        let g = array_of_genre[i];
        html += "<button class=\"m-0 p-0 btn btn-link\" id=\"Genre"+i+movie_id+"_id_\" onclick=\"browse_search('Genre"+i+movie_id+"_id_')\" value=\""+g+"\">"+g+"</button><br>";
    }
    return html;
}
//-----------------------------------------------search------------------------------------
function refillSearchBar(search_info){
    //console.log(search_info);
    if(search_info['title'] !== "")
        $("#mo_t").val(search_info['title']);
    if(search_info['year'] !== "")
        $("#mo_y").val(search_info['year']);
    if(search_info['director'] !== "")
        $("#mo_d").val(search_info['director']);
    if(search_info['star'] !== "")
        $("#mo_s").val(search_info['star']);
}

function handelMovieList(resultData){
    //console.log("handelMovieList: populating movie table from resultData");
    $("#userInfo").text("welcome, "+resultData['userName']);
    $("#total_item_num").append(resultData['cart_size']);

    let tableBody = jQuery("#movie_table_body");
    let movieList = resultData['movieList'];
    $('#page_number').html(resultData["page_num"]);
    $('#current_movie_num').html("Number of Listings: "+resultData["movie_limit"]["movie_limit"]);

    // remember and auto-refill user's search and sort
    if (resultData["sort_type"] != null)
        $("#sort_form").val(resultData["sort_type"]);
    if (resultData["search_info"] != null)
        refillSearchBar(resultData["search_info"]);

    for (let i = 0; i < Math.min(resultData["movie_limit"]["movie_limit"], movieList.length); i++) {
        let pic_url = "image/placeholder_pic.jpg"; //hard coded pic path
        let star_links = movieList[i]["movie_star"].map((val, i, arr) => {
            return "<a href = 'single-star.html?id=" + val['star_id'] + "'>" +
                val['star_name'] + "</a>";
        });
        let rowHTML ="";
        rowHTML += "<div rel =\"movie_tag\"  class=\"container\">";
        rowHTML += " <div class=\"row my_row pic_row\">";
        rowHTML += "<div class=\"col-5 mt-1 ml-1 my_col align-item-start\" style=\"background-image: url('" + pic_url + "')\">";
        rowHTML += "</div><div class='col-1'></div>";
        rowHTML += "<div class = \"col-4 movie_title my_col \">";
        rowHTML += "<div class='mt-1'><b>Movie Title:</b> </div><a href = 'single-movie.html?id=" + movieList[i]['movie_id'] + "'>" + movieList[i]['movie_title'] + "</a>";
        rowHTML += "<div class='mt-3'><b>Director:</b> </div> <div class=\"movie_director\">"+movieList[i]["movie_director"] +"</div>";
        rowHTML += "</div>";
        rowHTML += "</div>";
        rowHTML += "<div class=\"row my_row \">";
        rowHTML += "<div class = \"col-2 my_col movie_year \">"+"<b>Year:</b> "+movieList[i]["movie_year"] +"</div>";
        rowHTML += "<div class = \"col-2 my_col movie_rating \">"+ "<b>Rating:</b> "+ movieList[i]["movie_rating"] +"</div>";
        rowHTML += "<div class = \"col-2 my_col movie_genres \" ><div><b>Genres:</b> </div>" + "<div>"+genre_link(movieList[i]["movie_genre"],movieList[i]["movie_id"]) + "</div></div>";
        rowHTML += "<div class = \"col-4 ml-1 my_col\"><div><b>Actors:</b></div>";
        rowHTML += star_links.join("<br/>");
        rowHTML+= "</div>";
        rowHTML+= "</div>";
        rowHTML+="<div class=\"row my_row\">";
        rowHTML+= "<div class = \"col my_col\">";
        rowHTML+="<form id='form-"+i+"' onsubmit=\"submitForm(event, this.id, '"+movieList[i]['movie_title']+"')\">" +
            "<input type='hidden' name='action' value='add'>" +
            "<input type='hidden' name='movie_id' value=\'" + movieList[i]['movie_id'] +"\' />" +
            "<input type='hidden' name='movie_title' value=\'" + movieList[i]['movie_title'] +"\' />" +
            "<button class='buy_class btn btn-danger mb-2' >Add to Cart</button>" + "</form>";
        rowHTML+= "</div>";
        rowHTML+= "</div>";
        rowHTML+= "</div>";
        tableBody.append(rowHTML);
    }
    let genres = resultData["genres"];
    let browse_genre_body = $("#browse_genre_body");
    for (let i = 0; i <genres.length; i++) {
        let g = genres[i]["genre_name"];
        let html = "<button id=\"Genre"+i+"_id_\" onclick=\"browse_search('Genre"+i+"_id_')\" value=\""+g+"\" class=\"btn btn-secondary m-2\">"+g+"</button>";
        browse_genre_body.append(html);
    }
    let brose_title_body = $("#brose_title_body");
    for (let i = 0; i <10; i++) {
        let html = "<button id=\"letter"+i+"_id_\" onclick=\"browse_search('letter"+i+"_id_')\" value=\""+i+"\" class=\"btn btn-secondary m-2\">"+i+"</button>";
        brose_title_body.append(html);
    }
    let char = 'A';
    for (let i = 10, j=0; j <26; i++,j++) {
        let html = "<button id=\"letter"+i+"_id_\" onclick=\"browse_search('letter"+i+"_id_')\" value=\""+char+"\" class=\"btn btn-secondary m-2\">"+char+"</button>";
        char = String.fromCharCode(char.charCodeAt(0) + 1);
        brose_title_body.append(html);
    }
}

function handleFail(resultData) {
    //console.log("We got a problem");
    let errmsgElement = jQuery("#errorMessage");
    errmsgElement.append("<p> Error Message:" + resultData["errorMessage"] + "</p>");
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/main-page",
    success: (resultData) => handelMovieList(resultData),
    error: (resultData) => handleFail(resultData)
});
