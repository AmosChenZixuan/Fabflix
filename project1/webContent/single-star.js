function checkNull(text){
    return (text == null)? "N/A" : text;
}

function handleStarResult(resultData) {
    console.log("handleStarResult: populating star info from resultData");

    if (resultData.length < 1)
        return;

    let starName = jQuery("#star_name");
    starName.append("<p>Star Name: " + resultData[0]["star_name"] + "</p>");
    let starBirthYear = jQuery("#star_birth_year");
    starBirthYear.append("<p>Year Of Birth: " + checkNull(resultData[0]["star_birth_year"]) + "</p>");

    console.log("handleStarResult: populating movie table from resultData");
    let movieTableBodyElement = jQuery("#movie_table_body");

    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "<tr>";
        rowHTML += "<th>" +
            "<a href = 'single-movie.html?id=" + resultData[i]['movie_id'] + "'>" +
            resultData[i]['movie_title'] + "</a>" +
            "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "</tr>";
        movieTableBodyElement.append(rowHTML);
    }
}

function handleFail(resultData) {
    console.log("We got a problem");
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