/*
 * If you want to query another API append this something like
 * &host=http://localhost:9000/
 * to the URL or overwrite the 'host' variable.*/

var tmpArgs = parseUrlWithHisto();

for (var key in tmpArgs)
{
    if (tmpArgs.hasOwnProperty(key)) {
        console.log("main.js..." + key + " = " + tmpArgs[key]);
    }

}
var host = tmpArgs["host"];
// var host = "http://graphhopper.com/api/1";
if (!host) {
    if (location.port === '') {
        host = location.protocol + '//' + location.hostname;
    } else {
        host = location.protocol + '//' + location.hostname + ":" + location.port;

    }
}

console.log("main.js...host = " + host);

var selectedWeighting = tmpArgs["weighting"];
var selectedElevation = tmpArgs["elevation"];
console.log("Elevation at the beginning of main.js if user directly submitted a query URL= " + selectedElevation);

var ghRequest = new GHRequest(host);

var bounds = {};

//var nominatim = "http://open.mapquestapi.com/nominatim/v1/search.php";
//var nominatim_reverse = "http://open.mapquestapi.com/nominatim/v1/reverse.php";
var nominatim = "http://nominatim.openstreetmap.org/search";
var nominatim_reverse = "http://nominatim.openstreetmap.org/reverse";
var routingLayer;
var map;
var browserTitle = "GraphHopper Maps - Driving Directions";
var defaultTranslationMap = null;
var enTranslationMap = null;
var routeSegmentPopup = null;
var elevationControl = null;

var iconFrom = L.icon({
    iconUrl: './img/marker-icon-green.png',
    shadowSize: [50, 64],
    shadowAnchor: [4, 62],
    iconAnchor: [12, 40]
});

var iconTo = L.icon({
    iconUrl: './img/marker-icon-red.png',
    shadowSize: [50, 64],
    shadowAnchor: [4, 62],
    iconAnchor: [12, 40]
});


$(document).ready(function (e) {
    // fixing cross domain support e.g in Opera
    jQuery.support.cors = true;

    if (isProduction())
        $('#hosting').show();

    var History = window.History;
    if (History.enabled) {
        History.Adapter.bind(window, 'statechange', function () {
            // No need for workaround?
            // Chrome and Safari always emit a popstate event on page load, but Firefox doesn’t
            // https://github.com/defunkt/jquery-pjax/issues/143#issuecomment-6194330

            var state = History.getState();
            initFromParams(state.data, true);
        });
    }



    $('#locationform').submit(function (e) {
        // no page reload
        e.preventDefault();
        mySubmit();
    });

    $('#gpxExportButton a').click(function (e) {
        // no page reload
        e.preventDefault();
        exportGPX();
    });

    var urlParams = parseUrlWithHisto();

    $.when(ghRequest.fetchTranslationMap(urlParams.locale), ghRequest.getInfo())
            .then(function (arg1, arg2) {
                // init translation retrieved from first call (fetchTranslationMap)
                var translations = arg1[0];

                // init language
                // 1. determined by Accept-Language header, falls back to 'en' if no translation map available
                // 2. can be overwritten by url parameter        
                ghRequest.setLocale(translations["locale"]);
                defaultTranslationMap = translations["default"];
                enTranslationMap = translations["en"];
                if (!defaultTranslationMap)
                    defaultTranslationMap = enTranslationMap;

                initI18N();

                var json = arg2[0];

                /*for (var key in json)
                 {
                 if (json.hasOwnProperty(key)) {
                 console.log("main.js, values of (ghRequest.getInfo of " + key + " = " + json[key]);
                 }
                 
                 }*/

                // init bounding box from getInfo result
                var tmp = json.bbox;
                bounds.initialized = true;
                bounds.minLon = tmp[0];
                bounds.minLat = tmp[1];
                bounds.maxLon = tmp[2];
                bounds.maxLat = tmp[3];

                var vehiclesDiv = $("#vehicles");
                function createButton(vehicle) {
                    var button = $("<button class='vehicle-btn' title='" + tr(vehicle) + "'/>");
                    button.attr('id', vehicle);
                    button.html("<img src='img/" + vehicle + ".png' alt='" + tr(vehicle) + "'></img>");
                    button.click(function () {
                        ghRequest.initVehicle(vehicle);
                        resolveFrom();
                        resolveTo();
                        routeLatLng(ghRequest);
                    });

                    return button;
                }


                if (json.features) {
                    ghRequest.features = json.features;
                    if (isProduction())
                        delete json.features['bike']

                    var vehicles = Object.keys(json.features);

                    if (vehicles.length > 0)
                        ghRequest.initVehicle(vehicles[0]);

                    for (var key in json.features) {
                        vehiclesDiv.append(createButton(key.toLowerCase()));
                    }
                }

                //@Amal Elgammal: takes the returned sensor data and append the weighting dropdown list box
                var sensorsTxt = json.sensors;

                for (var i = 0; i < sensorsTxt.length; i++)
                {
                    var wsensor = sensorsTxt[i];
                    wsensor = wsensor.replace("_", " ");
                    if (wsensor.indexOf("_") >= 0)
                    {
                        wsensor = wsensor.replace("_", " ");
                    }


                    console.log("Now wsensor = " + wsensor);

                    optionValue = sensorsTxt[i].toLowerCase();
                    $('#weightingSelect').append($('<option>', {
                        value: optionValue,
                        text: wsensor
                    }));

                }
                //---End
                //
                //@Amal Elgammal: if the user opens the url with a query, the value of the weighting in the query
                //is reflected to the corresponding drop-down list box on the webpage

                if (selectedWeighting)
                {
                    $("#weightingSelect").prop('value', selectedWeighting);
                }
                //--End

                //
                //@Amal Elgammal: if the user opens the url with an elevation value, the value of the elevation in the query
                //is reflected to the corresponding checkbox on the webpage

                if (selectedElevation)
                {
                    $("#elevationCheck").prop('checked', true);
                }
                //--End

                //var msg = arg3[0];
                //console.log("value of msg returned from the webservice call = "+ msg);


                initMap();

                // execute query
                initFromParams(urlParams, true);


            }, function (err) {
                console.log(err);
                $('#error').html('GraphHopper API offline? <a href="http://graphhopper.com/maps">Refresh</a>'
                        + '<br/>Status: ' + err.statusText + '<br/>' + host);

                bounds = {
                    "minLon": -180,
                    "minLat": -90,
                    "maxLon": 180,
                    "maxLat": 90
                };
                initMap();
            });

    $(window).resize(function () {
        adjustMapSize();
    });

    setAutoCompleteList("from");
    setAutoCompleteList("to");
});



function initFromParams(params, doQuery) {
    ghRequest.init(params);
    var fromAndTo = params.from && params.to;

    var routeNow = params.point && params.point.length === 2 || fromAndTo;
    if (routeNow) {
        if (fromAndTo)
            resolveCoords(params.from, params.to, doQuery);
        else
            resolveCoords(params.point[0], params.point[1], doQuery);
    }
}

function resolveCoords(fromStr, toStr, doQuery) {
    if (fromStr !== ghRequest.from.input || !ghRequest.from.isResolved())
        ghRequest.from = new GHInput(fromStr);

    if (toStr !== ghRequest.to.input || !ghRequest.to.isResolved())
        ghRequest.to = new GHInput(toStr);

    if (ghRequest.from.lat && ghRequest.to.lat) {
        // two mouse clicks into the map -> do not wait for resolve
        resolveFrom();
        resolveTo();
        routeLatLng(ghRequest, doQuery);
    } else {
        // at least one text input from user -> wait for resolve as we need the coord for routing     
        $.when(resolveFrom(), resolveTo()).done(function (fromArgs, toArgs) {
            routeLatLng(ghRequest, doQuery);
        });
    }
}

function adjustMapSize() {
    var mapDiv = $("#map");
    var width = $(window).width() - 295;
    if (width < 400) {
        width = 290;
        mapDiv.attr("style", "position: relative; float: right;");
    } else {
        mapDiv.attr("style", "position: absolute; right: 0;");
    }
    var height = $(window).height();
    mapDiv.width(width).height(height);
    $("#input").height(height);
    $("#info").css("max-height", height - $("#input_header").height() - 35);
}

