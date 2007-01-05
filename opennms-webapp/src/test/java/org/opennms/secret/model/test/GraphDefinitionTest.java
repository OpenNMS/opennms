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
package org.opennms.secret.model.test;

import junit.framework.TestCase;

import org.opennms.secret.model.DataSource;
import org.opennms.secret.model.GraphDataLine;
import org.opennms.secret.model.GraphDefinition;

public class GraphDefinitionTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testGraphDataElement() throws Exception {
		DataSource ds = new DataSource();
		ds.setDataSource("test_graphDataLine");
		ds.setId("test_graphDataLine");
		ds.setName("test name");
		ds.setSource("/root/test/rrd");		
		GraphDataLine gdl_1 = new GraphDataLine(ds);	
		GraphDataLine gdl_2 = new GraphDataLine(ds);	
		assertFalse(gdl_1.getUniqueID().equals(gdl_2.getUniqueID()));
		
	}
		
		

	public void testCreate() throws Exception {
		// populate Graph Definition
		DataSource ds = new DataSource();
		ds.setDataSource("test");
		ds.setId("test");
		ds.setName("test name");
		ds.setSource("/root/test/rrd");		
		GraphDataLine gdl = new GraphDataLine(ds);	
		GraphDefinition gdef = new GraphDefinition();
		gdef.addGraphDataElement(gdl);
		
		
	}
}
