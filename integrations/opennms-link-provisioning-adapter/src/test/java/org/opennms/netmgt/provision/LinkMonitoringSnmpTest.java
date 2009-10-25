/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.mock.snmp.JUnitSnmpAgent;
import org.opennms.mock.snmp.JUnitSnmpAgentExecutionListener;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.mock.snmp.MockSnmpAgentAware;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/snmpConfigFactoryContext.xml"
})
@JUnitSnmpAgent(resource="classpath:/snmpTestData1.properties")
@TestExecutionListeners({
    JUnitSnmpAgentExecutionListener.class
})
public class LinkMonitoringSnmpTest implements MockSnmpAgentAware {
    
    interface PropertyMatcher{
        public boolean validate(String value);
    }
    
    class EndPointServiceStatusValidator{
        
        private String m_oid;
        private PropertyMatcher m_matcher;
        public EndPointServiceStatusValidator(String oid, PropertyMatcher matcher) {
            m_oid = oid;
            m_matcher = matcher;
        }
        
        private String getValue(SnmpAgentConfig agentConfig, String oid) {
            SnmpValue val = SnmpUtils.get(agentConfig, SnmpObjId.get(oid));
            if(val == null || val.isNull() || val.isEndOfMib() || val.isError()) {
                return null;
            }else {
                return val.toString();
            }
        }

        public boolean validate() throws UnknownHostException {
            SnmpAgentConfig config = new SnmpAgentConfig();
            config.setAddress(InetAddress.getLocalHost());
            config.setPort(9161);
            config.setReadCommunity("public");
            
            return m_matcher.validate(getValue(config, m_oid)) ? true :false; 
            
        }
        
    }
    
    class LinkMonitor{
        
        private List<EndPointServiceStatusValidator> m_validators = new ArrayList<EndPointServiceStatusValidator>();
        
        public boolean isStatusUp(SnmpAgentConfig config) throws UnknownHostException {
            boolean retVal = true;
            
            for(EndPointServiceStatusValidator validator : m_validators) {
                if(!validator.validate()) {
                    retVal = false;
                    break;
                }
            }
            
            return retVal;            
        }

        public void addValidation(EndPointServiceStatusValidator validator) {
            m_validators.add(validator);
        }
        
    }
    
    private MockSnmpAgent m_snmpAgent;
    private SnmpAgentConfig m_config;
    
    
    @Before
    public void setup() throws InterruptedException, UnknownHostException {
        if(m_config == null) {
            m_config = new SnmpAgentConfig();
            m_config.setAddress(InetAddress.getLocalHost());
            m_config.setPort(9161);
            m_config.setReadCommunity("public");
        }
    }
    
    @After
    public void tearDown() throws InterruptedException{
        
    }
    
    @Test
    public void dwoTestSnmpUpdateMIBProperty() throws UnknownHostException {
        assertNotNull(m_snmpAgent);
        
        String modSyncValue = getValue(m_config, ".1.3.6.1.4.1.7262.1.19.3.1.0");
        assertNotNull(modSyncValue);
        assertEquals(1, Integer.parseInt(modSyncValue));
        
        m_snmpAgent.updateCounter32Value(".1.3.6.1.4.1.7262.1.19.3.1.0", 2);
        
        modSyncValue = getValue(m_config, ".1.3.6.1.4.1.7262.1.19.3.1.0");
        assertNotNull(modSyncValue);
        assertEquals(2, Integer.parseInt(modSyncValue));
    }
    
    @Test
    public void dwoTestLinkMonitorAirPairR3() throws UnknownHostException {
        assertNotNull(m_snmpAgent);
        
        m_snmpAgent.updateCounter32Value(".1.3.6.1.4.1.7262.1.19.3.1.0", 1);
        m_snmpAgent.updateCounter32Value(".1.3.6.1.4.1.7262.1.19.2.3.0", 1);
        
        LinkMonitor monitor = new LinkMonitor();
        monitor.addValidation(new EndPointServiceStatusValidator(".1.3.6.1.4.1.7262.1.19.3.1.0", matchValue("^1$")));
        monitor.addValidation(new EndPointServiceStatusValidator("1.3.6.1.4.1.7262.1.19.2.3.0", matchValue("^1$")));
        assertTrue("Status should up, but its not", monitor.isStatusUp(m_config));
        
        m_snmpAgent.updateCounter32Value(".1.3.6.1.4.1.7262.1.19.3.1.0", 2);
        
        assertFalse("Status has been changed and is now down, should return false", monitor.isStatusUp(m_config));
    }
    
    private PropertyMatcher matchValue(final String matcher) {
        return new PropertyMatcher() {

            public boolean validate(String value) {
                return value.matches(matcher);
            }
            
        };
    }
    
    private String getValue(SnmpAgentConfig agentConfig, String oid) {
        SnmpValue val = SnmpUtils.get(agentConfig, SnmpObjId.get(oid));
        if(val == null || val.isNull() || val.isEndOfMib() || val.isError()) {
            return null;
        }else {
            return val.toString();
        }
    }

    public void setMockSnmpAgent(MockSnmpAgent agent) {
        m_snmpAgent = agent;
    }

}
