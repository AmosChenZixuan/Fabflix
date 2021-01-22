function handleLookup(query, doneCallback) {
    console.log("Autocomplete Initiated");
    //  check past query results first
    let cachedResult = fetchCachedResult(query);
    if (cachedResult != null) {
        console.log("Past result found in the Cache! Retrieved from cache");
        console.log("Retrieved from Cache:");
        console.log(cachedResult);
        doneCallback( { suggestions: cachedResult } );
        return;
    }
    console.log("New query, sending AJAX request to backend Java Servlet");
    jQuery.ajax({
        "method": "GET",
        // generate the request url from the query.
        // escape the query string to avoid errors caused by special characters
        "url": "api/movie-suggestion?query=" + escape(query),
        "success": function(data) {
            // pass the data, query, and doneCallback function into the success handler
            handleLookupSuccess(data, query, doneCallback)
        },
        "error": function(errorData) {
            console.log("lookup ajax error");
            console.log(errorData)
        }
    })
}

function handleLookupSuccess(data, query, doneCallback) {
    console.log("Retrieved data from Servlet:");
    console.log(data);
    // cache to sessionStorage
    cacheResult(query, data);
    doneCallback( { suggestions: data } );
}

function handleSelectSuggestion(suggestion) {
    // jump to the specific result page based on the selected suggestion
    let id = suggestion["data"]["movieId"];
    console.log("you select " + suggestion["value"] + " with ID " + id);
    window.location.replace("single-movie.html?id="+id);
}


$('#fulltext-bar').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    minChars: 3
});

let cache_size = 100;
let cache_name = "resultCache";
let session = window.sessionStorage;
let default_cache = "{\"key\":[]}";
function cacheResult(query, data){
    let cache = session.getItem(cache_name);
    if (cache == null){
        session.setItem(cache_name, default_cache);
    }
    let json = JSON.parse(cache);
    query = query.trim();

    // pop first if cache is full
    if (json['key'].length >= cache_size){
        let toPop = json['key'].shift();
        delete json[toPop];
    }
    json['key'].push(query);
    json[query] = data;
    session.setItem(cache_name, JSON.stringify(json));
}

function fetchCachedResult(query){
    let cache = session.getItem(cache_name);
    if (cache == null){
        session.setItem(cache_name, default_cache);
        return null
    }
    return JSON.parse(cache)[query.trim()];
}