function initMap() {
    adjustMapSize();
    console.log("init map at " + JSON.stringify(bounds));
    
    //var testData= { max:313, data: [{lat: 53.34411391779312, value: 0.73, lng: -6.252017716640371, "user": null, "made_at": "2014-09-18T09:06:59Z"}, {lat: 53.34411719855868, value: 0.72, lng: -6.2520465280282, "user": null, "made_at": "2014-09-18T09:06:58Z"}, {lat: 53.3441204760398, value: 0.72, lng: -6.252075310637859, "user": null, "made_at": "2014-09-18T09:06:57Z"}, {lat: 53.3441204760398, value: 0.72, lng: -6.252075310637859, "user": null, "made_at": "2014-09-18T09:06:56Z"}, {lat: 53.3441204760398, value: 0.72, lng: -6.252075310637859, "user": null, "made_at": "2014-09-18T09:06:55Z"}, {lat: 53.3441204760398, value: 0.71, lng: -6.252075310637859, "user": null, "made_at": "2014-09-18T09:06:54Z"}, {lat: 53.3441204760398, value: 0.71, lng: -6.252075310637859, "user": null, "made_at": "2014-09-18T09:06:53Z"}, {lat: 53.3441204760398, value: 0.72, lng: -6.252075310637859, "user": null, "made_at": "2014-09-18T09:06:52Z"}, {lat: 53.3441204760398, value: 0.74, lng: -6.252075310637859, "user": null, "made_at": "2014-09-18T09:06:51Z"}, {lat: 53.3441204760398, value: 0.72, lng: -6.252075310637859, "user": null, "made_at": "2014-09-18T09:06:50Z"}, {lat: 53.3441204760398, value: 0.72, lng: -6.252075310637859, "user": null, "made_at": "2014-09-18T09:06:49Z"}, {lat: 53.3441204760398, value: 0.74, lng: -6.252075310637859, "user": null, "made_at": "2014-09-18T09:06:48Z"}, {lat: 53.3441204760398, value: 0.72, lng: -6.252075310637859, "user": null, "made_at": "2014-09-18T09:06:47Z"}, {lat: 53.3441204760398, value: 0.74, lng: -6.252075310637859, "user": null, "made_at": "2014-09-18T09:06:46Z"}, {lat: 53.3441204760398, value: 0.72, lng: -6.252075310637859, "user": null, "made_at": "2014-09-18T09:06:45Z"}, {lat: 53.3441204760398, value: 0.74, lng: -6.252075310637859, "user": null, "made_at": "2014-09-18T09:06:44Z"}, {lat: 53.3441204760398, value: 0.73, lng: -6.252075310637859, "user": null, "made_at": "2014-09-18T09:06:43Z"}, {lat: 53.3441204760398, value: 0.74, lng: -6.252075310637859, "user": null, "made_at": "2014-09-18T09:06:42Z"}, {lat: 53.3441204760398, value: 0.75, lng: -6.252075310637859, "user": null, "made_at": "2014-09-18T09:06:41Z"}, {lat: 53.3441204760398, value: 0.72, lng: -6.252075310637859, "user": null, "made_at": "2014-09-18T09:06:40Z"}, {lat: 53.3441204760398, value: 0.73, lng: -6.252075310637859, "user": null, "made_at": "2014-09-18T09:06:39Z"}, {lat: 53.3441204760398, value: 0.74, lng: -6.252075310637859, "user": null, "made_at": "2014-09-18T09:06:38Z"}, {lat: 53.34419642576421, value: 0.71, lng: -6.252647722145527, "user": null, "made_at": "2014-09-18T09:06:37Z"}, {lat: 53.34419642576421, value: 0.71, lng: -6.252647722145527, "user": null, "made_at": "2014-09-18T09:06:36Z"}, {lat: 53.34422085901382, value: 0.71, lng: -6.252704844828906, "user": null, "made_at": "2014-09-18T09:06:35Z"}, {lat: 53.34422085901382, value: 0.73, lng: -6.252704844828906, "user": null, "made_at": "2014-09-18T09:06:34Z"}, {lat: 53.34423923252741, value: 0.76, lng: -6.252764509999126, "user": null, "made_at": "2014-09-18T09:06:33Z"}, {lat: 53.34423923252741, value: 0.73, lng: -6.252764509999126, "user": null, "made_at": "2014-09-18T09:06:32Z"}, {lat: 53.34425187375049, value: 0.72, lng: -6.252813303407585, "user": null, "made_at": "2014-09-18T09:06:31Z"}, {lat: 53.34425690443489, value: 0.73, lng: -6.252834128653032, "user": null, "made_at": "2014-09-18T09:06:30Z"}, {lat: 53.34426193009, value: 0.72, lng: -6.2528549330989245, "user": null, "made_at": "2014-09-18T09:06:29Z"}, {lat: 53.344266955741475, value: 0.72, lng: -6.252875737549703, "user": null, "made_at": "2014-09-18T09:06:28Z"}, {lat: 53.34427198138934, value: 0.74, lng: -6.252896542005365, "user": null, "made_at": "2014-09-18T09:06:27Z"}, {lat: 53.34427700703356, value: 0.72, lng: -6.252917346465939, "user": null, "made_at": "2014-09-18T09:06:26Z"}, {lat: 53.344282032674144, value: 0.71, lng: -6.2529381509313975, "user": null, "made_at": "2014-09-18T09:06:25Z"}, {lat: 53.34428706333674, value: 0.72, lng: -6.252958976206234, "user": null, "made_at": "2014-09-18T09:06:24Z"}, {lat: 53.34429209399571, value: 0.73, lng: -6.252979801485955, "user": null, "made_at": "2014-09-18T09:06:23Z"}, {lat: 53.34429711459977, value: 0.71, lng: -6.253000585161629, "user": null, "made_at": "2014-09-18T09:06:22Z"}, {lat: 53.34429711459977, value: 0.71, lng: -6.253000585161629, "user": null, "made_at": "2014-09-18T09:06:21Z"}, {lat: 53.34429711459977, value: 0.75, lng: -6.253000585161629, "user": null, "made_at": "2014-09-18T09:06:20Z"}, {lat: 53.34429711459977, value: 0.74, lng: -6.253000585161629, "user": null, "made_at": "2014-09-18T09:06:19Z"}, {lat: 53.34429711459977, value: 0.71, lng: -6.253000585161629, "user": null, "made_at": "2014-09-18T09:06:18Z"}, {lat: 53.34429711459977, value: 0.73, lng: -6.253000585161629, "user": null, "made_at": "2014-09-18T09:06:17Z"}, {lat: 53.34429711459977, value: 0.73, lng: -6.253000585161629, "user": null, "made_at": "2014-09-18T09:06:16Z"}, {lat: 53.34429711459977, value: 0.73, lng: -6.253000585161629, "user": null, "made_at": "2014-09-18T09:06:15Z"}, {lat: 53.34429711459977, value: 0.72, lng: -6.253000585161629, "user": null, "made_at": "2014-09-18T09:06:14Z"}, {lat: 53.34429711459977, value: 0.71, lng: -6.253000585161629, "user": null, "made_at": "2014-09-18T09:06:13Z"}, {lat: 53.34429711459977, value: 0.73, lng: -6.253000585161629, "user": null, "made_at": "2014-09-18T09:06:12Z"}, {lat: 53.34429711459977, value: 0.72, lng: -6.253000585161629, "user": null, "made_at": "2014-09-18T09:06:11Z"}, {lat: 53.34429711459977, value: 0.73, lng: -6.253000585161629, "user": null, "made_at": "2014-09-18T09:06:10Z"}, {lat: 53.34429711459977, value: 0.71, lng: -6.253000585161629, "user": null, "made_at": "2014-09-18T09:06:09Z"}, {lat: 53.34429711459977, value: 0.72, lng: -6.253000585161629, "user": null, "made_at": "2014-09-18T09:06:08Z"}, {lat: 53.34429711459977, value: 0.71, lng: -6.253000585161629, "user": null, "made_at": "2014-09-18T09:06:07Z"}, {lat: 53.34429711459977, value: 0.73, lng: -6.253000585161629, "user": null, "made_at": "2014-09-18T09:06:06Z"}, {lat: 53.34429711459977, value: 0.71, lng: -6.253000585161629, "user": null, "made_at": "2014-09-18T09:06:05Z"}, {lat: 53.34429711459977, value: 0.72, lng: -6.253000585161629, "user": null, "made_at": "2014-09-18T09:06:04Z"}, {lat: 53.34429711459977, value: 0.73, lng: -6.253000585161629, "user": null, "made_at": "2014-09-18T09:06:03Z"}, {lat: 53.344395452181004, value: 0.73, lng: -6.253421466676843, "user": null, "made_at": "2014-09-18T09:06:02Z"}, {lat: 53.344395452181004, value: 0.72, lng: -6.253421466676843, "user": null, "made_at": "2014-09-18T09:06:01Z"}, {lat: 53.344395452181004, value: 0.74, lng: -6.253421466676843, "user": null, "made_at": "2014-09-18T09:06:00Z"}, {lat: 53.34440767397955, value: 0.78, lng: -6.253513719176685, "user": null, "made_at": "2014-09-18T09:05:59Z"}, {lat: 53.34440767397955, value: 0.74, lng: -6.253513719176685, "user": null, "made_at": "2014-09-18T09:05:58Z"}, {lat: 53.344415785237004, value: 0.75, lng: -6.253585173658179, "user": null, "made_at": "2014-09-18T09:05:57Z"}, {lat: 53.344415785237004, value: 0.76, lng: -6.253585173658179, "user": null, "made_at": "2014-09-18T09:05:56Z"}, {lat: 53.34442048937369, value: 82, lng: -6.253635577749224, "user": null, "made_at": "2014-09-18T09:05:55Z"}, {lat: 53.3444236478337, value: 0.76, lng: -6.253655239075367, "user": null, "made_at": "2014-09-18T09:05:54Z"}, {lat: 53.34442680944889, value: 0.72, lng: -6.253674920065761, "user": null, "made_at": "2014-09-18T09:05:53Z"}, {lat: 53.3444299679024, value: 0.75, lng: -6.253694581397731, "user": null, "made_at": "2014-09-18T09:05:52Z"}, {lat: 53.34443312635269, value: 0.77, lng: -6.253714242732602, "user": null, "made_at": "2014-09-18T09:05:51Z"}, {lat: 53.34443628795815, value: 0.73, lng: -6.2537339237317235, "user": null, "made_at": "2014-09-18T09:05:50Z"}, {lat: 53.34443944640195, value: 0.73, lng: -6.253753585072421, "user": null, "made_at": "2014-09-18T09:05:49Z"}, {lat: 53.34444260800094, value: 0.73, lng: -6.253773266077369, "user": null, "made_at": "2014-09-18T09:05:48Z"}, {lat: 53.34444576643825, value: 0.73, lng: -6.253792927423868, "user": null, "made_at": "2014-09-18T09:05:47Z"}, {lat: 53.344448924872296, value: 0.73, lng: -6.253812588773267, "user": null, "made_at": "2014-09-18T09:05:46Z"}, {lat: 53.34445208330311, value: 0.72, lng: -6.253832250125593, "user": null, "made_at": "2014-09-18T09:05:45Z"}, {lat: 53.34445524488912, value: 0.75, lng: -6.2538519311421705, "user": null, "made_at": "2014-09-18T09:05:44Z"}, {lat: 53.34445524488912, value: 0.73, lng: -6.2538519311421705, "user": null, "made_at": "2014-09-18T09:05:43Z"}, {lat: 53.34445524488912, value: 1, lng: -6.2538519311421705, "user": null, "made_at": "2014-09-18T09:05:42Z"}, {lat: 53.34445524488912, value: 0.71, lng: -6.2538519311421705, "user": null, "made_at": "2014-09-18T09:05:41Z"}, {lat: 53.34445524488912, value: 0.71, lng: -6.2538519311421705, "user": null, "made_at": "2014-09-18T09:05:40Z"}, {lat: 53.34445524488912, value: 0.71, lng: -6.2538519311421705, "user": null, "made_at": "2014-09-18T09:05:39Z"}, {lat: 53.34445524488912, value: 0.71, lng: -6.2538519311421705, "user": null, "made_at": "2014-09-18T09:05:38Z"}, {lat: 53.34445524488912, value: 0.71, lng: -6.2538519311421705, "user": null, "made_at": "2014-09-18T09:05:37Z"}, {lat: 53.34445524488912, value: 0.71, lng: -6.2538519311421705, "user": null, "made_at": "2014-09-18T09:05:36Z"}, {lat: 53.34445524488912, value: 0.71, lng: -6.2538519311421705, "user": null, "made_at": "2014-09-18T09:05:35Z"}, {lat: 53.34445524488912, value: 0.72, lng: -6.2538519311421705, "user": null, "made_at": "2014-09-18T09:05:34Z"}, {lat: 53.34445524488912, value: 0.71, lng: -6.2538519311421705, "user": null, "made_at": "2014-09-18T09:05:33Z"}, {lat: 53.34445524488912, value: 0.72, lng: -6.2538519311421705, "user": null, "made_at": "2014-09-18T09:05:32Z"}, {lat: 53.34445524488912, value: 0.71, lng: -6.2538519311421705, "user": null, "made_at": "2014-09-18T09:05:31Z"}, {lat: 53.34445524488912, value: 0.72, lng: -6.2538519311421705, "user": null, "made_at": "2014-09-18T09:05:30Z"}, {lat: 53.34445524488912, value: 0.72, lng: -6.2538519311421705, "user": null, "made_at": "2014-09-18T09:05:29Z"}, {lat: 53.34445524488912, value: 0.71, lng: -6.2538519311421705, "user": null, "made_at": "2014-09-18T09:05:28Z"}, {lat: 53.34445524488912, value: 0.71, lng: -6.2538519311421705, "user": null, "made_at": "2014-09-18T09:05:27Z"}, {lat: 53.34445524488912, value: 0.71, lng: -6.2538519311421705, "user": null, "made_at": "2014-09-18T09:05:26Z"}, {lat: 53.34445524488912, value: 0.71, lng: -6.2538519311421705, "user": null, "made_at": "2014-09-18T09:05:25Z"}, {lat: 53.34450481341464, value: 0.71, lng: -6.254243183640294, "user": null, "made_at": "2014-09-18T09:05:24Z"}, {lat: 53.34450481341464, value: 0.74, lng: -6.254243183640294, "user": null, "made_at": "2014-09-18T09:05:23Z"}, {lat: 53.34450481341464, value: 0.72, lng: -6.254243183640294, "user": null, "made_at": "2014-09-18T09:05:22Z"}, {lat: 53.34447888191365, value: 0.75, lng: -6.254301248173367, "user": null, "made_at": "2014-09-18T09:05:21Z"}, {lat: 53.34447888191365, value: 0.72, lng: -6.254301248173367, "user": null, "made_at": "2014-09-18T09:05:20Z"}, {lat: 53.34447888191365, value: 0.74, lng: -6.254301248173367, "user": null, "made_at": "2014-09-18T09:05:19Z"}, {lat: 53.34447604963557, value: 0.71, lng: -6.254382835900855, "user": null, "made_at": "2014-09-18T09:05:18Z"}, {lat: 53.34447604963557, value: 0.71, lng: -6.254382835900855, "user": null, "made_at": "2014-09-18T09:05:17Z"}, {lat: 53.34448604517555, value: 0.72, lng: -6.254444090555133, "user": null, "made_at": "2014-09-18T09:05:16Z"}, {lat: 53.344488877688796, value: 0.74, lng: -6.2544696136204765, "user": null, "made_at": "2014-09-18T09:05:15Z"}, {lat: 53.3444917101966, value: 0.72, lng: -6.254495136689203, "user": null, "made_at": "2014-09-18T09:05:14Z"}, {lat: 53.34449454269894, value: 0.72, lng: -6.254520659761315, "user": null, "made_at": "2014-09-18T09:05:13Z"}, {lat: 53.34449454269894, value: 0.76, lng: -6.254520659761315, "user": null, "made_at": "2014-09-18T09:05:12Z"}, {lat: 53.34449454269894, value: 0.72, lng: -6.254520659761315, "user": null, "made_at": "2014-09-18T09:05:11Z"}, {lat: 53.34449454269894, value: 0.73, lng: -6.254520659761315, "user": null, "made_at": "2014-09-18T09:05:10Z"}, {lat: 53.34449454269894, value: 0.73, lng: -6.254520659761315, "user": null, "made_at": "2014-09-18T09:05:09Z"}, {lat: 53.34449454269894, value: 0.72, lng: -6.254520659761315, "user": null, "made_at": "2014-09-18T09:05:08Z"}, {lat: 53.34449454269894, value: 0.74, lng: -6.254520659761315, "user": null, "made_at": "2014-09-18T09:05:07Z"}, {lat: 53.34449454269894, value: 0.72, lng: -6.254520659761315, "user": null, "made_at": "2014-09-18T09:05:06Z"}, {lat: 53.34449454269894, value: 0.72, lng: -6.254520659761315, "user": null, "made_at": "2014-09-18T09:05:05Z"}, {lat: 53.34449454269894, value: 0.72, lng: -6.254520659761315, "user": null, "made_at": "2014-09-18T09:05:04Z"}, {lat: 53.34449454269894, value: 0.73, lng: -6.254520659761315, "user": null, "made_at": "2014-09-18T09:05:03Z"}, {lat: 53.34449454269894, value: 0.72, lng: -6.254520659761315, "user": null, "made_at": "2014-09-18T09:05:02Z"}, {lat: 53.34449454269894, value: 0.73, lng: -6.254520659761315, "user": null, "made_at": "2014-09-18T09:05:01Z"}, {lat: 53.34449454269894, value: 0.72, lng: -6.254520659761315, "user": null, "made_at": "2014-09-18T09:05:00Z"}, {lat: 53.34449454269894, value: 0.71, lng: -6.254520659761315, "user": null, "made_at": "2014-09-18T09:04:59Z"}, {lat: 53.34449454269894, value: 0.71, lng: -6.254520659761315, "user": null, "made_at": "2014-09-18T09:04:58Z"}, {lat: 53.34449454269894, value: 0.73, lng: -6.254520659761315, "user": null, "made_at": "2014-09-18T09:04:57Z"}, {lat: 53.34449454269894, value: 0.73, lng: -6.254520659761315, "user": null, "made_at": "2014-09-18T09:04:56Z"}, {lat: 53.34449454269894, value: 0.73, lng: -6.254520659761315, "user": null, "made_at": "2014-09-18T09:04:55Z"}, {lat: 53.34449454269894, value: 70, lng: -6.254520659761315, "user": null, "made_at": "2014-09-18T09:04:54Z"}, {lat: 53.34454913624101, value: 0.72, lng: -6.255035181562706, "user": null, "made_at": "2014-09-18T09:04:53Z"}, {lat: 53.34454913624101, value: 0.71, lng: -6.255035181562706, "user": null, "made_at": "2014-09-18T09:04:52Z"}, {lat: 53.34454845171692, value: 0.72, lng: -6.255088029486576, "user": null, "made_at": "2014-09-18T09:04:51Z"}, {lat: 53.34454845171692, value: 0.72, lng: -6.255088029486576, "user": null, "made_at": "2014-09-18T09:04:50Z"}, {lat: 53.34454845171692, value: 0.73, lng: -6.255088029486576, "user": null, "made_at": "2014-09-18T09:04:49Z"}, {lat: 53.34454601816911, value: 0.73, lng: -6.255162005615356, "user": null, "made_at": "2014-09-18T09:04:48Z"}, {lat: 53.34454601816911, value: 0.72, lng: -6.255162005615356, "user": null, "made_at": "2014-09-18T09:04:47Z"}, {lat: 53.34454601816911, value: 0.73, lng: -6.255162005615356, "user": null, "made_at": "2014-09-18T09:04:46Z"}, {lat: 53.34454601816911, value: 0.75, lng: -6.255162005615356, "user": null, "made_at": "2014-09-18T09:04:45Z"}, {lat: 53.34454601816911, value: 0.72, lng: -6.255162005615356, "user": null, "made_at": "2014-09-18T09:04:44Z"}, {lat: 53.34454601816911, value: 0.72, lng: -6.255162005615356, "user": null, "made_at": "2014-09-18T09:04:43Z"}, {lat: 53.34454601816911, value: 0.71, lng: -6.255162005615356, "user": null, "made_at": "2014-09-18T09:04:42Z"}, {lat: 53.34454601816911, value: 0.75, lng: -6.255162005615356, "user": null, "made_at": "2014-09-18T09:04:41Z"}, {lat: 53.34454601816911, value: 0.75, lng: -6.255162005615356, "user": null, "made_at": "2014-09-18T09:04:40Z"}, {lat: 53.34454601816911, value: 0.71, lng: -6.255162005615356, "user": null, "made_at": "2014-09-18T09:04:39Z"}, {lat: 53.34454601816911, value: 0.71, lng: -6.255162005615356, "user": null, "made_at": "2014-09-18T09:04:38Z"}, {lat: 53.34454601816911, value: 0.71, lng: -6.255162005615356, "user": null, "made_at": "2014-09-18T09:04:37Z"}, {lat: 53.34455951315565, value: 0.71, lng: -6.2554945695440765, "user": null, "made_at": "2014-09-18T09:04:36Z"}, {lat: 53.34455951315565, value: 0.72, lng: -6.2554945695440765, "user": null, "made_at": "2014-09-18T09:04:35Z"}, {lat: 53.34458088271241, value: 0.71, lng: -6.255568186773239, "user": null, "made_at": "2014-09-18T09:04:34Z"}, {lat: 53.34458088271241, value: 0.71, lng: -6.255568186773239, "user": null, "made_at": "2014-09-18T09:04:33Z"}, {lat: 53.34459687118064, value: 0.72, lng: -6.255634655275031, "user": null, "made_at": "2014-09-18T09:04:32Z"}, {lat: 53.34459687118064, value: 0.71, lng: -6.255634655275031, "user": null, "made_at": "2014-09-18T09:04:31Z"}, {lat: 53.344614198554005, value: 0.71, lng: -6.255680024879683, "user": null, "made_at": "2014-09-18T09:04:30Z"}, {lat: 53.3446282987426, value: 0.71, lng: -6.255696937798478, "user": null, "made_at": "2014-09-18T09:04:29Z"}, {lat: 53.3446282987426, value: 0.71, lng: -6.255696937798478, "user": null, "made_at": "2014-09-18T09:04:28Z"}, {lat: 53.3446282987426, value: 0.74, lng: -6.255696937798478, "user": null, "made_at": "2014-09-18T09:04:27Z"}, {lat: 53.3446282987426, value: 0.72, lng: -6.255696937798478, "user": null, "made_at": "2014-09-18T09:04:26Z"}, {lat: 53.3446282987426, value: 0.72, lng: -6.255696937798478, "user": null, "made_at": "2014-09-18T09:04:25Z"}, {lat: 53.3446282987426, value: 0.73, lng: -6.255696937798478, "user": null, "made_at": "2014-09-18T09:04:24Z"}, {lat: 53.3446282987426, value: 0.74, lng: -6.255696937798478, "user": null, "made_at": "2014-09-18T09:04:23Z"}, {lat: 53.3446282987426, value: 0.72, lng: -6.255696937798478, "user": null, "made_at": "2014-09-18T09:04:22Z"}, {lat: 53.3446282987426, value: 0.71, lng: -6.255696937798478, "user": null, "made_at": "2014-09-18T09:04:21Z"}, {lat: 53.3446282987426, value: 0.71, lng: -6.255696937798478, "user": null, "made_at": "2014-09-18T09:04:20Z"}, {lat: 53.3446282987426, value: 0.72, lng: -6.255696937798478, "user": null, "made_at": "2014-09-18T09:04:19Z"}, {lat: 53.3446282987426, value: 0.71, lng: -6.255696937798478, "user": null, "made_at": "2014-09-18T09:04:18Z"}, {lat: 53.3446282987426, value: 0.72, lng: -6.255696937798478, "user": null, "made_at": "2014-09-18T09:04:17Z"}, {lat: 53.3446282987426, value: 0.71, lng: -6.255696937798478, "user": null, "made_at": "2014-09-18T09:04:16Z"}, {lat: 53.3446282987426, value: 0.76, lng: -6.255696937798478, "user": null, "made_at": "2014-09-18T09:04:15Z"}, {lat: 53.3446282987426, value: 0.74, lng: -6.255696937798478, "user": null, "made_at": "2014-09-18T09:04:14Z"}, {lat: 53.3446282987426, value: 0.74, lng: -6.255696937798478, "user": null, "made_at": "2014-09-18T09:04:13Z"}, {lat: 53.3446282987426, value: 0.74, lng: -6.255696937798478, "user": null, "made_at": "2014-09-18T09:04:12Z"}, {lat: 53.3446282987426, value: 0.72, lng: -6.255696937798478, "user": null, "made_at": "2014-09-18T09:04:11Z"}, {lat: 53.3446282987426, value: 0.72, lng: -6.255696937798478, "user": null, "made_at": "2014-09-18T09:04:10Z"}, {lat: 53.344909386083295, value: 0.72, lng: -6.256016968298387, "user": null, "made_at": "2014-09-18T09:04:09Z"}, {lat: 53.344909386083295, value: 0.72, lng: -6.256016968298387, "user": null, "made_at": "2014-09-18T09:04:08Z"}, {lat: 53.344909386083295, value: 0.72, lng: -6.256016968298387, "user": null, "made_at": "2014-09-18T09:04:07Z"}, {lat: 53.34495249243549, value: 0.72, lng: -6.256010547987757, "user": null, "made_at": "2014-09-18T09:04:06Z"}, {lat: 53.34495249243549, value: 0.73, lng: -6.256010547987757, "user": null, "made_at": "2014-09-18T09:04:05Z"}, {lat: 53.34495249243549, value: 0.71, lng: -6.256010547987757, "user": null, "made_at": "2014-09-18T09:04:04Z"}, {lat: 53.34495249243549, value: 0.72, lng: -6.256010547987757, "user": null, "made_at": "2014-09-18T09:04:03Z"}, {lat: 53.34495249243549, value: 0.73, lng: -6.256010547987757, "user": null, "made_at": "2014-09-18T09:04:02Z"}, {lat: 53.34495249243549, value: 0.71, lng: -6.256010547987757, "user": null, "made_at": "2014-09-18T09:04:01Z"}, {lat: 53.34495249243549, value: 0.71, lng: -6.256010547987757, "user": null, "made_at": "2014-09-18T09:04:00Z"}, {lat: 53.34495249243549, value: 0.72, lng: -6.256010547987757, "user": null, "made_at": "2014-09-18T09:03:59Z"}, {lat: 53.34495249243549, value: 0.72, lng: -6.256010547987757, "user": null, "made_at": "2014-09-18T09:03:58Z"}, {lat: 53.34495249243549, value: 0.74, lng: -6.256010547987757, "user": null, "made_at": "2014-09-18T09:03:57Z"}, {lat: 53.34495249243549, value: 0.74, lng: -6.256010547987757, "user": null, "made_at": "2014-09-18T09:03:56Z"}, {lat: 53.34495249243549, value: 0.75, lng: -6.256010547987757, "user": null, "made_at": "2014-09-18T09:03:55Z"}, {lat: 53.34495249243549, value: 0.73, lng: -6.256010547987757, "user": null, "made_at": "2014-09-18T09:03:54Z"}, {lat: 53.34495249243549, value: 0.72, lng: -6.256010547987757, "user": null, "made_at": "2014-09-18T09:03:53Z"}, {lat: 53.34495249243549, value: 0.72, lng: -6.256010547987757, "user": null, "made_at": "2014-09-18T09:03:52Z"}, {lat: 53.345210667540776, value: 0.73, lng: -6.25596850286066, "user": null, "made_at": "2014-09-18T09:03:51Z"}, {lat: 53.345210667540776, value: 0.75, lng: -6.25596850286066, "user": null, "made_at": "2014-09-18T09:03:50Z"}, {lat: 53.34524259402966, value: 0.74, lng: -6.255964309369081, "user": null, "made_at": "2014-09-18T09:03:49Z"}, {lat: 53.34524259402966, value: 0.73, lng: -6.255964309369081, "user": null, "made_at": "2014-09-18T09:03:48Z"}, {lat: 53.34524259402966, value: 0.72, lng: -6.255964309369081, "user": null, "made_at": "2014-09-18T09:03:47Z"}, {lat: 53.34524259402966, value: 0.72, lng: -6.255964309369081, "user": null, "made_at": "2014-09-18T09:03:46Z"}, {lat: 53.34524259402966, value: 0.74, lng: -6.255964309369081, "user": null, "made_at": "2014-09-18T09:03:45Z"}, {lat: 53.34529814311246, value: 0.74, lng: -6.255927868189161, "user": null, "made_at": "2014-09-18T09:03:44Z"}, {lat: 53.34529814311246, value: 0.74, lng: -6.255927868189161, "user": null, "made_at": "2014-09-18T09:03:43Z"}, {lat: 53.34529814311246, value: 0.72, lng: -6.255927868189161, "user": null, "made_at": "2014-09-18T09:03:42Z"}, {lat: 53.345340188178014, value: 0.72, lng: -6.255956861064146, "user": null, "made_at": "2014-09-18T09:03:41Z"}, {lat: 53.345340188178014, value: 0.72, lng: -6.255956861064146, "user": null, "made_at": "2014-09-18T09:03:40Z"}, {lat: 53.3453669948021, value: 0.74, lng: -6.255974354975323, "user": null, "made_at": "2014-09-18T09:03:39Z"}, {lat: 53.3453669948021, value: 0.73, lng: -6.255974354975323, "user": null, "made_at": "2014-09-18T09:03:38Z"}, {lat: 53.3453669948021, value: 0.72, lng: -6.255974354975323, "user": null, "made_at": "2014-09-18T09:03:37Z"}, {lat: 53.3453669948021, value: 0.74, lng: -6.255974354975323, "user": null, "made_at": "2014-09-18T09:03:36Z"}, {lat: 53.3453669948021, value: 0.77, lng: -6.255974354975323, "user": null, "made_at": "2014-09-18T09:03:35Z"}, {lat: 53.3453669948021, value: 0.77, lng: -6.255974354975323, "user": null, "made_at": "2014-09-18T09:03:34Z"}, {lat: 53.3453669948021, value: 0.78, lng: -6.255974354975323, "user": null, "made_at": "2014-09-18T09:03:33Z"}, {lat: 53.3453669948021, value: 0.76, lng: -6.255974354975323, "user": null, "made_at": "2014-09-18T09:03:32Z"}, {lat: 53.3453669948021, value: 0.75, lng: -6.255974354975323, "user": null, "made_at": "2014-09-18T09:03:30Z"}, {lat: 53.3453669948021, value: 0.73, lng: -6.255974354975323, "user": null, "made_at": "2014-09-18T09:03:29Z"}, {lat: 53.3453669948021, value: 0.74, lng: -6.255974354975323, "user": null, "made_at": "2014-09-18T09:03:28Z"}, {lat: 53.34544627188994, value: 0.76, lng: -6.256067148496905, "user": null, "made_at": "2014-09-18T09:03:27Z"}, {lat: 53.34544627188994, value: 0.75, lng: -6.256067148496905, "user": null, "made_at": "2014-09-18T09:03:26Z"}, {lat: 53.34544627188994, value: 0.75, lng: -6.256067148496905, "user": null, "made_at": "2014-09-18T09:03:25Z"}, {lat: 53.34547666448374, value: 0.75, lng: -6.25615166630724, "user": null, "made_at": "2014-09-18T09:03:24Z"}, {lat: 53.34547666448374, value: 0.75, lng: -6.25615166630724, "user": null, "made_at": "2014-09-18T09:03:23Z"}, {lat: 53.34550406856316, value: 0.75, lng: -6.256202676047724, "user": null, "made_at": "2014-09-18T09:03:22Z"}, {lat: 53.34550406856316, value: 0.75, lng: -6.256202676047724, "user": null, "made_at": "2014-09-18T09:03:21Z"}, {lat: 53.34550406856316, value: 0.75, lng: -6.256202676047724, "user": null, "made_at": "2014-09-18T09:03:20Z"}, {lat: 53.345538892751854, value: 0.75, lng: -6.256260780856211, "user": null, "made_at": "2014-09-18T09:03:19Z"}, {lat: 53.345538892751854, value: 1, lng: -6.256260780856211, "user": null, "made_at": "2014-09-18T09:03:18Z"}, {lat: 53.345538892751854, value: 0.75, lng: -6.256260780856211, "user": null, "made_at": "2014-09-18T09:03:17Z"}, {lat: 53.345564435139735, value: 0.76, lng: -6.256311917464366, "user": null, "made_at": "2014-09-18T09:03:16Z"}, {lat: 53.34556774816711, value: 1, lng: -6.256328827732921, "user": null, "made_at": "2014-09-18T09:03:15Z"}, {lat: 53.3455710611921, value: 82, lng: -6.256345738004095, "user": null, "made_at": "2014-09-18T09:03:14Z"}, {lat: 53.34557437090501, value: 0.78, lng: -6.256362631384539, "user": null, "made_at": "2014-09-18T09:03:13Z"}, {lat: 53.345577680615506, value: 0.79, lng: -6.256379524767555, "user": null, "made_at": "2014-09-18T09:03:12Z"}, {lat: 53.345580990323604, value: 0.79, lng: -6.256396418153214, "user": null, "made_at": "2014-09-18T09:03:11Z"}, {lat: 53.345584303339024, value: 0.77, lng: -6.256413328434872, "user": null, "made_at": "2014-09-18T09:03:10Z"}, {lat: 53.34558761635203, value: 0.77, lng: -6.25643023871915, "user": null, "made_at": "2014-09-18T09:03:09Z"}, {lat: 53.34559092274327, value: 0.77, lng: -6.256447115219272, "user": null, "made_at": "2014-09-18T09:03:08Z"}, {lat: 53.34559423575149, value: 0.78, lng: -6.256464025508792, "user": null, "made_at": "2014-09-18T09:03:07Z"}, {lat: 53.34559754544763, value: 0.76, lng: -6.256480918907531, "user": null, "made_at": "2014-09-18T09:03:06Z"}, {lat: 53.345600855141356, value: 0.76, lng: -6.25649781230889, "user": null, "made_at": "2014-09-18T09:03:05Z"}, {lat: 53.345604164832714, value: 0.78, lng: -6.256514705712872, "user": null, "made_at": "2014-09-18T09:03:04Z"}, {lat: 53.345607477831344, value: 0.78, lng: -6.256531616012874, "user": null, "made_at": "2014-09-18T09:03:03Z"}, {lat: 53.34561079082761, value: 0.77, lng: -6.256548526315498, "user": null, "made_at": "2014-09-18T09:03:02Z"}, {lat: 53.34561079082761, value: 0.76, lng: -6.256548526315498, "user": null, "made_at": "2014-09-18T09:03:01Z"}, {lat: 53.34561079082761, value: 0.76, lng: -6.256548526315498, "user": null, "made_at": "2014-09-18T09:03:00Z"}, {lat: 53.34561079082761, value: 0.76, lng: -6.256548526315498, "user": null, "made_at": "2014-09-18T09:02:59Z"}, {lat: 53.34561079082761, value: 0.76, lng: -6.256548526315498, "user": null, "made_at": "2014-09-18T09:02:58Z"}, {lat: 53.34561079082761, value: 0.76, lng: -6.256548526315498, "user": null, "made_at": "2014-09-18T09:02:57Z"}, {lat: 53.34561079082761, value: 0.77, lng: -6.256548526315498, "user": null, "made_at": "2014-09-18T09:02:56Z"}, {lat: 53.34561079082761, value: 0.76, lng: -6.256548526315498, "user": null, "made_at": "2014-09-18T09:02:55Z"}, {lat: 53.34561079082761, value: 0.76, lng: -6.256548526315498, "user": null, "made_at": "2014-09-18T09:02:54Z"}, {lat: 53.34561079082761, value: 0.75, lng: -6.256548526315498, "user": null, "made_at": "2014-09-18T09:02:53Z"}, {lat: 53.34561079082761, value: 0.75, lng: -6.256548526315498, "user": null, "made_at": "2014-09-18T09:02:52Z"}, {lat: 53.34561079082761, value: 0.75, lng: -6.256548526315498, "user": null, "made_at": "2014-09-18T09:02:51Z"}, {lat: 53.34561079082761, value: 0.77, lng: -6.256548526315498, "user": null, "made_at": "2014-09-18T09:02:50Z"}, {lat: 53.34561079082761, value: 0.76, lng: -6.256548526315498, "user": null, "made_at": "2014-09-18T09:02:49Z"}, {lat: 53.34561079082761, value: 0.75, lng: -6.256548526315498, "user": null, "made_at": "2014-09-18T09:02:48Z"}, {lat: 53.34561079082761, value: 0.76, lng: -6.256548526315498, "user": null, "made_at": "2014-09-18T09:02:47Z"}, {lat: 53.34561079082761, value: 0.76, lng: -6.256548526315498, "user": null, "made_at": "2014-09-18T09:02:46Z"}, {lat: 53.34561079082761, value: 0.76, lng: -6.256548526315498, "user": null, "made_at": "2014-09-18T09:02:45Z"}, {lat: 53.34561079082761, value: 0.76, lng: -6.256548526315498, "user": null, "made_at": "2014-09-18T09:02:44Z"}, {lat: 53.34561079082761, value: 0.77, lng: -6.256548526315498, "user": null, "made_at": "2014-09-18T09:02:43Z"}, {lat: 53.34568684948639, value: 0.78, lng: -6.256878065853984, "user": null, "made_at": "2014-09-18T09:02:42Z"}, {lat: 53.34568684948639, value: 0.75, lng: -6.256878065853984, "user": null, "made_at": "2014-09-18T09:02:41Z"}, {lat: 53.34568684948639, value: 0.75, lng: -6.256878065853984, "user": null, "made_at": "2014-09-18T09:02:40Z"}, {lat: 53.34573306016084, value: 0.75, lng: -6.256910616725367, "user": null, "made_at": "2014-09-18T09:02:39Z"}, {lat: 53.34573306016084, value: 0.74, lng: -6.256910616725367, "user": null, "made_at": "2014-09-18T09:02:38Z"}, {lat: 53.34577327129344, value: 0.75, lng: -6.256946632638205, "user": null, "made_at": "2014-09-18T09:02:37Z"}, {lat: 53.34577327129344, value: 0.75, lng: -6.256946632638205, "user": null, "made_at": "2014-09-18T09:02:36Z"}, {lat: 53.345805922920555, value: 0.75, lng: -6.256971835844452, "user": null, "made_at": "2014-09-18T09:02:35Z"}, {lat: 53.345805922920555, value: 0.75, lng: -6.256971835844452, "user": null, "made_at": "2014-09-18T09:02:34Z"}, {lat: 53.345805922920555, value: 0.76, lng: -6.256971835844452, "user": null, "made_at": "2014-09-18T09:02:33Z"}, {lat: 53.345841619530226, value: 1, lng: -6.256988275295623, "user": null, "made_at": "2014-09-18T09:02:32Z"}, {lat: 53.345841619530226, value: 0.74, lng: -6.256988275295623, "user": null, "made_at": "2014-09-18T09:02:31Z"}, {lat: 53.345841619530226, value: 0.74, lng: -6.256988275295623, "user": null, "made_at": "2014-09-18T09:02:30Z"}, {lat: 53.345841619530226, value: 0.75, lng: -6.256988275295623, "user": null, "made_at": "2014-09-18T09:02:29Z"}, {lat: 53.345841619530226, value: 0.75, lng: -6.256988275295623, "user": null, "made_at": "2014-09-18T09:02:28Z"}, {lat: 53.34589579890622, value: 0.76, lng: -6.257040774116953, "user": null, "made_at": "2014-09-18T09:02:27Z"}, {lat: 53.34589579890622, value: 0.76, lng: -6.257040774116953, "user": null, "made_at": "2014-09-18T09:02:26Z"}, {lat: 53.34591570667101, value: 0.79, lng: -6.257089083196499, "user": null, "made_at": "2014-09-18T09:02:25Z"}, {lat: 53.34592532095169, value: 0.79, lng: -6.257111984551611, "user": null, "made_at": "2014-09-18T09:02:24Z"}, {lat: 53.34593492561372, value: 1, lng: -6.25713486301566, "user": null, "made_at": "2014-09-18T09:02:23Z"}, {lat: 53.34594453988558, value: 1, lng: -6.257157764391355, "user": null, "made_at": "2014-09-18T09:02:22Z"}, {lat: 53.34594453988558, value: 0.79, lng: -6.257157764391355, "user": null, "made_at": "2014-09-18T09:02:21Z"}, {lat: 53.34594453988558, value: 0.79, lng: -6.257157764391355, "user": null, "made_at": "2014-09-18T09:02:20Z"}, {lat: 53.34594453988558, value: 0.79, lng: -6.257157764391355, "user": null, "made_at": "2014-09-18T09:02:19Z"}, {lat: 53.34594453988558, value: 0.79, lng: -6.257157764391355, "user": null, "made_at": "2014-09-18T09:02:18Z"}, {lat: 53.34594453988558, value: 0.78, lng: -6.257157764391355, "user": null, "made_at": "2014-09-18T09:02:17Z"}, {lat: 53.34594453988558, value: 0.79, lng: -6.257157764391355, "user": null, "made_at": "2014-09-18T09:02:16Z"}, {lat: 53.34594453988558, value: 0.79, lng: -6.257157764391355, "user": null, "made_at": "2014-09-18T09:02:15Z"}, {lat: 53.34594453988558, value: 0.78, lng: -6.257157764391355, "user": null, "made_at": "2014-09-18T09:02:14Z"}, {lat: 53.34594453988558, value: 0.78, lng: -6.257157764391355, "user": null, "made_at": "2014-09-18T09:02:13Z"}, {lat: 53.34594453988558, value: 1, lng: -6.257157764391355, "user": null, "made_at": "2014-09-18T09:02:12Z"}, {lat: 53.34594453988558, value: 82, lng: -6.257157764391355, "user": null, "made_at": "2014-09-18T09:02:11Z"}, {lat: 53.34594453988558, value: 1, lng: -6.257157764391355, "user": null, "made_at": "2014-09-18T09:02:10Z"}, {lat: 53.34594453988558, value: 0.78, lng: -6.257157764391355, "user": null, "made_at": "2014-09-18T09:02:09Z"}, {lat: 53.34594453988558, value: 0.78, lng: -6.257157764391355, "user": null, "made_at": "2014-09-18T09:02:08Z"}, {lat: 53.34594453988558, value: 0.77, lng: -6.257157764391355, "user": null, "made_at": "2014-09-18T09:02:07Z"}, {lat: 53.34594453988558, value: 0.76, lng: -6.257157764391355, "user": null, "made_at": "2014-09-18T09:02:06Z"}, {lat: 53.34594453988558, value: 0.76, lng: -6.257157764391355, "user": null, "made_at": "2014-09-18T09:02:05Z"}, {lat: 53.34594453988558, value: 0.76, lng: -6.257157764391355, "user": null, "made_at": "2014-09-18T09:02:04Z"}, {lat: 53.34594453988558, value: 0.76, lng: -6.257157764391355, "user": null, "made_at": "2014-09-18T09:02:03Z"}, {lat: 53.34613722337892, value: 0.76, lng: -6.257608153490897, "user": null, "made_at": "2014-09-18T09:02:02Z"}, {lat: 53.34614719752117, value: 0.76, lng: -6.257623330466408, "user": null, "made_at": "2014-09-18T09:02:01Z"}, {lat: 53.34615718163561, value: 0.76, lng: -6.257638522626014, "user": null, "made_at": "2014-09-18T09:02:00Z"}, {lat: 53.34616715577395, value: 0.76, lng: -6.257653699615697, "user": null, "made_at": "2014-09-18T09:01:59Z"}, {lat: 53.34617712991038, value: 0.77, lng: -6.257668876612479, "user": null, "made_at": "2014-09-18T09:01:58Z"}, {lat: 53.34618709407071, value: 0.76, lng: -6.257684038439339, "user": null, "made_at": "2014-09-18T09:01:57Z"}, {lat: 53.34619707817736, value: 0.76, lng: -6.257699230627289, "user": null, "made_at": "2014-09-18T09:01:56Z"}, {lat: 53.34620706228206, value: 0.76, lng: -6.25771442282234, "user": null, "made_at": "2014-09-18T09:01:55Z"}, {lat: 53.34621703641068, value: 0.76, lng: -6.257729599847468, "user": null, "made_at": "2014-09-18T09:01:54Z"}, {lat: 53.346227040459716, value: 0.77, lng: -6.257744822410785, "user": null, "made_at": "2014-09-18T09:01:53Z"}, {lat: 53.346227040459716, value: 0.78, lng: -6.257744822410785, "user": null, "made_at": "2014-09-18T09:01:52Z"}, {lat: 53.346227040459716, value: 0.77, lng: -6.257744822410785, "user": null, "made_at": "2014-09-18T09:01:51Z"}, {lat: 53.346227040459716, value: 0.78, lng: -6.257744822410785, "user": null, "made_at": "2014-09-18T09:01:50Z"}, {lat: 53.346227040459716, value: 0.79, lng: -6.257744822410785, "user": null, "made_at": "2014-09-18T09:01:49Z"}, {lat: 53.346227040459716, value: 0.78, lng: -6.257744822410785, "user": null, "made_at": "2014-09-18T09:01:48Z"}, {lat: 53.346227040459716, value: 0.78, lng: -6.257744822410785, "user": null, "made_at": "2014-09-18T09:01:47Z"}, {lat: 53.346227040459716, value: 0.79, lng: -6.257744822410785, "user": null, "made_at": "2014-09-18T09:01:46Z"}]};
    //
    // mapquest provider
    var osmAttr = '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors';
    var tp = "ls";
    if (L.Browser.retina)
        tp = "lr";

    var lyrk = L.tileLayer('http://{s}.tiles.lyrk.org/' + tp + '/{z}/{x}/{y}?apikey=6e8cfef737a140e2a58c8122aaa26077', {
        attribution: osmAttr + ', <a href="http://geodienste.lyrk.de/">Lyrk</a>',
        subdomains: ['a', 'b', 'c']
    });

    var mapquest = L.tileLayer('http://{s}.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png', {
        attribution: osmAttr + ', <a href="http://open.mapquest.co.uk">MapQuest</a>',
        subdomains: ['otile1', 'otile2', 'otile3', 'otile4']
    });

    var mapquestAerial = L.tileLayer('http://{s}.mqcdn.com/tiles/1.0.0/sat/{z}/{x}/{y}.png', {
        attribution: osmAttr + ', <a href="http://open.mapquest.co.uk">MapQuest</a>',
        subdomains: ['otile1', 'otile2', 'otile3', 'otile4']
    });

    var thunderTransport = L.tileLayer('http://{s}.tile.thunderforest.com/transport/{z}/{x}/{y}.png', {
        attribution: osmAttr + ', <a href="http://www.thunderforest.com/transport/">Thunderforest Transport</a>',
        subdomains: ['a', 'b', 'c']
    });

    var thunderCycle = L.tileLayer('http://{s}.tile.thunderforest.com/cycle/{z}/{x}/{y}.png', {
        attribution: osmAttr + ', <a href="http://www.thunderforest.com/opencyclemap/">Thunderforest Cycle</a>',
        subdomains: ['a', 'b', 'c']
    });

    var thunderOutdoors = L.tileLayer('http://{s}.tile.thunderforest.com/outdoors/{z}/{x}/{y}.png', {
        attribution: osmAttr + ', <a href="http://www.thunderforest.com/outdoors/">Thunderforest Outdoors</a>',
        subdomains: ['a', 'b', 'c']
    });

    var wrk = L.tileLayer('http://{s}.wanderreitkarte.de/topo/{z}/{x}/{y}.png', {
        attribution: osmAttr + ', <a href="http://wanderreitkarte.de">WanderReitKarte</a>',
        subdomains: ['topo4', 'topo', 'topo2', 'topo3']
    });

    var osm = L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: osmAttr
    });

    var osmde = L.tileLayer('http://{s}.tile.openstreetmap.de/tiles/osmde/{z}/{x}/{y}.png', {
        attribution: osmAttr
    });


    //@Amal Elgammal: try with leaflet-heat
   
    map = L.map('map', {
        layers: [lyrk],
        contextmenu: true,
        contextmenuWidth: 140,
        contextmenuItems: [{
                text: 'Set as start',
                callback: setStartCoord
            }, {
                text: 'Set as end',
                callback: setEndCoord
            }, {
                separator: true,
                index: 1
            }, {
                text: 'Show coordinates',
                callback: function (e) {
                    alert(e.latlng.lat + "," + e.latlng.lng);
                }
            }, {
                text: 'Center map here',
                callback: function (e) {
                    map.panTo(e.latlng);
                }
            }]
    });
    
    var heat = L.heatLayer(testData,{
            radius: 20,
            blur: 15, 
            maxZoom: 17
        }).addTo(map);
        
        console.log("Value of length of heat = " + heat.toString());
   
    
   /* var baseMaps = {
        "Lyrk": [lyrk],
        "MapQuest": mapquest,
        "MapQuest Aerial": mapquestAerial,
        "TF Transport": thunderTransport,
        "TF Cycle": thunderCycle,
        "TF Outdoors": thunderOutdoors,
        "WanderReitKarte": wrk,
        "OpenStreetMap": osm,
        "OpenStreetMap.de": osmde
    };*/
    

   // L.control.layers(baseMaps/*, overlays*/).addTo(map);
    L.control.scale().addTo(map);

    map.fitBounds(new L.LatLngBounds(new L.LatLng(bounds.minLat, bounds.minLon),
            new L.LatLng(bounds.maxLat, bounds.maxLon)));
    if (isProduction())
        map.setView(new L.LatLng(0, 0), 2);

    map.attributionControl.setPrefix('');

    var myStyle = {
        "color": 'black',
        "weight": 2,
        "opacity": 0.3
    };
    var geoJson = {
        "type": "Feature",
        "geometry": {
            "type": "LineString",
            "coordinates": [
                [bounds.minLon, bounds.minLat],
                [bounds.maxLon, bounds.minLat],
                [bounds.maxLon, bounds.maxLat],
                [bounds.minLon, bounds.maxLat],
                [bounds.minLon, bounds.minLat]]
        }
    };

    if (bounds.initialized)
        L.geoJson(geoJson, {
            "style": myStyle
        }).addTo(map);

    routingLayer = L.geoJson().addTo(map);
  
    routingLayer.options = {style: {color: "#00cc33", "weight": 5, "opacity": 0.6}};
}

