
function submitMainForm(event){
    event.preventDefault();
    console.log($("#dashboard-main-form").serialize());
    $.ajax("api/dashboard", {
        method:"POST",
        data: $("#dashboard-main-form").serialize(),
        success: handleFormResult
    });
}

function handleFormResult(resultData){
    let status = resultData['status'];
    if (status === -1)
        document.getElementById("message-box").style.background = "pink";
    else if (status === 0)
        document.getElementById("message-box").style.background = "white";
    else
        document.getElementById("message-box").style.background = "lightblue";
    $("#message").text(resultData['message']);
}
// <div class="container">
//     <div class="row">
//     <div class="col-2">id</div>
//     <div class="col-3">varchar(20)</div>
//     <div class="col-2">PRI</div>
//     </div>
//     </div>
function create_modal_model(data, schema_name){
    let table_content = "<div class=\"container\">" + "<div class=\"row\">";
    for(let i=0; i<data.length;i++){
        let name = data[i]["name"];
        let type = data[i]["type"];
        let key = data[i]["key"];
        let extra = data[i]["extra"];
        table_content += " <div class=\"col-3\">"+ name +"</div>";
        table_content += " <div class=\"col-3\">"+ type +"</div>";
        if(key != "")
            table_content += " <div class=\"col-3\">"+ key +"</div>";
        if (extra !="")
            table_content += " <div class=\"col-3\">"+ extra +"</div>";
        table_content+= "</div>";
        if (i+1 <data.length){
            table_content+="<div class=\"row\">";
        }
    }
    table_content+="</div>";
    let model = "<div class='modal fade' id='" + schema_name+"Modal' role='dialog'>";
        model +=    '<div class="modal-dialog" role="document">';
        model +=        "<div class=\"modal-content\">";
        model +=            '<div class="modal-header">';
        model +=                "<h5 class=\"modal-title\">" + schema_name+"Schema</h5>";
        model +=                "<button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\">";
        model +=                    "<span aria-hidden=\"true\">&times;</span>";
        model +=                "</button>";
        model +=            "</div>";
        model +=            "<div class=\"modal-body m-2\">"
        model +=                "<div id='" + schema_name + "'>" + table_content+"</div>";
        model +=            "</div>";
        model +=        "</div>";
        model +=    "</div>";
        model += "</div>";
        console.log(model);
        return model;
}


function handleDashboard(resultData){
    if (!resultData["isAllowed"]){
        window.location.replace("main-page.html");
    }
    $("#userInfo").text("welcome, "+resultData['userName']);
    let metaData = resultData['metaData'];
    let schema_names = "";
    let schema_menu = document.getElementById("schema_menu");
    let modal_content = "";
    let modal_collection = document.getElementById("modals");
    for (let table in metaData) {
        schema_names += " <button class=\"dropdown-item\" data-toggle=\"modal\" data-target='#" + table +"Modal'" +"href=\"#\">" +table+"</button>";
        modal_content += create_modal_model(metaData[table]['columns'],table);
    }
    schema_menu.innerHTML = schema_names;
    modal_collection.innerHTML = modal_content;
    console.log(modal_content);

}

$.ajax("api/dashboard", {
    method:"GET",
    success: handleDashboard
});