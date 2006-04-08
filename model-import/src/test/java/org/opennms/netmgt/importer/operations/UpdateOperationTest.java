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
	public final void testScanForSnmpData() throws MarshalException, ValidationException, IOException {
		
		Reader rdr = new StringReader("<?xml version=\"1.0\"?>\n" + 
				"<snmp-config port=\"161\" retry=\"3\" timeout=\"800\"\n" + 
				"             read-community=\"public\" \n" + 
				"                 version=\"v1\">\n" + 
				"\n" + 
				"</snmp-config>");
		
		SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
		
		
		AbstractSaveOrUpdateOperation op = new UpdateOperation(new Integer(1), "1", "node1", "theoffice", "pittsboro");		
		op.foundInterface("192.168.0.102", "if1", "P", true, 1);
		op.foundInterface("127.0.0.1", "if2", "N", true, 1);
		op.updateSnmpData();
		

	}

}
