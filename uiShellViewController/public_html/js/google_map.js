var map;
var geocoder;
var orig_coordinates;
var orig_marker;

function initialize(event) {
    var source = event.getSource();
    AdfCustomEvent.queue(source, "loadGoogleMap", 
    {
    },
false);
}

function initializeMap(clientId, latitude, longitude, address, countryCode) {
    if (GBrowserIsCompatible()) {
        map = new google.maps.Map2(document.getElementById(clientId));
        map.addControl(new GLargeMapControl());
        map.addControl(new GMapTypeControl());
        GEvent.addListener(map, 'click', getAddress);
        geocoder = new GClientGeocoder();
        //tailor to a particular domain (country)     
        if (countryCode) {
            geocoder.setBaseCountryCode(countryCode);
        }
        if (latitude != null && longitude != null) {
            //valid coordinates
            //set center of map to the coordinates and add a marker 
            orig_coordinates = new GLatLng(latitude, longitude);
            map.setCenter(orig_coordinates, 15);
            orig_marker = new GMarker(orig_coordinates);
            map.addOverlay(orig_marker);
            var info = "Original Coordinates: " + latitude + "," + longitude;
            addClickListener(orig_marker, info);
        }
        else {
            //none valid coordinates so try to find the address instead
            if (address != null) {
                findAddress(address);
            }
            else {
                //address is null so set center to default
                map.setCenter(new GLatLng(0, 0), 1);
            }
        }
    }
}

function getAddress(overlay, latlng) {
    //if far enough return
    if (map.getZoom() < 14) {
        return;
    }
    if (latlng != null) {
        var icon = createArrowIcon();
        arrow_marker = new GMarker(latlng, 
        {
            icon : icon
        });
        map.clearOverlays();
        map.addOverlay(arrow_marker);
        addClickListener(arrow_marker, latlng.toUrlValue());
        if (orig_marker) {
            map.addOverlay(orig_marker);
            var info = "Original Coordinates: " + orig_coordinates.toUrlValue();
            addClickListener(orig_marker, info);
        }
        //Reverse GeoCode
        geocoder.getLocations(latlng, showAddress);
    }
}

function createArrowIcon() {
    var icon = new GIcon();
    var url = "http://maps.google.com/mapfiles/";
    icon.image = url + "arrow.png";
    icon.shadow = url + "arrowshadow.png";
    icon.iconSize = new GSize(39, 34);
    icon.shadowSize = new GSize(39, 34);
    icon.iconAnchor = new GPoint(20, 34);
    icon.infoWindowAnchor = new GPoint(20, 0);
    return icon;
}

function showAddress(response) {
    if (!response || response.Status.code != 200) {
        alert("Status Code:" + response.Status.code);
    }
    else {
        submitInfoToServer(response);
        place = response.Placemark[0];
        plotPlace(place);
    }
}

function goFindAddress(event) {
    var searchField = event.getSource().findComponent("searchField");
    var input = searchField.getValue();
    findAddress(input);
    event.cancel();
}

function findAddress(input) {
    var address = input;
    //GeoCode
    if (geocoder) {
        geocoder.getLocations(input, function (response) {
            if (!response || response.Status.code != 200) {
                var index = address.indexOf(",");
                if (index ==  - 1) {
                    processPointNotFound();
                    return;
                }
                //simplify address by removing the details separated by comma
                address = address.substring(index + 1);
                //recurse until a point is found
                findAddress(address);
            }
            else {
                place = response.Placemark[0];
                plotPlace(place);
            }
        });
    }
}

function processPointNotFound() {
    alert("address not found");
    map.setCenter(new GLatlng(0, 0), 1);
}

function plotPlace(place) {
    var point = new GLatLng(place.Point.coordinates[1], place.Point.coordinates[0]);
    var marker = new GMarker(point);
    map.addOverlay(marker);
    map.setCenter(point, 14);
    var info = "<b>Result Coordinates:</b>" + point.toUrlValue() + "<br/>" + "<b>Address:</b>" + place.address + "<br/>" + "<b>Accuracy:</b>" + place.AddressDetails.Accuracy + "<br/>" + "<b>Country code:</b> " + place.AddressDetails.Country.CountryNameCode;
    addClickListener(marker, info);

}

function addClickListener(marker, info) {
    GEvent.addListener(marker, "click", function () {
        marker.openInfoWindowHtml(info);
    });
    marker.openInfoWindowHtml(info);
}

function submitInfoToServer(response) {
    var place = response.Placemark[0];
    var result_point = new GLatLng(place.Point.coordinates[1], place.Point.coordinates[0]);
    var address = place.address;
    var addressDetails = place.AddressDetails;
    var accuracy = addressDetails.Accuracy;
    var country = addressDetails.Country;
    var countryNameCode = country.CountryNameCode;
    var adminArea = country.AdministrativeArea;
    var adminAreaName;
    var locality;
    var localityName;
    var thoroughfare;
    var postalCode;
    if (adminArea != null) {
        adminAreaName = adminArea.AdministrativeAreaName;
        locality = adminArea.Locality;
        if (locality != null) {
            localityName = locality.LocalityName;
            thoroughfare = locality.Thoroughfare;
            postalCode = locality.PostalCode;
            if (thoroughfare != null) {
                thoroughfare = thoroughfare.ThoroughfareName;
            }
            if (postalCode != null) {
                postalCode = postalCode.PostalCodeNumber;
            }
        }
    }
    var source = AdfPage.PAGE.findComponent("d1");
    AdfCustomEvent.queue(source, "submitInfoToServer", 
    {
        selected_point : response.name, result_point:result_point , address : address, country : countryNameCode, area : adminAreaName, locality : localityName, thoroughfare : thoroughfare, postalCode : postalCode, accuracy : accuracy
    },
false);
}