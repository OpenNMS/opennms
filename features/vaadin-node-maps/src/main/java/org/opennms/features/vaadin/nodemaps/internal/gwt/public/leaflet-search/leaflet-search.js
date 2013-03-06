/*
 * Leaflet Search Control 1.3.5
 * http://labs.easyblog.it/maps/leaflet-search
 *
 * https://github.com/stefanocudini/leaflet-search
 * https://bitbucket.org/zakis_/leaflet-search
 *
 * Copyright 2013, Stefano Cudini - stefano.cudini@gmail.com
 * Licensed under the MIT license.
 */

L.Control.SearchMarker = L.Marker.extend({
//extended L.Marker for create new type of marker has animated circle around
//and has new methods: .hide() .show() .setTitle() .animate()
//TODO start L.Control.SearchMarker.animation after setView or panTo, maybe with map.on('moveend')...
	includes: L.Mixin.Events,
	
	options: {
		radius: 10,
		weight: 3,
		color: '#e03',
		stroke: true,
		fill: false,
		title: '',
		marker: false	//show icon optional, show only circleLoc
	},
	
	initialize: function (latlng, options) {
		L.setOptions(this, options);
		L.Marker.prototype.initialize.call(this, latlng, options);
		this._circleLoc = new L.CircleMarker(latlng, this.options);
	},

	onAdd: function (map) {
		L.Marker.prototype.onAdd.call(this, map);
		this._circleLoc.addTo(map);
		this.hide();
	},
	
	setLatLng: function (latlng) {
		L.Marker.prototype.setLatLng.call(this, latlng);
		this._circleLoc.setLatLng(latlng);
		return this;
	},
	
	setTitle: function(title) {
		this.options.title = title;
		this._icon.title = title;
		return this;
	},

	show: function() {
		if(this.options.marker)
		{
			if(this._icon)
				this._icon.style.display = 'block';
			if(this._shadow)
				this._shadow.style.display = 'block';
			//this._bringToFront();
		}
		if(this._circleLoc)
		{
			this._circleLoc.setStyle({fill: this.options.fill, stroke: this.options.stroke});
			//this._circleLoc.bringToFront();
		}
		return this;
	},

	hide: function() {
		if(this._icon)
			this._icon.style.display = 'none';
		if(this._shadow)
			this._shadow.style.display = 'none';
		if(this._circleLoc)			
			this._circleLoc.setStyle({fill: false, stroke: false});
		return this;
	},

	animate: function() {
	//TODO refact L.Control.SearchMarker.animate() more smooth! and use bringToFront()
		var circle = this._circleLoc,
			tInt = 200,	//time interval
			ss = 10,	//frames
			mr = parseInt(circle._radius/ss),
			oldrad = circle._radius,
			newrad = circle._radius * 2,
			acc = 0;

		circle._timerAnimLoc = setInterval(function() {
			acc += 0.5;
			mr += acc;	//adding acceleration
			newrad -= mr;
			
			circle.setRadius(newrad);

			if(newrad<oldrad)
			{
				clearInterval(circle._timerAnimLoc);
				circle.setRadius(circle.options.radius);//reset radius
				//if(typeof afterAnimCall == 'function')
					//afterAnimCall();
					//TODO use create event animateEnd in L.Control.SearchMarker 
			}
		}, tInt);
		
		return this;
	 }
});


