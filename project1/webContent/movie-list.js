
function handelMovieList(resultData){
    console.log("handelMovieList: populating movie table from resultData");

    let tableBody = jQuery("#movie_table_body");
    for (let i = 0; i < Math.min(20, resultData.length); i++) {
        let rowHTML = "<tr>";
        rowHTML += "<th>" +
            "<a href = 'single-movie.html?id=" + resultData[i]['movie_id'] + "'>" +
            resultData[i]['movie_title'] + "</a>" +
            "</th>>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_genre"].join("<br/>") + "</th>";

        let star_links = resultData[i]["movie_star"].map((val, i, arr) => {
            return "<a href = 'single-star.html?id=" + val['star_id'] + "'>" +
                val['star_name'] + "</a>";
        });
        rowHTML += "<th>" + star_links.join("<br/>") + "</th>";

        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";
        rowHTML += "</tr>";

        tableBody.append(rowHTML);
    }
}

function handleFail(resultData) {
    console.log("We got a problem");
    let errmsgElement = jQuery("#errorMessage");
    errmsgElement.append("<p> Error Message:" + resultData["errorMessage"] + "</p>");
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/movie-list",
    success: (resultData) => handelMovieList(resultData),
    error: (resultData) => handleFail(resultData)
});
