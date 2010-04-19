/*
**  jhistory 0.6 - jQuery plugin allowing simple non-intrusive browser history
**  author: Jim Palmer; released under MIT license
**    collage of ideas from Taku Sano, Mikage Sawatari, david bloom and Klaus Hartl
**  CONFIG -- place in your document.ready function two possible config settings:
**    $.history._cache = 'cache.html'; // REQUIRED - location to your cache response handler (static flat files prefered)
**    $.history.stack = {<old object>}; // OPTIONAL - prefill this with previously saved history stack (i.e. saved with session)
*/
(function($) {
	// initialize jhistory - the iframe controller and setinterval'd listener (pseudo observer)
	$(function () {
		// create the hidden iframe if not on the root window.document.body on-demand
		$("body").append('<iframe class="__historyFrame" src="' + $.history._cache +
			'" style="border:0px; width:0px; height:0px; visibility:hidden;" />');
		// setup interval function to check for changes in "history" via iframe hash and call appropriate callback function to handle it
		$.history.intervalId = $.history.intervalId || window.setInterval(function () {
				// fetch current cursor from the iframe document.URL or document.location depending on browser support
				var cursor = $(".__historyFrame").contents().attr( $.browser.msie ? 'URL' : 'location' ).toString().split('#')[1];
				// display debugging information if block id exists
				$('#__historyDebug').html('"' + $.history.cursor + '" vs "' + cursor + '" - ' + (new Date()).toString());
				// if cursors are different (forw/back hit) then reinstate data only when iframe is done loading
				if ( parseFloat($.history.cursor) >= 0 && parseFloat($.history.cursor) != ( parseFloat(cursor) || 0 ) ) {
					// set the history cursor to the current cursor
					$.history.cursor = parseFloat(cursor) || 0;
					// reinstate the current cursor data through the callback
					if ( typeof($.history.callback) == 'function' ) {
						// prevent the callback from re-inserting same history element
						$.history._locked = true;
						$.history.callback( $.history.stack[ cursor ], cursor );
						$.history._locked = false;
					}
				}
			}, 150);
	});
	// core history plugin functionality - handles singleton instantiation and history observer interval
	$.history = function ( store ) {
		// init the stack if not supplied
		if (!$.history.stack) $.history.stack = {};
		// avoid new history entries when in the middle of a callback handler
		if ($.history._locked) return false;
		// set the current unix timestamp for our history
		$.history.cursor = (new Date()).getTime().toString();
		// insert copy into the stack with current cursor
		$.history.stack[ $.history.cursor ] = $.extend( true, {}, store );
		// force the new hash we're about to write into the IE6/7 history stack
		if ( $.browser.msie )
			$('.__historyFrame')[0].contentWindow.document.open().close();
		// write the fragment id to the hash history - webkit required full href reset - ie/ff work with simple hash manipulation
		if ( $.browser.safari )
			$('.__historyFrame').contents()[0].location.href = $('.__historyFrame').contents()[0].location.href.split('?')[0] +
				'?' + $.history.cursor + '#' + $.history.cursor;
		else
			$('.__historyFrame').contents()[0].location.hash = '#' + $.history.cursor;
	}
})(jQuery);
