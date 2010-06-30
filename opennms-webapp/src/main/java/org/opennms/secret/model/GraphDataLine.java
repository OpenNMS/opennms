//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.secret.model;

import java.awt.Color;


/**
 * <p>GraphDataLine class.</p>
 *
 * @author mhuot cgallen
 * Contains the description of the data to be displayed on a graph
 *  Adds line plot to the graph definition, using the specified color, legend and line width. This method takes exactly the same parameters as RRDTool's LINE directive. The legend allows for the same alignment options as gprint or comment.
 *
 * Parameters:
 * sourceName - Graph source name.
 * color - Line color to be used.
 * legend - Legend to be printed on the graph.
 * lineWidth - Width of the line in pixels.
 * @version $Id: $
 * @since 1.6.12
 */
public class GraphDataLine extends GraphDataElement {
	
	Color color;
	int lineWidth;
	
	/**
	 * <p>Constructor for GraphDataLine.</p>
	 *
	 * @param ds a {@link org.opennms.secret.model.DataSource} object.
	 * @param color a {@link java.awt.Color} object.
	 * @param lineWidth a int.
	 */
	public  GraphDataLine (DataSource ds, Color color, int lineWidth)
	{
		this.dataSource=ds;
		this.color= color;
		this.lineWidth=lineWidth;
	}
	
	/**
	 * <p>Constructor for GraphDataLine.</p>
	 *
	 * @param ds a {@link org.opennms.secret.model.DataSource} object.
	 */
	public GraphDataLine (DataSource ds) {
		this(ds, new Color(0, 0, 0), 2);
	}
	
	/**
	 * <p>Getter for the field <code>color</code>.</p>
	 *
	 * @return a {@link java.awt.Color} object.
	 */
	public Color getColor() {
		return color;
	}
	
	/**
	 * <p>Setter for the field <code>color</code>.</p>
	 *
	 * @param color a java$awt$Color object.
	 */
	public void setColor(java.awt.Color color) {
		this.color = color;
	}
	
	/**
	 * <p>getLegend</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public java.lang.String getLegend() {
		return legend;
	}
	
	/** {@inheritDoc} */
	public void setLegend(java.lang.String legend) {
		this.legend = legend;
	}
	
	/**
	 * <p>Getter for the field <code>lineWidth</code>.</p>
	 *
	 * @return a int.
	 */
	public int getLineWidth() {
		return lineWidth;
	}
	
	/**
	 * <p>Setter for the field <code>lineWidth</code>.</p>
	 *
	 * @param lineWidth a int.
	 */
	public void setLineWidth(int lineWidth) {
		this.lineWidth = lineWidth;
	}
	
	
	
}