L.Control.Search = L.Control.extend({
	includes: L.Mixin.Events,
	
	options: {
		layer: null,				//layer where search markers
		propertyName: 'title',		//property in marker.options trough filter elements in layer
		//TODO add option searchLoc or searchLat,searchLon for remapping json data fields
		searchCall: null,			//function that fill _recordsCache, receive searching text in first param
		jsonpUrl: '',				//url for search by jsonp service, ex: "search.php?q={s}&callback={c}"
		filterJSON: null,			//callback for filtering data to _recordsCache
		minLength: 1,				//minimal text length for autocomplete
		initial: true,				//search elements only by initial text
		autoType: true,				//complete input with first suggested result and select this filled-in text.
		tooltipLimit: -1,			//limit max results to show in tooltip. -1 for no limit.
		tipAutoSubmit: true,  		//auto map panTo when click on tooltip
		autoResize: true,			//autoresize on input change
		autoCollapse: false,		//collapse search control after submit(on button or on tips if enabled tipAutoSubmit)
		//TODO add option for persist markerLoc after autoCollapse!
		autoCollapseTime: 1200,		//delay for autoclosing alert and collapse after blur
		animateLocation: true,		//animate a circle over location found
		markerLocation: false,		//draw a marker in location found
		zoom: null,					//zoom after pan to location found, default: map.getZoom()
		text: 'Search...',			//placeholder value	
		textCancel: 'Cancel',		//title in cancel button
		textErr: 'Location not found',	//error message
		position: 'topleft'
	},
//FIXME option condition problem {autoCollapse: true, markerLocation: true} not show location
//FIXME option condition problem {autoCollapse:false }
	initialize: function(options) {
		L.Util.setOptions(this, options);
		this._inputMinSize = this.options.text ? this.options.text.length : 10;
		this._layer = this.options.layer || new L.LayerGroup();
		this._filterJSON = this.options.filterJSON || this._defaultFilterJSON;
		this._autoTypeTmp = this.options.autoType;	//useful for disable autoType temporarily in delete/backspace keydown
		this._delayType = 300;		//delay after searchCall
		this._recordsCache = {};	//key,value table! that store locations! format: key,latlng
	},

	onAdd: function (map) {
		this._map = map;
		this._markerLoc = new L.Control.SearchMarker([0,0],{marker: this.options.markerLocation});
		this._layer.addLayer(this._markerLoc);
		this._layer.addTo(map);
		
		this._container = L.DomUtil.create('div', 'leaflet-control-search');
		this._alert = this._createAlert('search-alert');		
		this._input = this._createInput(this.options.text, 'search-input');
		this._tooltip = this._createTooltip('search-tooltip');		
		this._cancel = this._createCancel(this.options.textCancel, 'search-cancel');
		this._createButton(this.options.text, 'search-button');
		return this._container;
	},

	onRemove: function(map) {
		this._recordsCache = {};
	},
	
	showAlert: function(text) {
		this._alert.style.display = 'block';
		this._alert.innerHTML = text;
		var that = this;
		clearTimeout(this.timerAlert);
		this.timerAlert = setTimeout(function() {
			that._alert.style.display = 'none';
		},this.options.autoCollapseTime);
	},

	cancel: function() {
		this._input.value = '';
		this._handleKeypress({keyCode:8});//simulate backspace keypress
		this._input.size = this._inputMinSize;
		this._input.focus();
		this._cancel.style.display = 'none';
	},

	expand: function() {		
		this._input.style.display = 'block';
		L.DomUtil.addClass(this._container, 'search-exp');	
		this._input.focus();
	},

	collapse: function() {
		this._hideTooltip();
		this.cancel();
		this._alert.style.display = 'none';
		this._input.style.display = 'block';
		this._cancel.style.display = 'none';
		L.DomUtil.addClass(this._container, 'search-exp');		
		this._markerLoc.hide();
		this._input.focus();
	},
	
	collapseDelayed: function() {	//collapse after delay, used on_input blur
		var that = this;
		this.timerCollapse = setTimeout(function() {
			that.collapse();
		}, this.options.autoCollapseTime);
	},

	collapseDelayedStop: function() {
		clearTimeout(this.timerCollapse);
	},

////start DOM creations
	_createAlert: function(className) {
		var alert = L.DomUtil.create('div', className, this._container);
		alert.style.display = 'none';
		return alert;
	},

	_createInput: function (text, className) {
		var input = L.DomUtil.create('input', className, this._container);
		input.type = 'text';
		input.size = this._inputMinSize;
		input.value = '';
		input.autocomplete = 'off';
		input.placeholder = text;
		input.style.display = 'none';
		
		L.DomEvent
			.on(input, 'keyup', this._handleKeypress, this)
			.on(input, 'keydown', this._handleAutoresize, this);
			//.on(input, 'click', input.focus());
			//.on(input, 'blur', this.collapseDelayed, this)
			//.on(input, 'focus', this.collapseDelayedStop, this);
		
		return input;
	},

	_createCancel: function (title, className) {
		var cancel = L.DomUtil.create('a', className, this._container);
		cancel.href = '#';
		cancel.title = title;
		cancel.style.display = 'none';
		cancel.innerHTML = "<span>&otimes;</span>";//imageless(see css)

		L.DomEvent
			.on(cancel, 'click', L.DomEvent.stop, this)
			.on(cancel, 'click', this.cancel, this);

		return cancel;
	},
	
	_createButton: function (title, className) {
		var button = L.DomUtil.create('a', className, this._container);
		button.href = '#';
		button.title = title;

		L.DomEvent
			.on(button, 'click', L.DomEvent.stop, this)
			.on(button, 'click', this._handleSubmit, this);
			//.on(button, 'focus', this.collapseDelayedStop, this)
			//.on(button, 'blur', this.collapseDelayed, this);

		return button;
	},

	_createTooltip: function(className) {
		var tool = L.DomUtil.create('div', className, this._container);
		tool.style.display = 'none';

		var that = this;
		L.DomEvent
			.disableClickPropagation(tool)
			.on(tool, 'blur', this.collapseDelayed, this)
			.on(tool, 'mousewheel', function(e) {
				that.collapseDelayedStop();
				L.DomEvent.stopPropagation(e);//disable zoom map
			}, this)
			.on(tool, 'mouseover', function(e) {
				that._input.focus();//collapseDelayedStop
			}, this);
		return tool;
	},

	_createTip: function(text) {
		var tip = L.DomUtil.create('a', 'search-tip');
		tip.href = '#';
		tip.innerHTML = text;
		//TODO add new option: the callback for building the tip content from text argument

		this._tooltip.currentSelection = -1;  //inizialized for _handleArrowSelect()

		L.DomEvent
			.disableClickPropagation(tip)		
			.on(tip, 'click', L.DomEvent.stop, this)
			.on(tip, 'click', function(e) {
				this._input.value = text;
				this._input.focus();
				this._hideTooltip();
				this._handleAutoresize();	
				if(this.options.tipAutoSubmit)//go to location at once
					this._handleSubmit();
			}, this);

		return tip;
	},

//////end DOM creations
	
	_showTooltip: function() {	//Filter this._recordsCache with this._input.values and show tooltip

		if(this._input.value.length < this.options.minLength)
			return this._hideTooltip();

		var regFilter = new RegExp("^[.]$|[\[\]|()*]",'g'),	//remove . * | ( ) ] [
			text = this._input.value.replace(regFilter,''),		//sanitize text
			I = this.options.initial ? '^' : '',  //search for initial text
			regSearch = new RegExp(I + text,'i'),	//for search in _recordsCache
			ntip = 0;
		
		this._tooltip.innerHTML = '';

		for(var key in this._recordsCache)
		{
			//if(regSearch.test(key))//search in records
			//{
				if (ntip == this.options.tooltipLimit) break;
				this._tooltip.appendChild( this._createTip(key) );
				ntip++;
			//}
		}
		
		if(ntip > 0)
		{
			this._tooltip.style.display = 'block';
			if(this._autoTypeTmp)
				this._autoType();
			this._autoTypeTmp = this.options.autoType;//reset default value
		}
		else
			this._hideTooltip();

		this._tooltip.scrollTop = 0;
		return ntip;
	},

	_hideTooltip: function() {
		this._tooltip.style.display = 'none';
		this._tooltip.innerHTML = '';
		return 0;
	},

	_defaultFilterJSON: function(jsonraw) {	//default callback for filter data
		var jsonret = {},
			propname = this.options.propertyName;

		for(var i in jsonraw)
		{
			if( jsonraw[i].hasOwnProperty(propname) )
				jsonret[ jsonraw[i][propname] ]= L.latLng( jsonraw[i].loc );
		}
		//TODO use: throw new Error("my message");on error
		return jsonret;
	},
	
	_recordsFromJsonp: function(text, callAfter) {  //extract searched records from remote jsonp service
		
		var that = this;
		L.Control.Search.callJsonp = function(data) {	//jsonp callback
			var fdata = that._filterJSON.apply(that,[data]);//defined in inizialize...
			callAfter(fdata);
		}
		var script = L.DomUtil.create('script','search-jsonp', document.getElementsByTagName('body')[0] ),			
			url = L.Util.template(this.options.jsonpUrl, {s: text, c:"L.Control.Search.callJsonp"});
			//parsing url
			//rnd = '&_='+Math.floor(Math.random()*10000);
			//TODO add rnd param or randomize callback name! in recordsFromJsonp
		script.type = 'text/javascript';
		script.src = url;
		return this;
	},

	_recordsFromLayer: function() {	//return table: key,value from layer
		var retRecords = {},
			propname = this.options.propertyName;
		
		//TODO bind _recordsFromLayer to map events: layeradd layerremove update ecc
		//TODO implement filter by element type: marker|polyline|circle...
		//TODO caching retRecords while layerSearch not change, controlling on 'load' event
		//TODO return also marker! in _recordsFromLayer
		
		this._layer.eachLayer(function(marker) {
			if(marker.options.hasOwnProperty(propname) && marker.options[propname])
				retRecords[ marker.options[propname] ] = marker.getLatLng();
		},this);
		
		return retRecords;
	},

	_autoType: function() {
		
		var start = this._input.value.length,
			firstRecord = this._tooltip.getElementsByTagName('a')[0].innerHTML,
			//FIXME _autoType find a way without innerHTML that also guarantees correct order (application developer may want images in tooltip)
			end = firstRecord.length;
			
		this._input.value = firstRecord;
		this._handleAutoresize();
		
		if (this._input.createTextRange) {
			var selRange = this._input.createTextRange();
			selRange.collapse(true);
			selRange.moveStart('character', start);
			selRange.moveEnd('character', end);
			selRange.select();
		}
		else if(this._input.setSelectionRange) {
			this._input.setSelectionRange(start, end);
		}
		else if(this._input.selectionStart) {
			this._input.selectionStart = start;
			this._input.selectionEnd = end;
		}
	},

	_handleKeypress: function (e) {	//run _input keyup event
		switch(e.keyCode)
		{
			case 27: //Esc
				this.collapse();
			break;
			case 13: //Enter
				this._handleSubmit();	//do search
			break;
			case 38://Up
				this._handleArrowSelect(-1);
			break;
			case 40://Down
				this._handleArrowSelect(1);
			break;
			case 37://Left
			case 39://Right
			case 16://Shift
			case 17://Ctrl
			//case 32://Space
			break;
			case 8://backspace
			case 46://delete
				this._autoTypeTmp = false;//disable temporarily autoType
			default://All keys
				if (e.altKey || e.shiftKey || e.ctrlKey || e.metaKey) { return; }

				if(this._input.value.length)
					this._cancel.style.display = 'block';
				else
					this._cancel.style.display = 'none';

				if(this._input.value.length >= this.options.minLength)
				{
					var that = this;
					clearTimeout(this.timerKeypress);	//cancel last search request while type in				
					this.timerKeypress = setTimeout(function() {	//delay before request, for limit jsonp/ajax request

						that._fillRecordsCache();
					
					}, this._delayType);
				}
				else
					this._hideTooltip();
		}
	},
	
	_fillRecordsCache: function() {
	
		var inputText = this._input.value;

//TODO important optimization!!! always append data in this._recordsCache
//now _recordsCache content is emptied and replaced with new data founded
//always appending data on _recordsCache give the possibility of caching ajax, jsonp and layersearch!
		
		//TODO here insert function that search inputText FIRST in _recordsCache keys and if not find results.. 
		//run one of callbacks search(searchCall,jsonpUrl or options.layer)
		//and run this._showTooltip

//TODO change structure of _recordsCache
//	like this: _recordsCache = {"text-key1": {loc:[lat,lng], ..other attributes.. }, {"text-key2": {loc:[lat,lng]}...}, ...}
//	in this mode every record can have a free structure of attributes, only 'loc' is required
//
		L.DomUtil.addClass(this._container, 'search-load');

		if(this.options.searchCall)	//CUSTOM SEARCH CALLBACK(USUALLY FOR AJAX SEARCHING)
		{
			this._recordsCache = this.options.searchCall.apply(this,[inputText]);

			this._showTooltip();

			L.DomUtil.removeClass(this._container, 'search-load');
			//FIXME removeClass .search-load apparently executed before searchCall!! A BIG MYSTERY!
		}
		else if(this.options.jsonpUrl)	//JSONP SERVICE REQUESTING
		{
			var that = this;
			this._recordsFromJsonp(inputText, function(data) {// is async request then it need callback
				that._recordsCache = data;
				that._showTooltip();
				L.DomUtil.removeClass(that._container, 'search-load');
			});
		}
		else if(this.options.layer)	//SEARCH ELEMENTS IN PRELOADED LAYER
		{
			this._recordsCache = this._recordsFromLayer();	//fill table key,value from markers into layer				
			this._showTooltip();
			L.DomUtil.removeClass(this._container, 'search-load');
		}
	},
	
	//FIXME _handleAutoresize Should resize max search box size when map is resized.
	_handleAutoresize: function() {	//autoresize this._input
	//TODO refact _handleAutoresize now is not accurate
		if(this.options.autoResize && (this._container.offsetWidth + 45 < this._map._container.offsetWidth))
			this._input.size = this._input.value.length<this._inputMinSize ? this._inputMinSize : this._input.value.length;
	},

	_handleArrowSelect: function(velocity) {
	
		var searchTips = this._tooltip.getElementsByTagName('a');
		
		for (i=0; i<searchTips.length; i++) {	// Erase all highlighting
			L.DomUtil.removeClass(searchTips[i], 'search-tip-select');
		}
		
		if ((velocity == 1 ) && (this._tooltip.currentSelection >= (searchTips.length - 1))) {// If at end of list.
			L.DomUtil.addClass(searchTips[this._tooltip.currentSelection], 'search-tip-select');
		}
		else if ((velocity == -1 ) && (this._tooltip.currentSelection <= 0)) { // Going back up to the search box.
			this._tooltip.currentSelection = -1;
		}
		else if (this._tooltip.style.display != 'none') { // regular up/down
			this._tooltip.currentSelection += velocity;
			
			L.DomUtil.addClass(searchTips[this._tooltip.currentSelection], 'search-tip-select');
			
			this._input.value = searchTips[this._tooltip.currentSelection].innerHTML;

			// scroll:
			var tipOffsetTop = searchTips[this._tooltip.currentSelection].offsetTop;
			
			if (tipOffsetTop + searchTips[this._tooltip.currentSelection].clientHeight >= this._tooltip.scrollTop + this._tooltip.clientHeight) {
				this._tooltip.scrollTop = tipOffsetTop - this._tooltip.clientHeight + searchTips[this._tooltip.currentSelection].clientHeight;
			}
			else if (tipOffsetTop <= this._tooltip.scrollTop) {
				this._tooltip.scrollTop = tipOffsetTop;
			}
		}
	},

	_handleSubmit: function() {

		// deselect text:
		var sel;
		if ((sel = this._input.selection) && sel.empty) {
			sel.empty();
		}
		else {
			if (this._input.getSelection) {
				this._input.getSelection().removeAllRanges();
			}
			this._input.selectionStart = this._input.selectionEnd;
		}

		if( this._findLocation(this._input.value)===false )
			this.showAlert( this.options.textErr );//location not found, alert!
//		if(this._input.style.display == 'none')	//on first click show _input only
//			this.expand();
//		else
//		{
//			if(this._input.value == '')	//hide _input only
//				this.collapse();
//			else
//			{
//				if( this._findLocation(this._input.value)===false )
//					this.showAlert( this.options.textErr );//location not found, alert!
//			}
//		}
		this._input.focus();	//block collapseDelayed after _button blur
	},
	
	_animateCircle: function(circle, afterAnimCall) {
	//TODO refact _animateCircle more smooth!

		var tInt = 200,//time interval
			ss = 10,//animation frames
			mr = parseInt(circle._radius/ss),
			newrad = circle._radius * 2,
			acc = 0;

		circle._timerAnimLoc = setInterval(function() {  //animation
			acc += 0.5;
			mr += acc;	//adding acceleration
			newrad -= mr;
			
			circle.setRadius(newrad);

			if(newrad<2)//stop animation
			{
				clearInterval(circle._timerAnimLoc);
				circle.setRadius(circle.options.radius);//reset radius
				if(typeof afterAnimCall == 'function')
					afterAnimCall();
			}
		}, tInt);
	},
	
	_findLocation: function(text) {	//get location from table _recordsCache and pan to map!
	
		if( this._recordsCache.hasOwnProperty(text) )
		{
			var newCenter = this._recordsCache[text];//search in table key,value
			
			if(this.options.zoom)
				this._map.setView(newCenter, this.options.zoom);
			else
				this._map.panTo(newCenter);

			this._markerLoc.setLatLng(newCenter);  //show circle/marker in location found
			this._markerLoc.setTitle(text);
			this._markerLoc.show();
			if(this.options.animateLocation)
				this._markerLoc.animate();
			
			//FIXME autoCollapse option hide this._markerLoc before that visualized!!
//			if(this.options.autoCollapse)
//				this.collapse();

			return newCenter;
		}
//		else
//			this._markerLoc.hide();//remove this._circleLoc, this._markerLoc from map
//maybe needless
		
		return false;
	}
});
