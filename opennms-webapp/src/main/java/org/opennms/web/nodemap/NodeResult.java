
package org.opennms.web.nodemap;


public class NodeResult {

    private int nodeId;
    private int parentNodeId;
    private int type;
    private String nodeLabel;
    private String foreignId;
    private double geolocationLat;
    private double geolocationLon;
    private double avail;

    public static final int NODE_TYPE_GENERAL     = 0;
    public static final int NODE_TYPE_CPE         = 1;
    public static final int NODE_TYPE_BASESTATION = 2;


    //    select nodeid, eventuei,severity from alarms where nodeid is not null and severity > 3 order by nodeid, lasteventtime desc;
    private String eventuei;
    private int severity;

    public void setEventUei(String eventuei) {
	this.eventuei = eventuei;
    }

    public void setForeignId(String fid) {
	this.foreignId = fid;
    }

    public String getEventUei() {
	return eventuei;
    }

    public String getForeignId() {
	return foreignId;
    }

    public void setSeverity(int severity) {
	this.severity = severity;
    }

    public int getSeverity() {
	return severity;
    }

    public void setAvail(double avail) {
	this.avail = avail;
    }

    public double getAvail() {
	return avail;
    }

    public int getType() {
	return type;
    }
    
    public void setNodeId(int nodeId) {
	this.nodeId = nodeId;
    }    

    public void setType(int type) {
	this.type = type;
    }
    
    public int getNodeId() {
	return nodeId;
    }

    public void setParentNodeId(int parentNodeId) {
	this.parentNodeId = parentNodeId;
    }    

    public int getParentNodeId() {
	return parentNodeId;
    }

    public void setNodeLabel(String nodeLabel) {
	this.nodeLabel = nodeLabel;
    }

    public String getNodeLabel() {
	return nodeLabel;
    }

    public void setGeolocationLat(double lat) {
	this.geolocationLat = lat;
    }

    public double getGeolocationLat() {
	return geolocationLat;
    }

    public void setGeolocationLon(double lon) {
	this.geolocationLon = lon;
    }

    public double getGeolocationLon() {
	return geolocationLon;
    }

    public String toJson() {
	String out = "{ \"nodeId\" : " + nodeId + ", " +
	    "\"type\" : " + type + ", " +
	    "\"parentnodeid\" : \"" + parentNodeId + "\", " +
	    "\"nodeLabel\" : \"" + nodeLabel + "\", " +
	    "\"avail\" : \"" + avail + "\", " +
	    "\"latitude\" : " + geolocationLat + ", " +
	    "\"longitude\" : " + geolocationLon + ", " +
	    "\"eventuei\" : \"" + eventuei + "\", " + 
	    "\"foreignId\" : \"" + foreignId + "\", " + 
	    "\"severity\" : " + severity + "}";
	    
	return out;
    }

}