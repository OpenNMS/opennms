export default class Util {
	static getBaseHref() {
		const base = document.getElementsByTagName('base')[0];
		if (base) {
			return base.href;
		}
		return '';
	}
	static setLocation(url) {
		if (window && window.location) {
			window.location.href = Util.getBaseHref() + url;
		}
	}
	static toggle(booleanValue, elementName) {
		var checkboxes = document.getElementsByName(elementName);
		for (var index in checkboxes) {
			if (checkboxes.hasOwnProperty(index)) {
				checkboxes[index].checked = booleanValue;
			}
		}
	}
}