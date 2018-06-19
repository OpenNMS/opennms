if (!window.jQuery) {
	console.log('init: jquery-js'); // eslint-disable-line no-console

	const jQuery = require('jquery');
	window.jQuery = jQuery;
	window.$ = jQuery;
} else {
	window.$ = window.jQuery;
}

module.exports = window.jQuery;