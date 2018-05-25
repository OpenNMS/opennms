describe('Option removeOutsideVisibleBounds', function () {

	/**
	 * Avoid as much as possible creating and destroying objects for each test.
	 * Instead, try re-using them, except for the ones under test of course.
	 * PhantomJS does not perform garbage collection for the life of the page,
	 * i.e. during the entire test process (Karma runs all tests in a single page).
	 * http://stackoverflow.com/questions/27239708/how-to-get-around-memory-error-with-karma-phantomjs
	 *
	 * The `beforeEach` and `afterEach do not seem to cause much issue.
	 * => they can still be used to initialize some setup between each test.
	 * Using them keeps a readable spec/index.
	 *
	 * But refrain from re-creating div and map every time. Re-use those objects.
	 */

	/////////////////////////////
	// SETUP FOR EACH TEST
	/////////////////////////////

	beforeEach(function () {

		// Nothing for this test suite.
		clock = sinon.useFakeTimers();

	});

	afterEach(function () {

		// Restore the previous setting, so that even in case of test failure, next tests are not affected.
		L.Browser.mobile = previousMobileSetting;

		if (group instanceof L.MarkerClusterGroup) {
			//group.removeLayers(group.getLayers());
			group.clearLayers();
			map.removeLayer(group);
		}

		// group must be thrown away since we are testing it with a potentially
		// different configuration at each test.
		group = null;

		clock.restore();
		clock = null;

	});


	/////////////////////////////
	// PREPARATION CODE
	/////////////////////////////

	var marker1 = L.marker([1.5, -0.4]), // 2 screens width away.
	    marker2 = L.marker([1.5, 0.6]), // 1 screen width away.
	    marker3 = L.marker([1.5, 1.5]), // In view port.
	    marker4 = L.marker([1.5, 2.4]), // 1 screen width away.
	    marker5 = L.marker([1.5, 3.4]), // 2 screens width away.
	    markers = [marker1, marker2, marker3, marker4, marker5],
	    previousMobileSetting = L.Browser.mobile,
	    div, map, group, i, clock;

	div = document.createElement('div');
	div.style.width = '200px';
	div.style.height = '200px';
	document.body.appendChild(div);

	map = L.map(div, { maxZoom: 18 });

	// Corresponds to zoom level 8 for the above div dimensions.
	map.fitBounds(new L.LatLngBounds([
		[1, 1],
		[2, 2]
	]));

	// Add all markers once to map then remove them immediately so that their icon is null (instead of undefined).
	for (i = 0; i < markers.length; i++) {
		map.removeLayer(markers[i].addTo(map));
	}


	function prepareGroup() {

		// "group" should be assigned with a Marker Cluster Group before calling this function.
		group.addTo(map);

		group.addLayers(markers);
	}


	/////////////////////////////
	// TESTS
	/////////////////////////////

	it('removes objects more than 1 screen away from view port by default', function () {

		group = L.markerClusterGroup();

		prepareGroup();

		expect(marker1._icon).to.be(null);
		expect(map._panes.markerPane.childNodes.length).to.be(3); // markers 2, 3 and 4.
		expect(marker5._icon).to.be(null);

	});

	it('removes objects out of view port by default for mobile device', function () {

		// Fool Leaflet, make it thinks it runs on a mobile device.
		L.Browser.mobile = true;

		group = L.markerClusterGroup();

		prepareGroup();

		expect(marker1._icon).to.be(null);
		expect(marker2._icon).to.be(null);
		expect(map._panes.markerPane.childNodes.length).to.be(1); // marker 3 only.
		expect(marker4._icon).to.be(null);
		expect(marker5._icon).to.be(null);

	});

	it('leaves all objects on map when set to false', function () {

		group = L.markerClusterGroup({
			removeOutsideVisibleBounds: false
		});

		prepareGroup();

		expect(map._panes.markerPane.childNodes.length).to.be(5); // All 5 markers.

	});


	// Following tests need markers at very high latitude.
	// They test the _checkBoundsMaxLat method against the default Web/Spherical Mercator projection maximum latitude (85.0511287798).
	// The actual map view should be '-1.0986328125,84.92929204957956,1.0986328125,85.11983467698401'
	// The expanded bounds without correction should be '-3.2958984375,84.7387494221751,3.2958984375,85.31037730438847'
	var latLngsMaxLatDefault = [
		[100, 3], // Impossible in real world, but nothing prevents the user from entering such latitude, and  Web/Spherical Mercator projection will still display it at 85.0511287798
		[85.2, 1.5], // 1 "screen" heights away.
		[85, 0], // In center of view.
		[84.8, -1.5], // 1 "screen" height away.
		[84.6, -3] // 2 "screens" height away.
	];

	function moveMarkersAndMapToMaxLat(latLngs, isSouth) {
		for (i = 0; i < markers.length; i++) {
			if (isSouth) {
				markers[i].setLatLng([-latLngs[i][0], latLngs[i][1]]);
			} else {
				markers[i].setLatLng(latLngs[i]);
			}
		}

		map.fitBounds([
			[isSouth ? -86 : 85, -1],
			[isSouth ? -85 : 86, 1] // The actual map view longitude span will be wider. '-1.0986328125,84.92929204957956,1.0986328125,85.11983467698401'
		]);
	}

	function checkProjection(latLngs) {
		expect(map.options.crs).to.equal(L.CRS.EPSG3857);
		expect(L.CRS.EPSG3857.projection).to.equal(L.Projection.SphericalMercator);
		expect(L.Projection.SphericalMercator.MAX_LATITUDE).to.be.a('number');

		var mapZoom = map.getZoom();

		for (i = 0; i < markers.length; i++) {
			try {
				expect(markers[i].__parent._zoom).to.be.below(mapZoom);
			} catch (e) {
				console.log("Failed marker: " + (i + 1));
				throw e;
			}
		}
	}

	it('includes objects above the Web Mercator projection maximum limit by default', function () {

		moveMarkersAndMapToMaxLat(latLngsMaxLatDefault);

		group = L.markerClusterGroup();

		prepareGroup();

		checkProjection(latLngsMaxLatDefault);

		expect(map._panes.markerPane.childNodes.length).to.be(4); // Markers 1, 2, 3 and 4.
		expect(marker5._icon).to.be(null);

	});

	it('includes objects below the Web Mercator projection minimum limit by default', function () {

		moveMarkersAndMapToMaxLat(latLngsMaxLatDefault, true);

		// Make sure we are really in Southern hemisphere.
		expect(map.getBounds().getNorth()).to.be.below(-80);

		group = L.markerClusterGroup();

		prepareGroup();

		checkProjection(latLngsMaxLatDefault);

		clock.tick(1000);

		expect(map._panes.markerPane.childNodes.length).to.be(4); // Markers 1, 2, 3 and 4.
		expect(marker5._icon).to.be(null);

	});


	// The actual map view should be '-1.0986328125,84.92929204957956,1.0986328125,85.11983467698401'
	var latLngsMaxLatMobile = [
		[100, 1], // Impossible in real world, but nothing prevents the user from entering such latitude, and  Web/Spherical Mercator projection will still display it at 85.0511287798
		[85.2, 0.5], // 1 "screen" heights away, but should be included by the correction.
		[85, 0], // In center of view.
		[84.9, -1], // 1 "screen" height away.
		[84.8, -1.5] // 2 "screens" height away.
	];

	it('includes objects above the Web Mercator projection maximum limit for mobile device', function () {

		// Fool Leaflet, make it thinks it runs on a mobile device.
		L.Browser.mobile = true;

		moveMarkersAndMapToMaxLat(latLngsMaxLatMobile);

		group = L.markerClusterGroup({
			maxClusterRadius: 10
		});

		prepareGroup();

		checkProjection(latLngsMaxLatMobile);

		expect(map._panes.markerPane.childNodes.length).to.be(3); // Markers 1, 2 and 3.
		expect(marker4._icon).to.be(null);
		expect(marker5._icon).to.be(null);

	});

	it('includes objects below the Web Mercator projection minimum limit for mobile device', function () {

		// Fool Leaflet, make it thinks it runs on a mobile device.
		L.Browser.mobile = true;

		moveMarkersAndMapToMaxLat(latLngsMaxLatMobile, true);

		// Make sure we are really in Southern hemisphere.
		expect(map.getBounds().getNorth()).to.be.below(-80);

		group = L.markerClusterGroup({
			maxClusterRadius: 10
		});

		prepareGroup();

		checkProjection(latLngsMaxLatMobile);

		expect(map._panes.markerPane.childNodes.length).to.be(3); // Markers 1, 2 and 3.
		expect(marker4._icon).to.be(null);
		expect(marker5._icon).to.be(null);

	});


	/////////////////////////////
	// CLEAN UP CODE
	/////////////////////////////

	map.remove();
	document.body.removeChild(div);

});
