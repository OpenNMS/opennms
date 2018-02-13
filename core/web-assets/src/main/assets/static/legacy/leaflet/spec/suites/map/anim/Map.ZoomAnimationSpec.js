describe("Map.ZoomAnimation", function () {

	var container, spy;

	beforeEach(function () {
		container = document.createElement('div');
	});

	describe("zoomend event", function (done) {
		it.skipInPhantom("fires just once", function () {
			spy = sinon.spy();
			var map = new L.Map(container, {
				zoomAnimation: true,
				center: [0, 0],
				zoom: 1
			});

			map.on('zoomend moveend', spy);

			map.on('zoomend moveend', function () {
				expect(spy.callCount).to.equal(1);
				console.log('zoomends:', spy.callCount);
			});

			map.zoomIn();
			setTimeout(done, 3000);
		});
	});

	describe("zoomAnimationThreshold", function (done) {
		it.skipInPhantom("animates zoom if zoom delta is equal or lower", function (done) {
			spy = sinon.spy();
			var map = new L.Map(container, {
				zoomAnimation: true,
				center: [0, 0],
				zoom: 1,
				zoomAnimationThreshold: 4
			});

			map.on('zoomanim', spy);

			map.on('zoomend', function () {
				expect(spy.callCount).to.be.greaterThan(0);
				done();
			});

			map.zoomIn(4);
		});

		it.skipInPhantom("skips animation if zoom delta is greater", function (done) {
			spy = sinon.spy();
			var map = new L.Map(container, {
				zoomAnimation: true,
				center: [0, 0],
				zoom: 1,
				zoomAnimationThreshold: 4
			});

			map.on('zoomanim', spy);

			map.on('zoomend', function () {
				expect(spy.callCount).to.equal(0);
				done();
			});

			map.zoomIn(5);
		});
	});

});