function setStartCoord(e) {
    ghRequest.from.setCoord(e.latlng.lat, e.latlng.lng);
    resolveFrom();
    routeIfAllResolved();
}

function setEndCoord(e) {
    ghRequest.to.setCoord(e.latlng.lat, e.latlng.lng);
    resolveTo();
    routeIfAllResolved();
}

function routeIfAllResolved() {
    if (ghRequest.from.isResolved() && ghRequest.to.isResolved()) {
        routeLatLng(ghRequest);
        return true;
    }
    return false;
}

function makeValidLng(lon) {
    if (lon < 180 && lon > -180)
        return lon;
    if (lon > 180)
        return (lon + 180) % 360 - 180;
    return (lon - 180) % 360 + 180;
}

function setFlag(coord, isFrom) {
    if (coord.lat) {
        var marker = L.marker([coord.lat, coord.lng], {
            icon: (isFrom ? iconFrom : iconTo),
            draggable: true
        }).addTo(routingLayer).bindPopup(isFrom ? "Start" : "End");
        marker.on('dragend', function (e) {
            routingLayer.clearLayers();
            // inconsistent leaflet API: event.target.getLatLng vs. mouseEvent.latlng?
            var latlng = e.target.getLatLng();
            hideAutoComplete();
            if (isFrom) {
                ghRequest.from.setCoord(latlng.lat, latlng.lng);
                resolveFrom();
            } else {
                ghRequest.to.setCoord(latlng.lat, latlng.lng);
                resolveTo();
            }
            // do not wait for resolving and avoid zooming when dragging
            ghRequest.do_zoom = false;
            routeLatLng(ghRequest, false);
        });
    }
}

