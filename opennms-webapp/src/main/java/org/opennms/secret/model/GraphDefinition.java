package org.opennms.secret.model;

import java.util.LinkedList;

public class GraphDefinition {

	//name of graph
	//date/time interval
	//data source object - rdd file name
	//ds name
	//say to data source - give me the rdd file / give me the
	
	//line properties.colour
	//line properties.weight
	//line properties
	
	// graphdata
	
	String graphTitle; //name of graph
	long startTime, endTime; // epoc time interval start and end
	LinkedList graphDataElements; // data source objects for the graph 
	                     // name of data, file, rrd dsname

	public GraphDefinition(){
		graphDataElements= new LinkedList();
		setGraphTitle("");
		setEndTime(System.currentTimeMillis());
		setStartTime(getEndTime() - 86400000);
	}
	
	public void addGraphDataElement(GraphDataElement ds){
		graphDataElements.add(ds);
	}
	
	public void removeGraphDataElement(GraphDataElement ds){
		graphDataElements.remove(ds);
	}
	
	public LinkedList getGraphDataElements() {
		return graphDataElements;
	}
	
	public void setGraphDataElements(LinkedList graphDataElements) {
		this.graphDataElements = graphDataElements;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public String getGraphTitle() {
		return graphTitle;
	}

	public void setGraphTitle(String graphTitle) {
		this.graphTitle = graphTitle;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
}
