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