function resolveFrom() {
    setFlag(ghRequest.from, true);
    return resolve("from", ghRequest.from);
}

function resolveTo() {
    setFlag(ghRequest.to, false);
    return resolve("to", ghRequest.to);
}

function resolve(fromOrTo, locCoord) {
    $("#" + fromOrTo + "Flag").hide();
    $("#" + fromOrTo + "Indicator").show();
    $("#" + fromOrTo + "Input").val(locCoord.input);

    return createAmbiguityList(locCoord).done(function (arg1) {
        var errorDiv = $("#" + fromOrTo + "ResolveError");
        errorDiv.empty();

        if (locCoord.error) {
            errorDiv.show();
            errorDiv.text(locCoord.error).fadeOut(5000);
            locCoord.error = '';
        }

        $("#" + fromOrTo + "Indicator").hide();
        $("#" + fromOrTo + "Flag").show();
        return locCoord;
    });
}

/**
 * Returns a defer object containing the location pointing to a resolvedList with all the found
 * coordinates.
 */
function createAmbiguityList(locCoord) {
    // make example working even if nominatim service is down
    if (locCoord.input.toLowerCase() === "madrid") {
        locCoord.lat = 40.416698;
        locCoord.lng = -3.703551;
        locCoord.locationDetails = formatLocationEntry({city: "Madrid", country: "Spain"});
        locCoord.resolvedList = [locCoord];
    }
    if (locCoord.input.toLowerCase() === "moscow") {
        locCoord.lat = 55.751608;
        locCoord.lng = 37.618775;
        locCoord.locationDetails = formatLocationEntry({road: "Borowizki-Straße", city: "Moscow", country: "Russian Federation"});
        locCoord.resolvedList = [locCoord];
    }

    if (locCoord.isResolved()) {
        var tmpDefer = $.Deferred();
        tmpDefer.resolve([locCoord]);
        return tmpDefer;
    }

    locCoord.error = "";
    locCoord.resolvedList = [];
    var timeout = 3000;
    if (locCoord.lat && locCoord.lng) {
        var url = nominatim_reverse + "?lat=" + locCoord.lat + "&lon="
                + locCoord.lng + "&format=json&zoom=16";
        return $.ajax({
            url: url,
            type: "GET",
            dataType: "json",
            timeout: timeout
        }).fail(function (err) {
            // not critical => no alert
            locCoord.error = "Error while looking up coordinate";
            console.log(err);
        }).pipe(function (json) {
            if (!json) {
                locCoord.error = "No description found for coordinate";
                return [locCoord];
            }
            var address = json.address;
            var point = {};
            point.lat = locCoord.lat;
            point.lng = locCoord.lng;
            point.bbox = json.boundingbox;
            point.positionType = json.type;
            point.locationDetails = formatLocationEntry(address);
            // point.address = json.address;
            locCoord.resolvedList.push(point);
            return [locCoord];
        });
    } else {
        return doGeoCoding(locCoord.input, 10, timeout).pipe(function (jsonArgs) {
            console.log("value returned to doGeoCoding = " + jsonArgs[0]);
            if (!jsonArgs || jsonArgs.length == 0) {
                locCoord.error = "No area description found";
                return [locCoord];
            }
            var prevImportance = jsonArgs[0].importance;
            var address;
            for (var index in jsonArgs) {
                var json = jsonArgs[index];
                // if we have already some results ignore unimportant
                if (prevImportance - json.importance > 0.4)
                    break;

                // de-duplicate via ignoring boundary stuff => not perfect as 'Freiberg' would no longer be correct
                // if (json.type === "administrative")
                //    continue;

                // if no different properties => skip!
                if (address && JSON.stringify(address) === JSON.stringify(json.address))
                    continue;

                address = json.address;
                prevImportance = json.importance;
                var point = {};
                point.lat = round(json.lat);
                point.lng = round(json.lon);
                point.locationDetails = formatLocationEntry(address);
                point.bbox = json.boundingbox;
                point.positionType = json.type;
                locCoord.resolvedList.push(point);
            }
            if (locCoord.resolvedList.length === 0) {
                locCoord.error = "No area description found";
                return [locCoord];
            }
            var list = locCoord.resolvedList;
            locCoord.lat = list[0].lat;
            locCoord.lng = list[0].lng;
            // locCoord.input = dataToText(list[0]);
            return [locCoord];
        });
    }
}

