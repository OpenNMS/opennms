package org.opennms.secret.model;


/**
 * @author mhuot cgallen
 * Contains the description of the data to be displayed on a graph
 */
public class GraphDataElement {
	
	private static int ID=0;
	
	String uniqueID;
	DataSource dataSource; // data source objects for the graph 
                           // name of data, file, rrd dsname
	
	String legend; // legend for data element
	
	public String getUniqueID() {
		return uniqueID;
	}

	public GraphDataElement() {
		ID++;
	    uniqueID= "GraphDataElement_"+ ID;
	    legend = "legend";
	}

	public String getLegend() {
		return legend;
	}

	public void setLegend(java.lang.String legend) {
		this.legend = legend;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

}
