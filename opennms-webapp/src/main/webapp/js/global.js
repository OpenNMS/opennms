function getBaseHref() {
	return document.getElementsByTagName('base')[0].href;
}

function setLocation(url) {
	window.location.href = getBaseHref() + url;
}

function toggle(booleanValue, elementName) {
    var checkboxes = document.getElementsByName(elementName);
    for (var index in checkboxes) {
        checkboxes[index].checked = booleanValue;
    }
}

if (typeof requirejs === "function") {
  requirejs.config({
    baseUrl: 'lib',
    paths: {
      dc: 'dcjs/dc.min',
      d3: 'd3/d3.min',
      crossfilter: 'crossfilter/crossfilter.min',
      backshift: '../js/backshift.onms.min',
      holder: '../js/holder.min'
    }
  });
}
