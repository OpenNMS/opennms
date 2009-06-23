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
package org.opennms.netmgt.importer.operations;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import junit.framework.TestCase;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.SnmpPeerFactory;

public class UpdateOperationTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'org.opennms.netmgt.importer.operations.AbstractSaveOrUpdateOperation.scanForSnmpData(Node)'
	 */
	@SuppressWarnings("deprecation")
    public final void testScanForSnmpData() throws MarshalException, ValidationException, IOException {
		
		Reader rdr = new StringReader("<?xml version=\"1.0\"?>\n" + 
				"<snmp-config port=\"161\" retry=\"3\" timeout=\"800\"\n" + 
				"             read-community=\"public\" \n" + 
				"                 version=\"v1\">\n" + 
				"\n" + 
				"</snmp-config>");
		
		SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
		
		
		AbstractSaveOrUpdateOperation op = new UpdateOperation(new Integer(1), "imported:", "1", "node1", "theoffice", "pittsboro");		
		op.foundInterface("192.168.0.102", "if1", "P", true, 1);
		op.foundInterface("127.0.0.1", "if2", "N", true, 1);
		op.updateSnmpData();
		

	}

}
