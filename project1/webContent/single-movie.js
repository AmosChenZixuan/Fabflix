function checkNull(text){
    return (text == null)? "N/A" : text;
}

function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie info from resultData");

    if (resultData.length < 1)
        return;

    let movie_title = jQuery("#movie_title");
    movie_title.append("<p>Title: " + resultData["movie_title"] + "</p>");
    let movie_year = jQuery("#movie_year");
    movie_year.append("<p>Release Year: " + resultData["movie_year"] + "</p>");
    let movie_director = jQuery("#movie_director");
    movie_director.append("<p>Director: " + resultData["movie_director"] + "</p>");
    let movie_rating = jQuery("#movie_rating");
    movie_rating.append("<p>Rating: " + checkNull(resultData["movie_rating"]) + "</p>");
    let movie_genre = jQuery("#movie_genre");
    movie_genre.append("<p>Genres: " + resultData["movie_genre"].join(" & ") + "</p>");

    console.log("handleStarResult: populating star table from resultData");
    let movieTableBodyElement = jQuery("#star_table_body");
    let stars = resultData["movie_star"];
    for (let i = 0; i < stars.length; i++) {
        let rowHTML = "<tr>";
        rowHTML += "<th>" +
            "<a href = 'single-star.html?id=" + stars[i]['star_id'] + "'>" +
            stars[i]['star_name'] + "</a>" +
            "</th>";
        rowHTML += "<th>" + checkNull(stars[i]["star_birth_year"]) + "</th>";
        rowHTML += "</tr>";
        movieTableBodyElement.append(rowHTML);
    }
}


function handleFail(resultData) {
    console.log("We got a problem");
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