function adjustPaginationHeader() {

    if(hasPrevious()) {
        $("#previous").removeClass("disabled");
    } else {
        $("#previous").addClass("disabled");
    }

    if(hasNext()) {
        $("#next").removeClass("disabled");
    } else {
        $("#next").addClass("disabled");
    }
}

function hasPrevious() {
    return currentPage > 0;
}

function hasNext() {
    return currentPage < totalPages - 1;
}

function populateTable() {
    jQuery.getJSON('json?p=' + currentPage, function(data) {

        $("#computers").empty();

        $.get('assets/computerEntry.mustache', function(template) {
            var output = Mustache.render(template, data);
            $("#computers").append(output);
        });
        //ensures we are at the correct page
        currentPage = data.currentPage;
        totalPages = Math.ceil(data.total / 10);
        var offset = (currentPage * 10) + 1;
        $("#offset").empty();
        $("#offset").append(offset);
        var offsetUpperbound = offset + data.computers.length - 1;
        $("#offsetUpperbound").empty();
        $("#offsetUpperbound").append(offsetUpperbound);
        $("#total").empty();
        $("#total").append(data.total);

    }).error(function(error) {
        alert("error" + error);
    });
    var History = window.History;
    // Note: We are using a capital H instead of a lower h
    if(History.enabled) {
        var stateObj = {
            p : currentPage
        };
        History.pushState(stateObj, "nothing", "computers?p=" + currentPage);
    }
}

function editLoad(computerId) {
    jQuery.getJSON('/json/' + computerId, function(data) {

        $("#edit").empty();

        $.get('/assets/computerEdit.mustache.html', function(template) {
            var output = Mustache.render(template, data);
            $("#edit").append(output);
        });
    }).error(function(error) {
        alert("error" + error);
    });
}

function update(computerId, jsonStream) {
    $.ajax({
        type : "POST",
        url : '/json/update/' + computerId,
        data : jsonStream,
        success : function(data) {

            $.get('assets/OneComputerEntry.mustache', function(template) {
                var output = Mustache.render(template, data);
                var tr = $("tr[id=" + computerId + "]");
                tr.empty();
                tr.append(output);
                //add class for all td children of this line
                var children = $("tr[id=" + computerId + "] > td");
                children.addClass("justEdited");
                children.animate({
                    backgroundColor : "white"
                }, 'slow', function() {
                    $(this).removeClass("justEdited");
                    $(this).removeAttr("style");
                });
            });
        },
        contentType : "application/json",
        processData : false
    }).fail(function(html) {
        alert("Oups...no edit has been done! Sorry.");
    });
};

function show(sectionId) {

    //hides all sections
    $("section").removeClass("showing");
    $("section").addClass("hidden");

    $(sectionId).removeClass("hidden");
    $(sectionId).addClass("showing");
}


$(document).ready(function() {
    var offset = parseInt($("#offset").text());
    currentPage = Math.ceil((offset - 1) / 10);
    totalPages = Math.ceil($("#total").text() / 10);
    // alert(currentPage + " ; " + totalPages);
    $("#next").click(function(event) {
        if(hasNext()) {
            currentPage++;
            populateTable();
        }
        adjustPaginationHeader();
    });
    $("#previous").click(function(event) {
        if(hasPrevious()) {
            currentPage--;
            populateTable();
        }
        adjustPaginationHeader();
    });

    $("a[forEdit=yes]").click(function(event) {
        var computerId = $(this).attr("id");
        show("#edit");
        editLoad(computerId);
        event.preventDefault();
    });
});
var currentPage = 0;
var totalPages = 1;