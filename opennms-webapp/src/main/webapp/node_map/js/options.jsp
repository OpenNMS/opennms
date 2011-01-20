<%@ page contentType="application/javascript" language="java" %>

var onms;
if (!onms) onms = {};
if (!onms.vs) onms.vs = {};

if (!onms.vs.NodeMapOptions) {
    onms.vs.NodeMapOptions = {};
    onms.vs.NodeMapOptions.NODE_REQUEST_URL = "/opennms/nodemap/getMapNodes";
//    onms.vs.NodeMapOptions.NODE_REQUEST_URL = "/opennms/node_map/js/test.json";
    onms.vs.NodeMapOptions.CAT_REQUEST_URL = "/opennms/nodemap/getMapViewCategories";
    onms.vs.NodeMapOptions.MAP_ID = "map";
    onms.vs.NodeMapOptions.CAT_ID = "categories";
    onms.vs.NodeMapOptions.UPDATE_ID = "update_time";
    onms.vs.NodeMapOptions.CLUSTER_THRESH = 20;
    <% out.println("onms.vs.NodeMapOptions.TILECACHE_URL=\"" + System.getProperty("com.cbnl.onms.tilecache_location") + "/tilecache.cgi?\";"); %>
    <% out.println("onms.vs.NodeMapOptions.OPENLAYERS_BASE=\"" + System.getProperty("com.cbnl.onms.openlayers_location") + "\";"); %>


}
