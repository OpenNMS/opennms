var onms;
if (!onms) onms = {};
if (!onms.vs) onms.vs = {};


function print(s) {
    var doc = document.getElementById("debug");
    doc.innerHTML += s;
}


if (!onms.vs.NodeMapController) {


    onms.vs.NodeMapController = function NodeMapController(node_request_url, 
							   category_url,
							   map_id, 
							   cat_id,
							   tilecache_url, 
							   update_id,
							   cluster_threshold
							   ) {

	/* Start by loading all nodes/cpes/aps */
	this.category = "ALL";
	this.refresh_time = 30000;

	/* urls to xmlHttp request to. */
	this.node_request_url = node_request_url;
	this.cats_request_url = category_url;

	/* We have two modes, BASESTATIONS_ONLY will show basestations
	 * only with child nodes hidden beneith.  Past a certain zoom level
	 * we switch to SHOW_ALL where child nodes are show in their own 
	 * right.
	 */
	this.mode = this.MODE_BASESTATIONS_ONLY;

	/* Some id's for html tags */
	this.category_html_id = cat_id;
	this.update_id = update_id;

	/* The distance in pixels.  Nodes spaced within this radius 
	 * are clusterd together under one marker.
	 */
	this.cluster_threshold = cluster_threshold;

	/* Make 'this' accessable to our closures */
	var this_map_cont = this;
	function mapEvent(e) {
	    if (e.type == "zoomend") {
		this_map_cont.current_extent = this_map_cont.map.getExtent();

		this_map_cont.mode = this_map_cont.MODE_BASESTATIONS_ONLY;

		if (this_map_cont.map.getZoom() > 7) {
		    this_map_cont.mode = this_map_cont.MODE_SHOW_ALL_NODES;
		} 

		this_map_cont.clear_popups();
		this_map_cont.refresh();
	    }
	}

	this.map_options.eventListeners = {
	    "zoomend": mapEvent,
	};


	/* create some openlayers objects. */
	this.map = new OpenLayers.Map(map_id, this.map_options);
	this.osm_layer = new OpenLayers.Layer.WMS("VffMap0", 
						  tilecache_url, 
						  ({layers: 'osm', 
						    format: 'image/png'}));
    
	this.map.addLayer(this.osm_layer);
	this.map.setCenter(new OpenLayers.LonLat(0 ,0), 0);

	if (!this.map.getCenter()) this.map.zoomToMaxExtent();
	this.current_extent = this.map.getExtent();

	this.markers_layer = new OpenLayers.Layer.Markers("Markers");
	this.map.addLayer(this.markers_layer);

	/* Disable oncontext menu for right clicks */
	this.map.div.oncontextmenu = function(){return false;};	
	this.map.div.onmouseup = function(e){
	    if (OpenLayers.Event.isRightClick(e)){
		e.xy = this_map_cont.map.events.getMousePosition(e);
		e.geoxy = this_map_cont.map.getLonLatFromPixel(e.xy);

		var n = {};
		n.longitude = e.geoxy.lon;
		n.latitude = e.geoxy.lat;
		

		e.lonlat = onms.vs.utils.projection_lonlat_from_4326(this_map_cont.map, n);
		var location_div = document.getElementById("location");
		location_div.innerHTML = e.lonlat;
	    }
	};
	
	
	/* Now we keep track of map drags so that we can clear popups on
	 * a click to the map but not when the map is dragged.
	 */
	function mouse_down_handler(e) { 
	    if (OpenLayers.Event.isLeftClick(e)){
		/* We've not been dragged yet but the left buton was clicked */
		this_map_cont.phase_down = true;
		this_map_cont.is_dragged = false;
	    }
	}
	this.map.events.register('mousedown', this, mouse_down_handler);
       

	this.map.div.onmousemove = function(e){
	    if (this_map_cont.phase_down) {
		/* the mouse move while the button was held down. */
		this_map_cont.is_dragged = true;
	    }
	}

	this.map.div.onclick = function(e){
	    if (OpenLayers.Event.isLeftClick(e)){
		if (!this_map_cont.is_dragged) {
		    /* if the click cam after a drag don't clear popup */
		    this_map_cont.clear_popups();
		}

		/* remember, mouse button is up */
	    	this_map_cont.phase_down = false;
	    	this_map_cont.is_dragged = false;
	    }

	}

	/* Handle category changes */
	var cats = document.getElementById(this.category_html_id);
	function node_data_cb(node_data) {
	    this_map_cont.node_data = node_data;	    

	    /* Zoom to bounds causes a zoom event, nodes are put in the
	     * map from there.
	     */
	    this_map_cont.zoom_to_bounds(node_data);
	}

	function category_change() {
	    /* clear refresh time so it doesn't go in the middle of this
	     * request.
	     */
	    this_map_cont.clear_refresh_timer();
	    this_map_cont.category = this.options[this.selectedIndex].value;
	    this_map_cont._get_node_data(node_data_cb);
	    this_map_cont.set_last_update();
	}

	cats.addEventListener("change",category_change,false);

    }

    
    /* Mappings for serverity of alarms. See:
     *
     *./opennms-qosdaemon/src/main/java/org/openoss/opennms/...
     *    .../spring/dao/OnmsAlarmOssjMapper.java
     *
     * public static final int INDETERMINATE_SEVERITY = 1;
     * public static final int CLEARED_SEVERITY = 2;
     * public static final int NORMAL_SEVERITY = 3;
     * public static final int WARNING_SEVERITY = 4;
     * public static final int MINOR_SEVERITY = 5;
     * public static final int MAJOR_SEVERITY = 6;
     * public static final int CRITICAL_SEVERITY = 7;
     */
    onms.vs.NodeMapController.prototype.severity_mapping = [];
    onms.vs.NodeMapController.prototype.severity_mapping[0] = "Normal";
    onms.vs.NodeMapController.prototype.severity_mapping[1] = "Indeterminate";
    onms.vs.NodeMapController.prototype.severity_mapping[2] = "Cleared";
    onms.vs.NodeMapController.prototype.severity_mapping[3] = "Normal";
    onms.vs.NodeMapController.prototype.severity_mapping[4] = "Warning";
    onms.vs.NodeMapController.prototype.severity_mapping[5] = "Minor";
    onms.vs.NodeMapController.prototype.severity_mapping[6] = "Major";
    onms.vs.NodeMapController.prototype.severity_mapping[7] = "Critical";

    /* 'constants' */
    onms.vs.NodeMapController.prototype.MODE_BASESTATIONS_ONLY = 0;
    onms.vs.NodeMapController.prototype.MODE_SHOW_ALL_NODES    = 1;

    onms.vs.NodeMapController.prototype.clear_popups = function(){ 	
	for(var i=0; i < this.map.popups.length; i++) {
	    this.map.popups[i].related_node.clear_highlight();
	    this.map.removePopup(this.map.popups[i]);
	    this.markers_layer.selectedFeature = null;

	}
    }

    onms.vs.NodeMapController.prototype.map_options = { 
	restrictedExtent: new OpenLayers.Bounds(-20037508.34,
						-20037508.34,
						20037508.34,
						20037508.34),
	sphericalMercator: true,
	maxExtent: new OpenLayers.Bounds(-20037508.34,-20037508.34,
					 20037508.34,20037508.34),
	units: 'm',
	maxResolution: 78271.516950000005,
	projection: "EPSG:900913",	

	controls : [
		    new OpenLayers.Control.MouseDefaults(),
		    new OpenLayers.Control.ArgParser(),
		    new OpenLayers.Control.PanZoom()
		    ],

    };


    onms.vs.NodeMapController.prototype.set_refresh_timer = function(ms) {
	/* Just in case... */
	this.clear_refresh_timer();

	var this_map_cont = this;	
	function node_data_cb(node_data) {
	    this_map_cont.node_data = node_data;
	    this_map_cont.set_last_update();
	    this_map_cont.clear_popups(); 
	    this_map_cont.refresh();
	}

	function on_refresh() {
	    this_map_cont._get_node_data(node_data_cb);
	}
	
	this.refresh_timer = setTimeout(on_refresh, ms);
    }

    onms.vs.NodeMapController.prototype.clear_refresh_timer = function(ms) {
	clearTimeout(this.refresh_timer);
    }

	
    onms.vs.NodeMapController.prototype.refresh = function() {
	var j;
	
	/* Remove the markers... */
	if (this.nms) {
	    for (j = 0; j < this.nms.length; j++) {
		this.nms[j].remove_from_map(this.markers_layer, true);
	    }
	}

	this.update_table();
	this.draw_map();
    }




    onms.vs.NodeMapController.prototype.draw_map = function() {
	var report_cpe = ((this.mode == this.MODE_BASESTATIONS_ONLY)? 
	                  true : false);
	var i, j, k;

	/* clear previous nodes */
	this.nms = [];
	var nms = this.nms;


	for (i = 0; i < this.node_data.length; i++) {
	    /* Clustering is performed here... */

	    var added = false;
	    var pos   = onms.vs.utils.projection_lonlat(this.map, 
							this.node_data[i]);

	    if (true || this.current_extent.containsLonLat(pos)) {
		for (j = 0; j < nms.length; j++) {
		    if (nms[j].check_proximity(this.node_data[i],
					       this.cluster_threshold )) {
			nms[j].add(this.node_data[i]);
			added = true;

			break;
		    }
		}
		
		if (!added) {
		    nms.push(new onms.vs.NodeMapMarker(this.map, this.node_data[i]));
		}
	    }


	    /* Child nodes, if apropriate */
	    if (this.mode == this.MODE_SHOW_ALL_NODES) {

		/*cpes actually includes any aps too */
		var cpes = this.node_data[i].cpes;

		if (cpes) { 
		    for (j = 0; j < cpes.length; j++) {
			for (k = 0; k < nms.length; k++) {
			    if (nms[k].check_proximity(cpes[j], 
						       this.cluster_threshold )) {
				nms[k].add(cpes[j]);
				added = true;
				break;
			    }
			}
			
			if (!added) {
			    nms.push(new onms.vs.NodeMapMarker(this.map, cpes[j]));
			}
		    }
		}
	    }
	    
	}

	for (j = 0; j < nms.length; j++) {
	    nms[j].add_to_map(this.markers_layer, true);
	}

	/* Refresh in */
	this.set_refresh_timer(this.refresh_time);
    }


    /* Create table element dom nodes. */
    onms.vs.NodeMapController.prototype.create_table_element = function(node) {	
	var elm = document.createElement('tr');
	var node_elm = document.createElement('td');
	var alarm_elm = document.createElement('td');
	var alarm_link = document.createElement('a');
	var node_link = document.createElement('a');
	var node_text = document.createTextNode(node.nodeLabel);
	var alarm_text = document.createTextNode("view alarms");
	var text = document.createTextNode("");

	node_link.href = "/opennms/element/node.jsp?node=" + node.nodeId;
	node_link.name = node.nodeId;
	alarm_link.href = "/opennms/alarm/list.htm?filter=node%3d" + node.nodeId;
	
	node_elm.id = "tb_node_"+node.nodeId;

	if (node.severity == 0) {
	    if (node.avail < 100.0) {
		elm.className = "Warning";
	    } else {
		elm.className = "Normal";
	    }
	} else {
	    elm.className = this.severity_mapping[node.severity];
	}

	node_elm.className = "divider";
	alarm_elm.className = "divider";

	alarm_link.appendChild(alarm_text);
	node_link.appendChild(node_text);
	node_elm.appendChild(node_link);
	alarm_elm.appendChild(alarm_link);

	elm.appendChild(node_elm);
	elm.appendChild(alarm_elm);

	return elm;

    }

    onms.vs.NodeMapController.prototype.update_table = function() {	
	var nodes = this.node_data;
	var table = document.getElementById('link_table');
	var table_ld = document.getElementById("table_ld");
	var i, j;

	table.innerHTML = "";
	
	for (i = 0; i < nodes.length; i++) {
	    var elm = this.create_table_element(nodes[i]);
	    table.appendChild(elm);

	    if (nodes[i].cpes) {
		for (j =0 ;j < nodes[i].cpes.length; j++) {
		    var elm = this.create_table_element(nodes[i].cpes[j]);
		    table.appendChild(elm);
		}
	    }
	}

    }

    onms.vs.NodeMapController.prototype.set_last_update = function() {
	var update_div = document.getElementById(this.update_id);
	var current_time = new Date();
	update_div.innerHTML = "Last Update:  " + current_time;
	

    }

    onms.vs.NodeMapController.prototype.zoom_to_bounds = function(nodes) {
	var i;
	var node_bounds = new OpenLayers.Bounds();
	var last_zoom = this.map.getZoom() ;

	for (i = 0; i < nodes.length; i++) {
	    /* need to project between coordinate systems */
	    node_bounds.extend(onms.vs.utils.projection_lonlat(this.map, 
							       nodes[i]));
	}
	
	if (nodes.length > 0) {
	    this.map.zoomToExtent(node_bounds);
	    if (last_zoom == this.map.getZoom()) {
		/* If we ddn't actually zoom, redraw here, otherwise
		 * redraw in the zoom event that occurs soon.
		 */
		this.clear_popups();
		this.refresh();
	    }
	} else {
	    /* No nodes to show, show the whole world map */
	    this.map.zoomToMaxExtent();
	}
    }

    onms.vs.NodeMapController.prototype._get_node_data = function(node_callback, category) {
	var i;
	var xmlHttp = getXmlHttp();
		
	xmlHttp.onreadystatechange=function()
        {
            if(xmlHttp.readyState==4 && xmlHttp.status == 200){
		try {
		    var node_data = eval("(" + xmlHttp.responseText + ")");
		    node_callback(node_data);
		} catch (e) {
		    alert("parse error:\n" + e)
		    onms.vs.utils.dumpProps(e);
		}
            }
        }
	
	xmlHttp.open("GET", this.node_request_url + "?cat=" + this.category, true);
	xmlHttp.send(null);

	return;
    }

    onms.vs.NodeMapController.prototype.handle_category_data = function() {
	var cat_data = this.cat_data;
	var cats = document.getElementById(this.category_html_id);
	var last_cat = undefined;
	var i;
	var elm;	

	for (i = 0; i < cat_data.length; i++) {
	    elm = document.createElement('option');
	    
	    elm.value = cat_data[i].id;
	    elm.text = cat_data[i].name;
	    
	    cats.add(elm, last_cat);
	    
	    last_cat = elm;
	}

	/* Plus two special options. */
	elm = document.createElement('option');
	elm.value = "ALL"
	elm.text = "All";
	cats.add(elm, last_cat);

	elm = document.createElement('option');
	elm.value = "UCAT"
	elm.text = "Uncategorised";
	cats.add(elm, last_cat);
	

    }

    /* load the categories from the database */
    onms.vs.NodeMapController.prototype._get_category_data = function(callback) {
	var i;
	var xmlHttp = getXmlHttp();
	
	xmlHttp.onreadystatechange=function()
        {
            if(xmlHttp.readyState==4 && xmlHttp.status == 200){
		try {
		    if (callback) {
			var cat_data = eval("(" + xmlHttp.responseText + ")");
			callback(cat_data);
		    }

		} catch (e) {
		    alert("parse error:\n" + e)
		    onms.vs.utils.dumpProps(e);
		}
            }
        }
	
	xmlHttp.open("GET", this.cats_request_url,  true);
	xmlHttp.send(null);

	return;
    }


    /* Kick things off at the start of time. */
    onms.vs.NodeMapController.prototype.run_map = function() {
	var this_map = this;
	function node_data_cb(node_data) {
	    this_map.node_data = node_data;
	    
	    /* Zoom to bounds causes a zoom event, nodes are put in the
	     * map from there.
	     */
	    this_map.zoom_to_bounds(node_data);
	    this_map.set_last_update(node_data);

	}

	function cat_data_cb(cat_data) {
	    this_map.cat_data = cat_data;
	    this_map.handle_category_data();
	}

	this._get_node_data(node_data_cb);
	this._get_category_data( cat_data_cb);
    }
}

function getXmlHttp()
{
    var xmlHttp;

    try{
        // Firefox, Opera 8.0+, Safari
        xmlHttp=new XMLHttpRequest();
    }
    catch (e){
        alert("no xmlHttpRequest");
    }

    return xmlHttp;
}




function osm_init(){  
    var map_controller = new onms.vs.NodeMapController(onms.vs.NodeMapOptions.NODE_REQUEST_URL,
						       onms.vs.NodeMapOptions.CAT_REQUEST_URL,
						       onms.vs.NodeMapOptions.MAP_ID,
						       onms.vs.NodeMapOptions.CAT_ID,
						       onms.vs.NodeMapOptions.TILECACHE_URL,
						       onms.vs.NodeMapOptions.UPDATE_ID,
						       onms.vs.NodeMapOptions.CLUSTER_THRESH);

    map_controller.run_map();

    
}
