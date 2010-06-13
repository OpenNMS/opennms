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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.eventd;

import org.opennms.core.utils.Base64;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.model.events.Constants;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Tticket;
import org.opennms.netmgt.xml.event.Value;

public class EventUtilTest extends OpenNMSTestCase {

    private MockService m_svc;
    private Event m_svcLostEvent;
    private Event m_nodeDownEvent;
    private Event m_bgpBkTnEvent;

    protected void setUp() throws Exception {
        super.setUp();
        m_svc = m_network.getService(1, "192.168.1.1", "SMTP");
        m_svcLostEvent = MockEventUtil.createNodeLostServiceEvent("Test", m_svc);
        m_nodeDownEvent = MockEventUtil.createNodeDownEvent("Text", m_network.getNode(1));
        m_bgpBkTnEvent = MockEventUtil.createBgpBkTnEvent("Test", m_network.getNode(1), "128.64.32.16", 2);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for 'org.opennms.netmgt.eventd.EventUtil.getValueAsString(Value)'
     */
    public void testGetValueAsString() {
        Value v = new Value();
        v.setContent(String.valueOf(Base64.encodeBase64((new String("test")).getBytes())));
        v.setEncoding("base64");
        
        assertEquals("test", EventUtil.getValueAsString(v));
    }

    /*
     * Test method for 'org.opennms.netmgt.eventd.EventUtil.escape(String, char)'
     */
    public void testEscape() {
        assertEquals("m%onkeys%47rock", EventUtil.escape("m%onkeys/rock", '/'));
    }

    /*
     * Test method for 'org.opennms.netmgt.eventd.EventUtil.getValueOfParm(String, Event)'
     */
    public void testGetValueOfParm() {
        String testString = EventUtil.getValueOfParm(EventUtil.TAG_UEI, m_svcLostEvent);
        assertEquals("uei.opennms.org/nodes/nodeLostService", testString);
        
        m_svcLostEvent.setSeverity(Constants.getSeverityString(EventConstants.SEV_MINOR));
        testString = EventUtil.getValueOfParm(EventUtil.TAG_SEVERITY, m_svcLostEvent);
        assertEquals("Minor", testString);
        
        Event event = MockEventUtil.createNodeLostServiceEvent("Test", m_svc, "noReasonAtAll");
        assertEquals("noReasonAtAll", EventUtil.getNamedParmValue("parm["+EventConstants.PARM_LOSTSERVICE_REASON+"]", event));
    }

    /*
     * Test method for 'org.opennms.netmgt.eventd.EventUtil.expandParms(String, Event)'
     */
    public void testExpandParms() {
        String testString = "%uei%:%dpname%:%nodeid%:%interface%:%service%";
        
        String newString = EventUtil.expandParms(testString, m_svcLostEvent);
        assertEquals("uei.opennms.org/nodes/nodeLostService::1:192.168.1.1:SMTP", newString);

    }
    
    /**
     * Test method for extracting parm names rather than parm values
     */
    public void testExpandParmNames() {
        String testString = "%uei%:%dpname%:%nodeid%:%parm[name-#1]%";
        
        String newString = EventUtil.expandParms(testString, m_bgpBkTnEvent);
        assertEquals("http://uei.opennms.org/standards/rfc1657/traps/bgpBackwardTransition::1:.1.3.6.1.2.1.15.3.1.7.128.64.32.16", newString);
    }

    /**
     * Test method for split-and-extract functionality indexed from beginning of name
     */
    public void testSplitAndExtractParmNamePositive() {
        String testString = "%uei%:%dpname%:%nodeid%:%parm[name-#1.1]%.%parm[name-#1.3]%.%parm[name-#1.5]%.%parm[name-#1.7]%";
        
        String newString = EventUtil.expandParms(testString, m_bgpBkTnEvent);
        assertEquals("http://uei.opennms.org/standards/rfc1657/traps/bgpBackwardTransition::1:1.6.2.15", newString);
    }

    /**
     * Additional test method for split-and-extract functionality indexed from end of name
     */
    public void testSplitAndExtractParmNameNegative() {
        String testString = "%uei%:%dpname%:%nodeid%:%parm[name-#1.-4]%.%parm[name-#1.-3]%.%parm[name-#1.-2]%.%parm[name-#1.-1]%";
        
        String newString = EventUtil.expandParms(testString, m_bgpBkTnEvent);
        assertEquals("http://uei.opennms.org/standards/rfc1657/traps/bgpBackwardTransition::1:128.64.32.16", newString);
    }
    
    /**
     * Test method for split-and-extract-range functionality indexed from beginning of name
     */
    public void testSplitAndExtractParmNameRangePositive() {
        String testString = "%uei%:%dpname%:%nodeid%:%parm[name-#1.1:4]%";
        
        String newString = EventUtil.expandParms(testString, m_bgpBkTnEvent);
        assertEquals("http://uei.opennms.org/standards/rfc1657/traps/bgpBackwardTransition::1:1.3.6.1", newString);
    }
    
    /**
     * Test method for split-and-extract-range functionality indexed from beginning of name and extending to end
     */
    public void testSplitAndExtractParmNameRangePositiveToEnd() {
        String testString = "%uei%:%dpname%:%nodeid%:%parm[name-#1.5:]%";
        
        String newString = EventUtil.expandParms(testString, m_bgpBkTnEvent);
        assertEquals("http://uei.opennms.org/standards/rfc1657/traps/bgpBackwardTransition::1:2.1.15.3.1.7.128.64.32.16", newString);
    }
    
    /**
     * Test method for split-and-extract-range functionality indexed from end of name
     */
    public void testSplitAndExtractParmNameRangeNegative() {
        String testString = "%uei%:%dpname%:%nodeid%:%parm[name-#1.-4:2]%";
        
        String newString = EventUtil.expandParms(testString, m_bgpBkTnEvent);
        assertEquals("http://uei.opennms.org/standards/rfc1657/traps/bgpBackwardTransition::1:128.64", newString);
    }
    
    /**
     * Test method for split-and-extract-range functionality indexed from end of name and extending to end
     */
    public void testSplitAndExtractParmNameRangeNegativeToEnd() {
        String testString = "%uei%:%dpname%:%nodeid%:%parm[name-#1.-5:]%";
        
        String newString = EventUtil.expandParms(testString, m_bgpBkTnEvent);
        assertEquals("http://uei.opennms.org/standards/rfc1657/traps/bgpBackwardTransition::1:7.128.64.32.16", newString);
    }
    
    public void testExpandTticketId() {
        String testString = "%tticketid%";
        String newString = EventUtil.expandParms(testString, m_nodeDownEvent);
        assertEquals("", newString);
        
        Tticket ticket = new Tticket();
        ticket.setContent("777");
        ticket.setState("1");
        m_nodeDownEvent.setTticket(ticket);
        newString = EventUtil.expandParms(testString, m_nodeDownEvent);
        assertEquals("777", newString);
    }

}
