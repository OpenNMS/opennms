package org.opennms.features.vaadin.nodemaps.gwt.client;

import org.opennms.features.vaadin.nodemaps.gwt.client.openlayers.FeatureCollection;
import org.opennms.features.vaadin.nodemaps.gwt.client.openlayers.OnmsOpenLayersMap;
import org.opennms.features.vaadin.nodemaps.gwt.client.openlayers.VectorLayer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.VConsole;

public class GWTOpenlayersWidget extends Widget {
	private final DivElement m_div;
	private OnmsOpenLayersMap m_map;
	private VectorLayer m_vectorLayer;
	private FeatureCollection m_features;

	public GWTOpenlayersWidget() {
		super();
		m_div = Document.get().createDivElement();
		m_div.setId("gwt-map");
		setElement(m_div);
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		Scheduler.get().scheduleDeferred(new Command() {
			public void execute() {
				createMap(m_div.getId());
			}
		});
	}

	@Override
	protected void onUnload() {
		destroyMap();
		super.onUnload();
	}

	private String getNodesGml() {
		return GWT.getModuleBaseURL() + "nodes.gml";
	}

	private void createMap(final String divId) {
		m_map = OnmsOpenLayersMap.newInstance(divId);
		initializeMap(m_map);
	}

	private final native void initializeMap(final OnmsOpenLayersMap map) /*-{
		var displayAllNodes = true;

		var nodeFillColors = {
			Critical: "#F5CDCD",
			Major: "#FFD7CD",
			Minor: "#FFEBCD",
			Warning: "#FFF5CD",
			Normal: "#D7E100" // was #D7E1CD
		};

		var nodeStrokeColors = {
			Critical: "#CC0000",
			Major: "#FF3300",
			Minor: "#FF9900",
			Warning: "#FFCC00",
			Normal: "#336600"
		};

        	var nodeStyles = new $wnd.OpenLayers.Style({
        		pointRadius: "${radius}",
        		graphicName: "${shape}",
        		label: "${label}",
        		display: "${display}",
        		fillColor: "${fillColor}",
        		fillOpacity: 0.8,
        		strokeColor: "${strokeColor}",
        		strokeOpacity: 0.8,
        		strokeWidth: 3,
        		fontFamily: "'Lucida Grande', Verdana, sans-serif",
        		fontSize: 10
        	}, {
        		context: {
        			// The Shape will change if the cluster contain several nodes or not.
        			shape: function(feature) {
        				return feature.cluster && feature.cluster.length > 1 ? "circle" : "square";
        			},
        			// The Radius will change according with the amount of nodes on the cluster.
        			radius: function(feature) {
        				return feature.cluster ? Math.min(feature.attributes.count, 7) + 5 : 5;
        			},
        			// The label will display the amount of nodes only for clusters.
        			label: function(feature) {
        				return feature.cluster && feature.cluster.length > 1 ? feature.cluster.length : "";
        			},
        			display: function(feature) {
        				if (displayAllNodes) {
        					return 'display';
        				}
        				// Display only nodes with availability < 100
        				return getHighestSeverity(feature) == 'Normal' ? 'none' : 'display';
        			},
        			// It depends on the calculated severity
        			strokeColor: function(feature) {
        				return nodeStrokeColors[getNodeSeverity(feature)];
        			},
        			// It depends on the calculated severity
        			fillColor: function(feature) {
        				return nodeFillColors[getNodeSeverity(feature)];
        			}
        		}
        	});

		// Nodes Layer

		var nodesLayer = new $wnd.OpenLayers.Layer.Vector("All Nodes", {
			strategies: [
				new $wnd.OpenLayers.Strategy.Cluster()
			],
			styleMap: new $wnd.OpenLayers.StyleMap({
				'default': nodeStyles,
				'select': {
					fillColor: "#8aeeef",
					strokeColor: "#32a8a9"
				}
			})
		});

                this.@org.opennms.features.vaadin.nodemaps.gwt.client.GWTOpenlayersWidget::m_vectorLayer = nodesLayer;
                this.@org.opennms.features.vaadin.nodemaps.gwt.client.GWTOpenlayersWidget::updateFeatureLayer()();

		// Selection Features

		var select = new $wnd.OpenLayers.Control.SelectFeature(
			nodesLayer, {hover: true}
		);
		map.addControl(select);
		select.activate();

		nodesLayer.events.on({
			'featureselected': onFeatureSelect,
			'featureunselected': onFeatureUnselect
		});
		map.addLayer(nodesLayer);

		map.setCenter(new $wnd.OpenLayers.LonLat(0, 0), 1);

		function getAvailability(feature) {
			if (!feature.cluster) return 100;
			var count = 0;
			for (var i=0; i<feature.cluster.length; i++) {
				var n = feature.cluster[i].attributes;
				if (n.nodeStatus == 'Down') count++;
			}
			return ((1 - count/feature.cluster.length) * 100).toFixed(2);
		}

		function getNodeSeverity(feature) {
		    return getHighestSeverity(feature);
		}

		function onPopupClose(evt) {
			select.unselect(this.feature);
		}

                function onFeatureSelect(evt) {
                    feature = evt.feature;
                    var msg = "";
                    if (feature.cluster.length > 1) {
                        var nodes = [];
                        for (var i=0; i<feature.cluster.length; i++) {
                            var n = feature.cluster[i].attributes;
                            nodes.push(n.nodeLabel + "(" + n.ipAddress + ") : " + n.nodeStatus);
                        }
                        msg = "<h2># of nodes: " + feature.cluster.length + " (" + getNumUnacked(feature) + " Unacknowledged Alarms)</h2><ul><li>" + nodes.join("</li><li>") + "</li></ul>";
                    } else {
                        var n = feature.cluster[0].attributes;
                        msg = "<h2>Node " + n.nodeLabel + "</h2>" +
                              "<p>Node ID: " + n.nodeId + "</br>" +
                              "Foreign Source: " + n.foreignSource + "</br>" +
                              "Foreign ID: " + n.foreignId + "</br>" +
                              "IP Address: " + n.ipAddress + "</br>" +
                              "Status: " + n.nodeStatus + "</br></p>";
                    }
                    popup = new $wnd.OpenLayers.Popup.FramedCloud("nodePopup",
                        feature.geometry.getBounds().getCenterLonLat(),
                        new $wnd.OpenLayers.Size(100,100), msg, null, false, onPopupClose);
                    feature.popup = popup;
                    popup.feature = feature;
                    map.addPopup(popup);
                }

                function getHighestSeverity(feature) {
                    if (!feature.cluster) return "Normal";
                    var severity = 0;
                    var severityLabel = "Normal";
                    for (var i=0; i<feature.cluster.length; i++) {
                        var n = feature.cluster[i].attributes;
                        if (n.severity && n.severity > severity) {
                            severity = n.severity;
                            severityLabel = n.severityLabel;
                        }
                        if (severity == 7) {
                            break;
                        }
                    }
                }

                function getNumUnacked(feature) {
                        if (!feature.cluster) return 0;
                        var count = 0;
                        for (var i=0; i<feature.cluster.length; i++) {
                                var n = feature.cluster[i].attributes;
                                if (n.unackedCount) count += n.unackedCount;
                        }
                        return count;
                }

		function onFeatureUnselect(evt) {
			feature = evt.feature;
			if (feature.popup) {
				popup.feature = null;
				map.removePopup(feature.popup);
				feature.popup.destroy();
				feature.popup = null;
			}
		}

		function applyFilters(btn) {
			btn.value = displayAllNodes ? 'Show All Nodes' : 'Show Down Nodes';
			displayAllNodes = !displayAllNodes; 
			nodesLayer.redraw();
		}
	}-*/;

	public void updateFeatureLayer() {
	    if (m_features == null) {
	        VConsole.log("features not initialized yet, skipping update");
	        return;
	    }
	    if (m_vectorLayer == null) {
	        VConsole.log("vector layer not initialized yet, skipping update");
	        return;
	    }
	    VConsole.log("adding features to the node layer");
            m_vectorLayer.replaceFeatureCollection(m_features);
            VConsole.log("finished adding features");
	}

	public FeatureCollection getFeatureCollection() {
	    return m_features;
	}

	public void setFeatureCollection(final FeatureCollection collection) {
            m_features = collection;
	}

	private final native void destroyMap() /*-{
		map.destroy();
	}-*/;

}
