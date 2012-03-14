function adjustPaginationHeader(currentPage, total) {

	var totalPages = Math.ceil(total / 10);

	if(currentPage == 1) {
		$("#previous").addClass("disabled");
	} else {
		$("#previous").removeClass("disabled");
	}

	if(currentPage < totalPages) {
		$("#next").removeClass("disabled");
	} else {
		$("#next").addClass("disabled");
	}
}

function populateTable() {
	jQuery.getJSON('assets/test.json?p=6', function(data) {

		$("#computers").empty();

		$.get('assets/computerEntry.mustache', function(template) {
			var output = Mustache.render(template, data);
			$("#computers").append(output);
		});
		adjustPaginationHeader(data.current_page, data.total);

	}).error(function(error) {
		alert("error" + error);
	});
	
	var stateObj = { foo: "bar" };
	history.pushState(stateObj, "page 2", "table.PHP!!!?p=5");
}


$(document).ready(function() {
	$("#next").click(function(event) {
		populateTable();
	});
	$("#previous").click(function(event) {
		populateTable();
	});
});