function insComma(textA, textB) {
    if (textA.length > 0)
        return textA + ", " + textB;
    return textB;
}

function formatLocationEntry(address) {
    var locationDetails = {};
    var text = "";
    if (address.road) {
        text = address.road;
        if (address.house_number) {
            if (text.length > 0)
                text += " ";
            text += address.house_number;
        }
        locationDetails.road = text;
    }

    locationDetails.postcode = address.postcode;
    locationDetails.country = address.country;

    if (address.city || address.suburb || address.town
            || address.village || address.hamlet || address.locality) {
        text = "";
        if (address.locality)
            text = insComma(text, address.locality);
        if (address.hamlet)
            text = insComma(text, address.hamlet);
        if (address.village)
            text = insComma(text, address.village);
        if (address.suburb)
            text = insComma(text, address.suburb);
        if (address.city)
            text = insComma(text, address.city);
        if (address.town)
            text = insComma(text, address.town);
        locationDetails.city = text;
    }

    text = "";
    if (address.state)
        text += address.state;

    if (address.continent)
        text = insComma(text, address.continent);

    locationDetails.more = text;
    return locationDetails;
}

function doGeoCoding(input, limit, timeout) {
    // see https://trac.openstreetmap.org/ticket/4683 why limit=3 and not 1
    if (!limit)
        limit = 10;
    var url = nominatim + "?format=json&addressdetails=1&q=" + encodeURIComponent(input) + "&limit=" + limit;

    console.log("Value of nominatim url = " + url);
    if (bounds.initialized) {
        // minLon, minLat, maxLon, maxLat => left, top, right, bottom
        url += "&bounded=1&viewbox=" + bounds.minLon + "," + bounds.maxLat + "," + bounds.maxLon + "," + bounds.minLat;
    }

    return $.ajax({
        url: url,
        type: "GET",
        dataType: "json",
        timeout: timeout
    }).fail(createCallback("[nominatim] Problem while looking up location " + input));
}

