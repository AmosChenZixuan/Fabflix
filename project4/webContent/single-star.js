function checkNull(text){
    return (text == null)? "N/A" : text;
}

function handleStarResult(resultData) {
    //console.log("handleStarResult: populating star info from resultData");
    if (resultData.length < 1)
        return;
    $("#userInfo").text("welcome, "+resultData['userName']);
    $("#total_item_num").append(resultData['cart_size']);

    let star = resultData['star'];
    let starName = jQuery("#star_name");
    starName.append("<p><b>Star Name:</b> " + star[0]["star_name"] + "</p>");
    let starBirthYear = jQuery("#star_birth_year");
    starBirthYear.append("<p><b>Year Of Birth:</b> " + checkNull(star[0]["star_birth_year"]) + "</p>");


    let movie_links = star.map((val, i, arr) => {
        return "<a href = 'single-movie.html?id=" + val['movie_id'] + "' title='Release Year:"+val['movie_year']+"'>" +
            val['movie_title'] + "</a>";
    });
    let start_movies = "<p><b>Movies:</b> " + movie_links.join(', ') + "</p>";
    $("#star_movie").append(start_movies)
    /*console.log("handleStarResult: populating movie table from resultData");
    let movieTableBodyElement = jQuery("#movie_table_body");
    for (let i = 0; i < star.length; i++) {
        let rowHTML = "<tr>";
        rowHTML += "<th>" +
            "<a href = 'single-movie.html?id=" + star[i]['movie_id'] + "'>" +
            star[i]['movie_title'] + "</a>" +
            "</th>";
        rowHTML += "<th>----</th>"
        rowHTML += "<th> Year:" + star[i]["movie_year"] + "</th>";
        rowHTML += "<th>----</th>"
        rowHTML += "<th> Director:" + star[i]["movie_director"] + "</th>";
        rowHTML += "</tr><br>";
        movieTableBodyElement.append(rowHTML);
    }*/
}

function handleFail(resultData) {
    //console.log("We got a problem");
    let errmsgElement = jQuery("#errorMessage");
    errmsgElement.append("<p> Error Message:" + resultData["errorMessage"] + "</p>");
}

let starId = new URLSearchParams(window.location.search).get('id');

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/single-star?id=" + starId,
    success: (resultData) => handleStarResult(resultData),
    error: (resultData) => handleFail(resultData)
});