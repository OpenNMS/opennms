var onms;
if (!onms) onms = {};
if (!onms.vs) onms.vs = {};

if (!onms.vs.NodeMapMarker) {
    /* A node map marked represents the nodes that will show as
     * one marker on the map.
     */


    onms.vs.NodeMapMarker = function(map, node) {
	this.nodes = [];
	this.map = map;	

	/* we keep count of the numbers of each type */
	this.n_cpes = 0;
	this.n_bs = 0;
	this.n_ap = 0;

	/* Catch any nodes that arn't either CPE's AP's or BS's*/
	this.n_general = 0;

	/* Add the first node */
	this.add(node);
    };
    
    /* The type of popup to use. */
    onms.vs.NodeMapMarker.prototype.NodeInfoBubble = OpenLayers.Class(OpenLayers.Popup.FramedCloud, {
            'autoSize': true
        });

    
    onms.vs.NodeMapMarker.prototype.is_node_cpe = function(node) {
	return node.foreignId.match(/CPE.*/);
    }

    onms.vs.NodeMapMarker.prototype.is_node_ap = function(node) {
	return node.foreignId.match(/ODU.*/);
    }


    onms.vs.NodeMapMarker.prototype.is_node_bs = function(node) {
	return node.foreignId.match(/APC.*/);
    }
     
    onms.vs.NodeMapMarker.prototype.add = function(node) {
	this.nodes.push(node);

	/* track numbers of each type */
	if (this.is_node_cpe(node)) {
	    this.n_cpes++;
	} else if (this.is_node_bs(node)) {
	    this.n_bs++;
	} else if (this.is_node_ap(node)) {
	    this.n_ap++;
	} else {
	    this.n_general++;
	}
    }


    /* coords need projecting between different coordinate systems */
    onms.vs.NodeMapMarker.prototype.projection_lonlat = function(node) {
	return onms.vs.utils.projection_lonlat(this.map, node)
    };

    onms.vs.NodeMapMarker.prototype.check_proximity = function(node, radius) {	
	if (!this.nodes[0].projected_lonlat) {
	    this.nodes[0].projected_lonlat = this.projection_lonlat(this.nodes[0]);
	}

	var pll = node.projected_lonlat = this.projection_lonlat(node);
	var pix1 = this.map.getPixelFromLonLat( this.nodes[0].projected_lonlat);
	var pix2 = this.map.getPixelFromLonLat(pll);

	var d = Math.sqrt(((pix1.x - pix2.x) * (pix1.x - pix2.x)) + ((pix1.y - pix2.y) * (pix1.y - pix2.y)));

	if (d < radius) {
	    return true;
	}
    }

    onms.vs.NodeMapMarker.prototype.count_children_with_alarms = function() {
	var i = 0;
	var node = this.nodes[0];
	var alarm_count = 0;

	if (node.cpes) {
	    for (i = 0; i < node.cpes.length; i++) {
		if (node.cpes[i].severity != 0) {
		    alarm_count++;
		}
	    }
	}

	return alarm_count;
    }
    
    onms.vs.NodeMapMarker.prototype.NODE_DOWN_ICON = onms.vs.NodeMapOptions.OPENLAYERS_BASE + "/img/marker.png";
    onms.vs.NodeMapMarker.prototype.NODE_UP_ICON = onms.vs.NodeMapOptions.OPENLAYERS_BASE + "/img/marker-green.png";
    onms.vs.NodeMapMarker.prototype.NODE_WARNING_ICON = onms.vs.NodeMapOptions.OPENLAYERS_BASE + "/img/marker-gold.png";

    onms.vs.NodeMapMarker.prototype.get_icon_img_path = function() {
	var icon;

	if (this.nodes.length > 0 ) {
	    var node = this.nodes[0];
	    if (node) {
		if (this.is_node_bs(node)) {
		    if (node.severity == 0) {
			if (this.count_children_with_alarms() > 0) {
			    icon = this.NODE_WARNING_ICON;
			} else {
			    icon = this.NODE_DOWN_ICON;
			}
		    } else {
			icon = this.NODE_DOWN_ICON;
		}
		} else {
		    /* is there an alarm? */
		    if (node.severity == 0) {
			icon = this.NODE_UP_ICON;
		    } else {
			/* yes.. show alarm with down icon */
			icon = this.NODE_DOWN_ICON;
		    }
		}
	    }	
	    
	} else {
	    var alarms = this.count_alarms();
	    if (alarms > 0) {
		icon = this.NODE_DOWN_ICON;
	    } else {
		icon = this.NODE_UP_ICON;
	    }
	}

	return icon;
    }

    /* generate the html to go in the popups */
    onms.vs.NodeMapMarker.prototype.get_bs_summary = function(index, report_cpe) {
	var node = this.nodes[index];
	var out = this.get_general_summary(index);
	
	
	if (node.cpes && node.cpes.length > 0) {
	    var n_alarms = this.count_children_with_alarms();

	    if (n_alarms > 0) {
		out += ("<div style=\"color: #aa0000;\">CPE/AP Status: "  + n_alarms
			+ " of " +  node.cpes.length
			+ " alarms</div>" );
	    } else {
		out += ("<div style=\"color: #00aa00;\">CPE/AP Status: " 
			+ " all " +  node.cpes.length + " CPE/APs OK</div>" );
	    }
	}

	return out;
	
    }


    onms.vs.NodeMapMarker.prototype.get_general_summary = function(index) {
	var node = this.nodes[index];
	var out = "<div>";

	if (this.is_node_bs(this.nodes[index])) {
	    out += "Basestation Summary " + node.nodeLabel + " ";
	} else if (this.is_node_ap(this.nodes[index])) {
	    out += "AP Summary " + node.nodeLabel + " ";
	} else if (this.is_node_cpe(this.nodes[index])) {
	    out += "CPE Summary " + node.nodeLabel + " ";
	} else {
	    out += "General Summary " + node.nodeLabel + " ";
	}


	out += "(" + node.nodeId  + ")";
	out += "</div>";

	if (node.avail != 100.0) {
	    out += ("<div style=\"color: #aa0000;\">Availability: " 
		    + node.avail + "%</div>" );
	} else {
	    out += ("<div style=\"color: #00aa00;\">Availability: " 
		    + node.avail + "%</div>" );
	}
	
	

	return out;
    }

    onms.vs.NodeMapMarker.prototype.count_alarms = function() {
	var i;
	var count = 0;

	for (i = 0; i < this.nodes.length; i++) {
	    if (this.nodes[i].severity > 0) {
		count++;
	    }
	}

	return count;
    }

    onms.vs.NodeMapMarker.prototype.get_summary = function(report_cpe) {
	/* Return HTML to go in popup box. */

	if (this.nodes.length == 1) {
	    /* Single node */

	    if (this.is_node_bs(this.nodes[0])) {
		/* Base station */
		return this.get_bs_summary(0, report_cpe);
	    } else {
		return this.get_general_summary(0);
	    } 
	    
	}

	var out_html = "";
	
	var node = this.nodes[0];
	var count_alarms = this.count_alarms();

	out_html += ("<div class=\"node_label\">Clustered Nodes</div><br/>");

	if (this.n_bs > 0) {
	    out_html += ("<div class=\"n_base\">Number of Basestations " + this.n_bs + "</div>" );
	}

	if (this.n_cpes > 0) {
	    out_html += ("<div class=\"n_base\">Number of CPEs " + this.n_cpes + "</div>" );
	}

	if (this.n_ap > 0) {
	    out_html += ("<div class=\"n_base\">Number of APs " + this.n_ap + "</div>" );
	}

	if (this.n_general > 0) {
	    out_html += ("<div class=\"n_base\">Number of Nodes " + this.n_general + "</div>" );
	}

	if (count_alarms > 0) {
	    out_html += ("<div style=\"color: #aa0000\">" + count_alarms + " nodes with alarms.</div>" );
	} else {
	    out_html += ("<div style=\"color: #00aa00\"> All Nodes Up.</div>" );
	}

	return out_html;
    }
        
    onms.vs.NodeMapMarker.prototype.clear_highlight = function() {
	this.set_table_class("divider");
    }

    onms.vs.NodeMapMarker.prototype.highlight = function() {
	this.set_table_class("divider bright");
	this.view();
    }

    /* make sure at least one node is on view in the table */
    onms.vs.NodeMapMarker.prototype.view = function() {
	var elm = document.getElementById("tb_node_"+this.nodes[0].nodeId);	
	elm.scrollIntoView(true);
    }

    onms.vs.NodeMapMarker.prototype.set_table_class = function(classes) {
	var i;

	for (i = 0; i < this.nodes.length; i++) {
	    var elm = document.getElementById("tb_node_" + this.nodes[i].nodeId);	
	    elm.className = classes;
	}
    }

    onms.vs.NodeMapMarker.prototype.remove_from_map = function(markers_layer, report_cpe) {
	markers_layer.removeMarker(this.marker);
    }

    /* create the openlayers markers and event handling for popups */
    onms.vs.NodeMapMarker.prototype.add_to_map = function(markers_layer, report_cpe) {
	var node       = this.nodes[0];
	var popup_html = this.get_summary(report_cpe);
	var location   = (this.nodes[0].projected_lonlat 
			  || this.projection_lonlat(node));

	var icon_img = this.get_icon_img_path(node);

	var feature = new OpenLayers.Feature(markers_layer.markers, location); 
	feature.closeBox = false;
	feature.popupClass = this.NodeInfoBubble;
	feature.data.popupContentHTML = popup_html;
	feature.data.overflow = "auto";
	feature.size = new OpenLayers.Size(21,25);
	feature.data.icon = new OpenLayers.Icon(icon_img);
	feature.data.icon.size = new OpenLayers.Size(21,25);
	feature.data.icon.offset = new OpenLayers.Pixel(-(feature.data.icon.size.w/2), 
						    -feature.data.icon.size.h);

	var marker = feature.createMarker();
	
	var this_node = this;
	var markerClick = function (evt) {
	    {
		var sameMarkerClicked = (this == markers_layer.selectedFeature);
		markers_layer.selectedFeature = (!sameMarkerClicked) ? this : null;
		
		/* remove the popups already showing */
		for(var i=0; i < this_node.map.popups.length; i++) {
		    this_node.map.popups[i].related_node.clear_highlight();		    
		    this_node.map.removePopup(this_node.map.popups[i]);
		}

		if (!sameMarkerClicked) {
		    /* then we need a new one... to replace the one we closed
		     */
		    var pop = this.createPopup(this.closeBox)

		    pop.related_node = this_node;
		    this_node.highlight();
		    this_node.map.addPopup(pop);
		}

	    }
	
	    OpenLayers.Event.stop(evt);
	    return false;
	};
	marker.events.register("click", feature, markerClick);
	this.marker = marker;

	markers_layer.addMarker(marker);
    }
};