function createCallback(errorFallback) {
    return function (err) {
        console.log(errorFallback + " " + JSON.stringify(err));
    };
}

function focusWithBounds(coord, bbox, isFrom) {
    routingLayer.clearLayers();
    // bbox needs to be in the none-geojson format!?
    // [[lat, lng], [lat2, lng2], ...]
    map.fitBounds(new L.LatLngBounds(bbox));
    setFlag(coord, isFrom);
}

function focus(coord, zoom, isFrom) {
    if (coord.lat && coord.lng) {
        if (!zoom)
            zoom = 11;
        routingLayer.clearLayers();
        map.setView(new L.LatLng(coord.lat, coord.lng), zoom);
        setFlag(coord, isFrom);
    }
}
function routeLatLng(request, doQuery) {
    // do_zoom should not show up in the URL but in the request object to avoid zooming for history change
    var doZoom = request.do_zoom;
    request.do_zoom = true;
    setWeighting(request);
    setElevation(request);
    setVechile(request);


    var urlForHistory = request.createFullURL();
    // not enabled e.g. if no cookies allowed (?)
    // if disabled we have to do the query and cannot rely on the statechange history event    
    if (!doQuery && History.enabled) {
        // 2. important workaround for encoding problems in history.js

        //console.log("urlForHistory = " + urlForHistory);
        var params = parseUrl(urlForHistory);

        console.log(params);
        params.do_zoom = doZoom;
        // force a new request even if we have the same parameters
        params.mathRandom = Math.random();
        History.pushState(params, browserTitle, urlForHistory);
        return;
    }

    $("#info").empty();
    $("#info").show();
    var descriptionDiv = $("<div/>");
    $("#info").append(descriptionDiv);

    var from = request.from.toString();
    var to = request.to.toString();
    if (!from || !to) {
        descriptionDiv.html('<small>' + tr('locationsNotFound') + '</small>');
        return;
    }

    if (elevationControl)
        elevationControl.clear();

    routingLayer.clearLayers();
    setFlag(request.from, true);
    setFlag(request.to, false);

    $("#vehicles button").removeClass("selectvehicle");
    $("button#" + request.vehicle.toLowerCase()).addClass("selectvehicle");

//Check the actual sent request
    var urlForAPI = request.createURL("point=" + from + "&point=" + to);
    descriptionDiv.html('<img src="img/indicator.gif"/> Search Route ...');
    request.doRequest(urlForAPI, function (json) {

        console.log("Sent URL to the servlet: " + urlForAPI);
        descriptionDiv.html("");
        if (json.info.errors) {
            var tmpErrors = json.info.errors;
            console.log(tmpErrors);
            for (var m = 0; m < tmpErrors.length; m++) {
                descriptionDiv.append("<div class='error'>" + tmpErrors[m].message + "</div>");
            }
            return;
        }
        var path = json.paths[0];
        //Result??
        /*for (var ar in path)
         {
         console.log("Resulted path,Key = " + ar + ", value = "+ path[ar]);
         }*/


        var geojsonFeature = {
            "type": "Feature",
            // "style": myStyle,                
            "geometry": path.points
        };

        if (request.hasElevation()) {
            if (elevationControl === null) {
                elevationControl = L.control.elevation({
                    position: "bottomright",
                    theme: "white-theme", //default: lime-theme
                    width: 450,
                    height: 125,
                    yAxisMin: 0, // set min domain y axis
                    // yAxisMax: 550, // set max domain y axis
                    forceAxisBounds: false,
                    margins: {
                        top: 10,
                        right: 20,
                        bottom: 30,
                        left: 50
                    },
                    useHeightIndicator: true, //if false a marker is drawn at map position
                    interpolation: "linear", //see https://github.com/mbostock/d3/wiki/SVG-Shapes#wiki-area_interpolate
                    hoverNumber: {
                        decimalsX: 3, //decimals on distance (always in km)
                        decimalsY: 0, //deciamls on height (always in m)
                        formatter: undefined //custom formatter function may be injected
                    },
                    xTicks: undefined, //number of ticks in x axis, calculated by default according to width
                    yTicks: undefined, //number of ticks on y axis, calculated by default according to height
                    collapsed: false    //collapsed mode, show chart on click or mouseover
                });
                elevationControl.addTo(map);
            }

            elevationControl.addData(geojsonFeature);
        }

        routingLayer.addData(geojsonFeature);
        if (path.bbox && doZoom) {
            var minLon = path.bbox[0];
            var minLat = path.bbox[1];
            var maxLon = path.bbox[2];
            var maxLat = path.bbox[3];
            var tmpB = new L.LatLngBounds(new L.LatLng(minLat, minLon), new L.LatLng(maxLat, maxLon));
            map.fitBounds(tmpB);
        }

        var tmpTime = createTimeString(path.time);
        var tmpDist = createDistanceString(path.distance);
        descriptionDiv.append(tr("routeInfo", [tmpDist, tmpTime]));

        $('.defaulting').each(function (index, element) {
            $(element).css("color", "black");
        });

        if (path.instructions) {
            var instructionsElement = $("<table id='instructions'>");

            var partialInstr = path.instructions.length > 100;
            var len = Math.min(path.instructions.length, 100);
            for (var m = 0; m < len; m++) {
                var instr = path.instructions[m];
                var lngLat = path.points.coordinates[instr.interval[0]];
                addInstruction(instructionsElement, instr, m, lngLat);
            }
            $("#info").append(instructionsElement);

            if (partialInstr) {
                var moreDiv = $("<button id='moreButton'>" + tr("moreButton") + "..</button>");
                moreDiv.click(function () {
                    moreDiv.remove();
                    for (var m = len; m < path.instructions.length; m++) {
                        var instr = path.instructions[m];
                        var lngLat = path.points.coordinates[instr.interval[0]];
                        addInstruction(instructionsElement, instr, m, lngLat);
                    }
                });
                instructionsElement.append(moreDiv);
            }

            var hiddenDiv = $("<div id='routeDetails'/>");
            hiddenDiv.hide();

            var toggly = $("<button id='expandDetails'>+</button>");
            toggly.click(function () {
                hiddenDiv.toggle();
            });
            $("#info").append(toggly);
            var infoStr = "took: " + round(json.info.took / 1000, 1000) + "s"
                    + ", points: " + path.points.coordinates.length;

            hiddenDiv.append("<span>" + infoStr + "</span>");

            var exportLink = $("#exportLink a");
            exportLink.attr('href', urlForHistory);
            var startOsmLink = $("<a>start</a>");
            startOsmLink.attr("href", "http://www.openstreetmap.org/?zoom=14&mlat=" + request.from.lat + "&mlon=" + request.from.lng);
            var endOsmLink = $("<a>end</a>");
            endOsmLink.attr("href", "http://www.openstreetmap.org/?zoom=14&mlat=" + request.to.lat + "&mlon=" + request.to.lng);
            hiddenDiv.append("<br/><span>View on OSM: </span>").append(startOsmLink).append(endOsmLink);

            var osrmLink = $("<a>OSRM</a>");
            osrmLink.attr("href", "http://map.project-osrm.org/?loc=" + from + "&loc=" + to);
            hiddenDiv.append("<br/><span>Compare with: </span>");
            hiddenDiv.append(osrmLink);
            var googleLink = $("<a>Google</a> ");
            var addToGoogle = "";
            var addToBing = "";
            if (request.vehicle.toUpperCase() === "FOOT") {
                addToGoogle = "&dirflg=w";
                addToBing = "&mode=W";
            } else if ((request.vehicle.toUpperCase().indexOf("BIKE") >= 0) ||
                    (request.vehicle.toUpperCase() === "MTB")) {
                addToGoogle = "&dirflg=b";
                // ? addToBing = "&mode=B";
            }
            googleLink.attr("href", "http://maps.google.com/?q=saddr=" + from + "&daddr=" + to + addToGoogle);
            hiddenDiv.append(googleLink);
            var bingLink = $("<a>Bing</a> ");
            bingLink.attr("href", "http://www.bing.com/maps/default.aspx?rtp=adr." + from + "~adr." + to + addToBing);
            hiddenDiv.append(bingLink);
            $("#info").append(hiddenDiv);
        }
    });
}

