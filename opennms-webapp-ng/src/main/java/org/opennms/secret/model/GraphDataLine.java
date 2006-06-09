package org.opennms.secret.model;

import java.awt.Color;


/**
 * @author mhuot cgallen
 * Contains the description of the data to be displayed on a graph
 *  Adds line plot to the graph definition, using the specified color, legend and line width. This method takes exactly the same parameters as RRDTool's LINE directive. The legend allows for the same alignment options as gprint or comment.
 
 Parameters:
 sourceName - Graph source name.
 color - Line color to be used.
 legend - Legend to be printed on the graph.
 lineWidth - Width of the line in pixels.
 
 */

public class GraphDataLine extends GraphDataElement {
	
	Color color;
	int lineWidth;
	
	public  GraphDataLine (DataSource ds, Color color, int lineWidth)
	{
		this.dataSource=ds;
		this.color= color;
		this.lineWidth=lineWidth;
	}
	
	public GraphDataLine (DataSource ds) {
		this(ds, new Color(0, 0, 0), 2);
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(java.awt.Color color) {
		this.color = color;
	}
	
	public java.lang.String getLegend() {
		return legend;
	}
	
	public void setLegend(java.lang.String legend) {
		this.legend = legend;
	}
	
	public int getLineWidth() {
		return lineWidth;
	}
	
	public void setLineWidth(int lineWidth) {
		this.lineWidth = lineWidth;
	}
	
	
	
}

