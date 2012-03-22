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

// window.onpopstate = function(event) {
// // alert("location: " + document.location + ", state: " + JSON.stringify(event.state));
// var History = window.History;
// // Note: We are using a capital H instead of a lower h
// if(History.enabled) {
// var State = History.getState();
// alert(State.data + " " + State.title + " " + State.url);
// }
// };

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
});
var currentPage = 0;
var totalPages = 1; (function(window, undefined) {

	// Prepare
	var History = window.History;
	// Note: We are using a capital H instead of a lower h
	if(!History.enabled) {
		// History.js is disabled for this browser.
		// This is because we can optionally choose to support HTML4 browsers or not.
		return false;
	}

	//Bind to StateChange Event
	History.Adapter.bind(window, 'statechange', function() {// Note: We are using statechange instead of popstate
		var State = History.getState();
		// Note: We are using History.getState() instead of event.state

		alert(State.data + " " + State.title + " " + State.url);
		History.log(State.data, State.title, State.url);
	});
})(window);
