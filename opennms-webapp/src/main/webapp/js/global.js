function getBaseHref() {
	return document.getElementsByTagName('base')[0].href;
}

function setLocation(url) {
	window.location.href = getBaseHref() + url;
}
