/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.Base64;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.datablock.EventUtil;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Tticket;
import org.opennms.netmgt.xml.event.Value;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
public class EventUtilTest {

    private final MockNetwork m_network = new MockNetwork();
    private MockService m_svc;
    private Event m_svcLostEvent;
    private Event m_nodeDownEvent;
    private Event m_bgpBkTnEvent;

    @Before
    public void setUp() throws Exception {
        m_network.createStandardNetwork();
        m_svc = m_network.getService(1, "192.168.1.1", "SMTP");
        m_svcLostEvent = MockEventUtil.createNodeLostServiceEvent("Test", m_svc);
        m_nodeDownEvent = MockEventUtil.createNodeDownEvent("Text", m_network.getNode(1));
        m_bgpBkTnEvent = MockEventUtil.createBgpBkTnEvent("Test", m_network.getNode(1), "128.64.32.16", 2);
    }

    @After
    public void tearDown() throws Exception {
    }

    /*
     * Test method for 'org.opennms.netmgt.eventd.EventUtil.getValueAsString(Value)'
     */
    @Test
    public void testGetValueAsString() {
        Value v = new Value();
        v.setContent(String.valueOf(Base64.encodeBase64((new String("abcd")).getBytes())));
        v.setEncoding("base64");
        
        assertEquals("0x61626364", EventUtil.getValueAsString(v));
    }

    /*
     * Test method for 'org.opennms.netmgt.eventd.EventUtil.escape(String, char)'
     */
    @Test
    public void testEscape() {
        assertEquals("m%onkeys%47rock", EventUtil.escape("m%onkeys/rock", '/'));
    }

    /*
     * Test method for 'org.opennms.netmgt.eventd.EventUtil.getValueOfParm(String, Event)'
     */
    @Test
    public void testGetValueOfParm() {
        String testString = EventUtil.getValueOfParm(EventUtil.TAG_UEI, m_svcLostEvent);
        assertEquals(EventConstants.NODE_LOST_SERVICE_EVENT_UEI, testString);
        
        m_svcLostEvent.setSeverity(OnmsSeverity.MINOR.getLabel());
        testString = EventUtil.getValueOfParm(EventUtil.TAG_SEVERITY, m_svcLostEvent);
        assertEquals("Minor", testString);
        
        Event event = MockEventUtil.createNodeLostServiceEvent("Test", m_svc, "noReasonAtAll");
        assertEquals("noReasonAtAll", EventUtil.getNamedParmValue("parm["+EventConstants.PARM_LOSTSERVICE_REASON+"]", event));
    }

    /*
     * Test method for 'org.opennms.netmgt.eventd.EventUtil.expandParms(String, Event)'
     */
    @Test
    public void testExpandParms() {
        String testString = "%uei%:%dpname%:%nodeid%:%interface%:%service%";
        
        String newString = EventUtil.expandParms(testString, m_svcLostEvent);
        assertEquals(EventConstants.NODE_LOST_SERVICE_EVENT_UEI + "::1:192.168.1.1:SMTP", newString);

    }
    
    /**
     * Test method for extracting parm names rather than parm values
     */
    @Test
    public void testExpandParmNames() {
        String testString = "%uei%:%dpname%:%nodeid%:%parm[name-#1]%";
        
        String newString = EventUtil.expandParms(testString, m_bgpBkTnEvent);
        assertEquals("http://uei.opennms.org/standards/rfc1657/traps/bgpBackwardTransition::1:.1.3.6.1.2.1.15.3.1.7.128.64.32.16", newString);
    }

    /**
     * Test method for split-and-extract functionality indexed from beginning of name
     */
    @Test
    public void testSplitAndExtractParmNamePositive() {
        String testString = "%uei%:%dpname%:%nodeid%:%parm[name-#1.1]%.%parm[name-#1.3]%.%parm[name-#1.5]%.%parm[name-#1.7]%";
        
        String newString = EventUtil.expandParms(testString, m_bgpBkTnEvent);
        assertEquals("http://uei.opennms.org/standards/rfc1657/traps/bgpBackwardTransition::1:1.6.2.15", newString);
    }

    /**
     * Additional test method for split-and-extract functionality indexed from end of name
     */
    @Test
    public void testSplitAndExtractParmNameNegative() {
        String testString = "%uei%:%dpname%:%nodeid%:%parm[name-#1.-4]%.%parm[name-#1.-3]%.%parm[name-#1.-2]%.%parm[name-#1.-1]%";
        
        String newString = EventUtil.expandParms(testString, m_bgpBkTnEvent);
        assertEquals("http://uei.opennms.org/standards/rfc1657/traps/bgpBackwardTransition::1:128.64.32.16", newString);
    }
    
    /**
     * Test method for split-and-extract-range functionality indexed from beginning of name
     */
    @Test
    public void testSplitAndExtractParmNameRangePositive() {
        String testString = "%uei%:%dpname%:%nodeid%:%parm[name-#1.1:4]%";
        
        String newString = EventUtil.expandParms(testString, m_bgpBkTnEvent);
        assertEquals("http://uei.opennms.org/standards/rfc1657/traps/bgpBackwardTransition::1:1.3.6.1", newString);
    }
    
    /**
     * Test method for split-and-extract-range functionality indexed from beginning of name and extending to end
     */
    @Test
    public void testSplitAndExtractParmNameRangePositiveToEnd() {
        String testString = "%uei%:%dpname%:%nodeid%:%parm[name-#1.5:]%";
        
        String newString = EventUtil.expandParms(testString, m_bgpBkTnEvent);
        assertEquals("http://uei.opennms.org/standards/rfc1657/traps/bgpBackwardTransition::1:2.1.15.3.1.7.128.64.32.16", newString);
    }
    
    /**
     * Test method for split-and-extract-range functionality indexed from end of name
     */
    @Test
    public void testSplitAndExtractParmNameRangeNegative() {
        String testString = "%uei%:%dpname%:%nodeid%:%parm[name-#1.-4:2]%";
        
        String newString = EventUtil.expandParms(testString, m_bgpBkTnEvent);
        assertEquals("http://uei.opennms.org/standards/rfc1657/traps/bgpBackwardTransition::1:128.64", newString);
    }
    
    /**
     * Test method for split-and-extract-range functionality indexed from end of name and extending to end
     */
    @Test
    public void testSplitAndExtractParmNameRangeNegativeToEnd() {
        String testString = "%uei%:%dpname%:%nodeid%:%parm[name-#1.-5:]%";
        
        String newString = EventUtil.expandParms(testString, m_bgpBkTnEvent);
        assertEquals("http://uei.opennms.org/standards/rfc1657/traps/bgpBackwardTransition::1:7.128.64.32.16", newString);
    }
    
    @Test
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
