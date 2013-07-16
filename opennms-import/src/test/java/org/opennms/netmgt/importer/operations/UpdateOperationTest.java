/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.importer.operations;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.importer.config.types.InterfaceSnmpPrimaryType;

public class UpdateOperationTest extends TestCase {

        @Override
	protected void setUp() throws Exception {
		super.setUp();
	}

        @Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'org.opennms.netmgt.importer.operations.AbstractSaveOrUpdateOperation.scanForSnmpData(Node)'
	 */
	public final void testScanForSnmpData() throws MarshalException, ValidationException, IOException {
		
		ByteArrayInputStream in = new ByteArrayInputStream(("<?xml version=\"1.0\"?>\n" + 
				"<snmp-config port=\"161\" retry=\"3\" timeout=\"800\"\n" + 
				"             read-community=\"public\" \n" + 
				"                 version=\"v1\">\n" + 
				"\n" + 
				"</snmp-config>").getBytes());
		
		SnmpPeerFactory.setInstance(new SnmpPeerFactory(in));
		
		
		AbstractSaveOrUpdateOperation op = new UpdateOperation(new Integer(1), "imported:", "1", "node1", "theoffice", "pittsboro");
		op.foundInterface("192.168.0.102", "if1", InterfaceSnmpPrimaryType.P, true, 1);
		op.foundInterface("127.0.0.1", "if2", InterfaceSnmpPrimaryType.N, true, 1);
		op.updateSnmpData();
		

	}

}
