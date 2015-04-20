/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.xmlrpcd;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

public class XmlrpcAnticipatorTest extends TestCase {
    private XmlrpcAnticipator m_anticipator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        m_anticipator = new XmlrpcAnticipator(0);
    }

    @Override
    protected void tearDown() throws Exception {
        if (m_anticipator != null) {
            m_anticipator.shutdown();
        }
        
        super.tearDown();
    }

    /**
     * See if we have any bugs with starting and stopping an anticipator.
     *   
     * @throws IOException
     */
    public void testSetupAndTearDown() {
        // do nothing, let setUp and tearDown do th work
    }

    
    /**
     * See if we have any bugs with starting and stopping two anticipators back to back.
     *   
     * @throws IOException
     */
    public void testSetupTwice() throws IOException {
        // It's already been set up in setUp(), so just shutdown
        m_anticipator.shutdown();

        m_anticipator = new XmlrpcAnticipator(0);
        // Let tearDown() do the shutdown
    }
    
    public void testGoodAnticipation() throws IOException, XmlRpcException {
        Vector<Object> v = new Vector<Object>();
        Hashtable<String, String> t = new Hashtable<String, String>();
        v.add(t);
        t.put("foo", "bar");
        
        
        Hashtable<String, String> t2 = new Hashtable<String, String>();
        t2.put("foo", "bar");
        
        m_anticipator.anticipateCall("howCheesyIsIt", t2);
        
        XmlRpcClient client = new XmlRpcClient("http://localhost:" + m_anticipator.getPort());
        Vector<Object> v2 = new Vector<Object>();
        v2.add(t2);
        client.execute("howCheesyIsIt", v2);
        
        try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

        m_anticipator.verifyAnticipated();
    }
    
    public void testAnticipatedNotSeen() throws IOException, XmlRpcException {
        Vector<Object> v = new Vector<Object>();
        Hashtable<String, String> t = new Hashtable<String, String>();
        v.add(t);
        t.put("foo", "bar");
        
        
        Vector<Object> v2 = new Vector<Object>();
        Hashtable<String, String> t2 = new Hashtable<String, String>();
        v2.add(t2);
        t2.put("foo", "baz");
        
        m_anticipator.anticipateCall("howCheesyIsIt", t);
        
        XmlRpcClient client = new XmlRpcClient("http://localhost:" + m_anticipator.getPort());
        client.execute("howCheesyIsIt", v2);

        try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

        boolean sawException = false;
        try {
            m_anticipator.verifyAnticipated();
        } catch (AssertionFailedError e) {
            // good, we were expecting this
            sawException = true;
        }
        
        if (!sawException) {
            fail("Did not receive an expected AssertionFailedError when calling verifyAnticipated() on the anticipator");
        }
    }
    
    public void testIgnoreDescriptionInsideHashtable() throws IOException, XmlRpcException {
        Vector<Object> v = new Vector<Object>();
        Hashtable<String, String> t = new Hashtable<String, String>();
        v.add(t);
        t.put("description", "cheesy");
        t.put("something other than description", "hello");
        
        
        Vector<Object> v2 = new Vector<Object>();
        Hashtable<String, String> t2 = new Hashtable<String, String>();
        v2.add(t2);
        t2.put("description", "cheesiest");
        t2.put("something other than description", "hello");
        
        m_anticipator.anticipateCall("howCheesyIsIt", t);
        
        XmlRpcClient client = new XmlRpcClient("http://localhost:" + m_anticipator.getPort());
        client.execute("howCheesyIsIt", v2);
        
        try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

        m_anticipator.verifyAnticipated();
    }
}