function createDistanceString(dist) {
    if (dist < 900)
        return round(dist, 1) + tr2("mAbbr");

    dist = round(dist / 1000, 100);
    if (dist > 100)
        dist = round(dist, 1);
    return dist + tr2("kmAbbr");
}

function createTimeString(time) {
    var tmpTime = round(time / 60 / 1000, 1000);
    var resTimeStr;
    if (tmpTime > 60) {
        if (tmpTime / 60 > 24) {
            resTimeStr = floor(tmpTime / 60 / 24, 1) + tr2("dayAbbr");
            tmpTime = floor(((tmpTime / 60) % 24), 1);
            if (tmpTime > 0)
                resTimeStr += " " + tmpTime + tr2("hourAbbr");
        } else {
            resTimeStr = floor(tmpTime / 60, 1) + tr2("hourAbbr");
            tmpTime = floor(tmpTime % 60, 1);
            if (tmpTime > 0)
                resTimeStr += " " + tmpTime + tr2("minAbbr");
        }
    } else
        resTimeStr = round(tmpTime % 60, 1) + tr2("minAbbr");
    return resTimeStr;
}

function addInstruction(main, instr, instrIndex, lngLat) {
    var sign = instr.sign;
    if (instrIndex === 0)
        sign = "marker-icon-green";
    else if (sign === -3)
        sign = "sharp_left";
    else if (sign === -2)
        sign = "left";
    else if (sign === -1)
        sign = "slight_left";
    else if (sign === 0)
        sign = "continue";
    else if (sign === 1)
        sign = "slight_right";
    else if (sign === 2)
        sign = "right";
    else if (sign === 3)
        sign = "sharp_right";
    else if (sign === 4)
        sign = "marker-icon-red";
    else
        throw "did not found sign " + sign;
    var title = instr.text;
    if (instr.annotationText) {
        if (!title)
            title = instr.annotationText;
        else
            title = title + ", " + instr.annotationText;
    }
    var distance = instr.distance;
    var str = "<td class='instr_title'>" + title + "</td>";

    if (distance > 0) {
        str += " <td class='instr_distance'><span>"
                + createDistanceString(distance) + "<br/>"
                + createTimeString(instr.time) + "</span></td>";
    }

    if (sign !== "continue") {
        var indiPic = "<img class='pic' style='vertical-align: middle' src='" +
                window.location.pathname + "img/" + sign + ".png'/>";
        str = "<td class='instr_pic'>" + indiPic + "</td>" + str;
    } else
        str = "<td class='instr_pic'/>" + str;
    var instructionDiv = $("<tr class='instruction'/>");
    instructionDiv.html(str);
    if (lngLat) {
        instructionDiv.click(function () {
            if (routeSegmentPopup)
                map.removeLayer(routeSegmentPopup);

            routeSegmentPopup = L.popup().
                    setLatLng([lngLat[1], lngLat[0]]).
                    setContent(title).
                    openOn(map);
        });
    }
    main.append(instructionDiv);
}

