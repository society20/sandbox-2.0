var geocoder;
var map;

function initialize() {
	geocoder = new google.maps.Geocoder();
	// var latlng = new google.maps.LatLng(-34.397, 150.644);

	var myOptions = {
		zoom : 8,
		//center : latlng,
		mapTypeId : google.maps.MapTypeId.ROADMAP
	}
	map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
}

function displayOnGoogleMaps() {
	var item = $("#address").data("placeSelected");

	map.setCenter(item.data.geometry.location);
	var marker = new google.maps.Marker({
		map : map,
		position : item.data.geometry.location
	});
}


$(document).ready(function() {
	$("#address").autocomplete({
		source : function(request, response) {
			geocoder.geocode({
				'address' : request.term
			}, function(results, status) {
				if(status == google.maps.GeocoderStatus.OK) {
					response($.map(results, function(item) {
						return {
							label : item.formatted_address,
							value : item.formatted_address,
							data : item
						}
					}));
				} else {
					alert("Geocode was not successful for the following reason: " + status);
				}
			});
		},
		minLength : 3,
		select : function(event, ui) {
			$(this).data("placeSelected", ui.item);
		}
	});

	initialize();
});
