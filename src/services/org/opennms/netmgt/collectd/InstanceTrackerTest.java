//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.collectd;

public class InstanceTrackerTest extends SnmpCollectorTestCase {

    public void testSingleInstanceTrackerZeroInstance() {
        testSpecificInstanceTracker("0", SnmpObjId.get(m_sysNameOid, "0"));
    }
    
    public void testSingleInstanceTrackerMultiIdInstance() {
        testSpecificInstanceTracker("1.2.3", SnmpObjId.get(m_sysNameOid, "1.2.3"));
    }
    
    public void testSpecificInstanceTracker(String instance, SnmpObjId receivedOid) {
        SnmpInstId inst = new SnmpInstId(instance);
        InstanceTracker it = new SpecificInstanceTracker(m_sysNameOid, instance);
        
        testInstanceTrackerInnerLoop(it, inst, receivedOid);
        
        // ensure that it thinks we are finished
        assertFalse(it.hasOidForNext());
    }
    
    private void testInstanceTrackerInnerLoop(InstanceTracker it, SnmpInstId inst, SnmpObjId receivedOid) {
        // ensure it needs to receive something - object id for the instance
        assertTrue(it.hasOidForNext());
        // ensure that is asks for the oid preceeding
        assertEquals(SnmpObjId.get(m_sysNameOid, inst).decrement(), it.getOidForNext());
        // tell it received the expected one and ensure that it agrees
        
        //FIXME: take this if out an make it explicit as an arg or something (can lead to hidden failures)
        if (receivedOid.equals(SnmpObjId.get(m_sysNameOid, inst)))
            assertEquals(inst, it.receivedOid(receivedOid));
        else
            assertNull(it.receivedOid(receivedOid));
    }
    
    public void testSingleInstanceTrackerNonZeroInstance() {
        testSpecificInstanceTracker("1", SnmpObjId.get(m_sysNameOid, "1"));
    }
    
    public void testSingleInstanceTrackerNoMatch() {
        testSpecificInstanceTracker("0", SnmpObjId.get(m_sysNameOid, "1"));
    }
    
    public void testListInstanceTrackerWithAllResults() {
        String instances[] = { "1", "3", "5" };
        InstanceTracker it = new SpecificInstanceTracker(m_sysNameOid, toCommaSeparated(instances));
        
        for(int i = 0; i < instances.length; i++) {
            testInstanceTrackerInnerLoop(it, new SnmpInstId(instances[i]), SnmpObjId.get(m_sysNameOid, instances[i]));
        }
        assertFalse(it.hasOidForNext());
    }
    
    public void testListInstanceTrackerWithNoResults() {
        String instances[] = { "1", "3", "5" };
        InstanceTracker it = new SpecificInstanceTracker(m_sysNameOid, toCommaSeparated(instances));
        
        for(int i = 0; i < instances.length; i++) {
            testInstanceTrackerInnerLoop(it, new SnmpInstId(instances[i]), SnmpObjId.get(m_sysNameOid, instances[i]+".0"));
        }
        assertFalse(it.hasOidForNext());
    }
    
    public void testColumnInstanceTracker() {
        SnmpObjId colOid = SnmpObjId.get(".1.3.6.1.2.1.1.5");
        SnmpObjId nextColOid = SnmpObjId.get(".1.3.6.1.2.1.1.6.2");
        InstanceTracker it = new ColumnInstanceTracker(colOid);
        
        int colLength = 5;
        
        for(int i = 0; i < colLength; i++) {
            String instance = Integer.toString(i);
            testInstanceTrackerInnerLoop(it, new SnmpInstId(instance), colOid.append(instance));
        }

        // it needs another non matching receipt before it can know its done
        assertTrue(it.hasOidForNext());
        SnmpObjId oidForNext = it.getOidForNext();
        assertEquals(colOid.append(""+(colLength-1)), oidForNext); 
        assertNull(it.receivedOid(nextColOid));

        // now it should be done
        assertFalse(it.hasOidForNext());
        
        
        
    }
    
    private String toCommaSeparated(String[] instances) {
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < instances.length; i++) {
            if (i != 0) {
                buf.append(',');
            }
            buf.append(instances[i]);
        }
        return buf.toString();
    }


}
