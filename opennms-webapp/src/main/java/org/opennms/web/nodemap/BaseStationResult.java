package org.opennms.web.nodemap;

import java.util.List;
import java.util.ArrayList;

public class BaseStationResult extends NodeResult {

    private List<NodeResult> cpes;

    BaseStationResult() {
	cpes = new ArrayList<NodeResult>();
    }

    public void addCpe(NodeResult cpe) {
	cpes.add(cpe);
    }


    public String toJson() {
	String out = "{ \"nodeId\" : " + getNodeId() + ", " +
	    "\"nodeLabel\" : \"" + getNodeLabel() + "\", " +
	    "\"avail\" : \"" + getAvail() + "\", " +
	    "\"latitude\" : " + getGeolocationLat() + ", " +
	    "\"longitude\" : " + getGeolocationLon() + ", " + 
	    "\"eventuei\" : \"" + getEventUei() + "\", " + 
	    "\"foreignId\" : \"" + getForeignId() + "\", " + 
	    "\"severity\" : " + getSeverity() + ", " + 
	    "\"cpes\" : [\n"; 
	    

	for (NodeResult cpe : cpes) {
	    out += "\t" + cpe.toJson() + ",\n";
	}

	out += "]}\n";
	    
	return out;
    }

}