function getCenter(bounds) {
    var center = {
        lat: 0,
        lng: 0
    };
    if (bounds.initialized) {
        center.lat = (bounds.minLat + bounds.maxLat) / 2;
        center.lng = (bounds.minLon + bounds.maxLon) / 2;
    }
    return center;
}

function parseUrlWithHisto() {
    if (window.location.hash)
        return parseUrl(window.location.hash);

    return parseUrl(window.location.search);
}

function parseUrlAndRequest() {
    return parseUrl(window.location.search);
}

function parseUrl(query) {
    var index = query.indexOf('?');
    if (index >= 0)
        query = query.substring(index + 1);
    var res = {};
    var vars = query.split("&");
    for (var i = 0; i < vars.length; i++) {
        var indexPos = vars[i].indexOf("=");
        if (indexPos < 0)
            continue;

        var key = vars[i].substring(0, indexPos);
        var value = vars[i].substring(indexPos + 1);
        value = decodeURIComponent(value.replace(/\+/g, ' '));

        if (typeof res[key] === "undefined") {
            if (value === 'true')
                res[key] = true;
            else if (value === 'false')
                res[key] = false;
            else {
                var tmp = Number(value);
                if (isNaN(tmp))
                    res[key] = value;
                else
                    res[key] = Number(value);
            }
        } else if (typeof res[key] === "string") {
            var arr = [res[key], value];
            res[key] = arr;
        } else
            res[key].push(value);

    }
    return res;
}

function mySubmit() {
    var fromStr = $("#fromInput").val();
    var toStr = $("#toInput").val();
    if (toStr == "To" && fromStr == "From") {
        // TODO print warning
        return;
    }
    if (fromStr == "From") {
        // no special function
        return;
    }
    if (toStr == "To") {
        // lookup area
        ghRequest.from = new GHInput(fromStr);
        $.when(resolveFrom()).done(function () {
            focus(ghRequest.from);
        });
        return;
    }
    // route!
    resolveCoords(fromStr, toStr);


}

function floor(val, precision) {
    if (!precision)
        precision = 1e6;
    return Math.floor(val * precision) / precision;
}

function round(val, precision) {
    if (precision === undefined)
        precision = 1e6;
    return Math.round(val * precision) / precision;
}

function tr(key, args) {
    return tr2("web." + key, args);
}

function tr2(key, args) {
    if (key === null) {
        console.log("ERROR: key was null?");
        return "";
    }
    if (defaultTranslationMap === null) {
        console.log("ERROR: defaultTranslationMap was not initialized?");
        return key;
    }
    key = key.toLowerCase();
    var val = defaultTranslationMap[key];
    if (!val && enTranslationMap)
        val = enTranslationMap[key];
    if (!val)
        return key;

    return stringFormat(val, args);
}

function stringFormat(str, args) {
    if (typeof args === 'string')
        args = [args];

    if (str.indexOf("%1$s") >= 0) {
        // with position arguments ala %2$s
        return str.replace(/\%(\d+)\$s/g, function (match, matchingNum) {
            matchingNum--;
            return typeof args[matchingNum] != 'undefined' ? args[matchingNum] : match;
        });
    } else {
        // no position so only values ala %s
        var matchingNum = 0;
        return str.replace(/\%s/g, function (match) {
            var val = typeof args[matchingNum] != 'undefined' ? args[matchingNum] : match;
            matchingNum++;
            return val;
        });
    }
}

function initI18N() {
    $('#searchButton').attr("value", tr("searchButton"));
    $('#fromInput').attr("placeholder", tr("fromHint"));
    $('#toInput').attr("placeholder", tr("toHint"));
    $('#gpxExportButton').attr("title", tr("gpxExportButton"));
}

function exportGPX() {
    if (ghRequest.from.isResolved() && ghRequest.to.isResolved())
        window.open(ghRequest.createGPXURL());
    return false;
}

function getAutoCompleteDiv(fromOrTo) {
    return $('#' + fromOrTo + 'Input');
}

function hideAutoComplete() {
    getAutoCompleteDiv("from").autocomplete().hide();
    getAutoCompleteDiv("to").autocomplete().hide();
}

function formatValue(orig, query) {
    var pattern = '(' + $.Autocomplete.utils.escapeRegExChars(query) + ')';
    return orig.replace(new RegExp(pattern, 'gi'), '<strong>$1<\/strong>');
}

function setAutoCompleteList(fromOrTo) {
    var isFrom = fromOrTo === "from";
    var pointIndex = isFrom ? 1 : 2;
    var myAutoDiv = getAutoCompleteDiv(fromOrTo);

    var options = {
        containerClass: "complete-" + pointIndex,
        /* as we use can potentially use jsonp we need to set the timeout to a small value */
        timeout: 1000,
        /* avoid too many requests when typing quickly */
        deferRequestBy: 5,
        minChars: 2,
        maxHeight: 510,
        noCache: true,
        /* this default could be problematic: preventBadQueries: true, */
        triggerSelectOnValidInput: false,
        autoSelectFirst: false,
        paramName: "q",
        dataType: ghRequest.dataType,
        onSearchStart: function (params) {
            // query server only if not a parsable point (i.e. format lat,lon)
            var val = new GHInput(params.q).lat;
            return val === undefined;
        },
        serviceUrl: function () {
            return ghRequest.createGeocodeURL();
        },
        transformResult: function (response, originalQuery) {
            response.suggestions = [];
            for (var i = 0; i < response.hits.length; i++) {
                var hit = response.hits[i];
                response.suggestions.push({value: dataToText(hit), data: hit});
            }
            return response;
        },
        onSearchError: function (element, q, jqXHR, textStatus, errorThrown) {
            // too many errors if interrupted console.log(element + ", " + JSON.stringify(q) + ", textStatus " + textStatus + ", " + errorThrown);
        },
        formatResult: function (suggestion, currInput) {
            // avoid highlighting for now as this breaks the html sometimes
            return dataToHtml(suggestion.data, currInput);
        },
        onSelect: function (suggestion) {
            options.onPreSelect(suggestion);
        },
        onPreSelect: function (suggestion) {
            var req = ghRequest.to;
            if (isFrom)
                req = ghRequest.from;

            myAutoDiv.autocomplete().disable();

            var point = suggestion.data.point;
            req.setCoord(point.lat, point.lng);

            req.input = suggestion.value;
            if (!routeIfAllResolved())
                focus(req, 15, isFrom);

            myAutoDiv.autocomplete().enable();
        }
    };

    myAutoDiv.autocomplete(options);

    // with the following more stable code we cannot click on suggestions anylonger
//    $("#" + fromOrTo + "Input").focusout(function() {
//        myAutoDiv.autocomplete().disable();
//        myAutoDiv.autocomplete().hide();
//    });
//    $("#" + fromOrTo + "Input").focusin(function() {
//        myAutoDiv.autocomplete().enable();
//    });
}

function dataToHtml(data, query) {
    var element = "";
    if (data.name)
        element += "<div class='nameseg'>" + formatValue(data.name, query) + "</div>";
    var addStr = "";
    if (data.postcode)
        addStr = data.postcode;
    if (data.city)
        addStr = insComma(addStr, data.city);
    if (data.country)
        addStr = insComma(addStr, data.country);

    if (addStr)
        element += "<div class='cityseg'>" + formatValue(addStr, query) + "</div>";

    if (data.osm_key === "highway") {
        // ignore
    }
    if (data.osm_key === "place") {
        element += "<span class='moreseg'>" + data.osm_value + "</span>";
    } else
        element += "<span class='moreseg'>" + data.osm_key + "</span>";
    return element;
}

function dataToText(data) {
    var text = "";
    if (data.name)
        text += data.name;

    if (data.postcode)
        text = insComma(text, data.postcode);

    // make sure name won't be duplicated
    if (data.city && text.indexOf(data.city) < 0)
        text = insComma(text, data.city);

    if (data.country && text.indexOf(data.country) < 0)
        text = insComma(text, data.country);
    return text;
}

function isProduction() {
    return host.indexOf("graphhopper.com") > 0;
}

//Reads the selected weighting option from the webpage and passes it to be appended in the request
function setWeighting(request) {
    //@Amal Elgammal: to add the selected weighting to the request
    var weighting = $("#weightingSelect").val().toLowerCase();
    weighting = weighting.replace(" ", "_");
    request.weighting = weighting;
}

//Gets the value of elevation and  passes it to be appended in the request
function setElevation(request) {
    //@Amal Elgammal: to add the selected Elevation to the request
    if ($("#elevationCheck").prop('checked'))
    {
        request.elevation = true;
    }
    else
    {
        request.elevation = false;
    }
    console.log("request.elevation vale in setElevation function is: " + request.elevation);

}

function toTitleCase(str)
{
    return str.replace(/\w\S*/g, function (txt) {
        return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
    });
}

function setVechile(request)
{

}

/*function callHelloRWS()
 {
 var url = "http://localhost:8080/GHRestfulWS/webresources/helloVersailles";
 console.log("url inside getMsg" + url);
 return $.ajax({
 url: url,
 type: "GET",
 dataType: "plain",
 timeout: 3000
 });
 
 }*/