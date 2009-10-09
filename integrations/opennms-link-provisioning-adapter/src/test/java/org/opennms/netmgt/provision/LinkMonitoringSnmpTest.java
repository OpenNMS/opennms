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
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/snmpConfigFactoryContext.xml"
})
@JUnitSnmpAgent(resource="classpath:/airPairR3_walk.properties")
@TestExecutionListeners({
    JUnitSnmpAgentExecutionListener.class
})
public class LinkMonitoringSnmpTest implements MockSnmpAgentAware {
    
    interface PropertyMatcher{
        public boolean validate(String value);
    }
    
    interface EndPointStatusValidator{
       public boolean validate() throws UnknownHostException;
    }
    
    class DefaultEndPointStatusValidator implements EndPointStatusValidator{
        
        
        private String getValue(SnmpAgentConfig agentConfig, String oid) {
            SnmpValue val = SnmpUtils.get(agentConfig, SnmpObjId.get(oid));
            if(val == null || val.isNull() || val.isEndOfMib() || val.isError()) {
                return null;
            }else {
                return val.toString();
            }
        }

        public boolean validate() throws UnknownHostException {
            return false;
        }
    }
    
    class ComplexEndPointStatusValidator extends DefaultEndPointStatusValidator{
        
        private String m_storedOID;
        private String m_oidTemplate;
        private PropertyMatcher m_propertyMatcher;
        
        @Override
        public boolean validate() throws UnknownHostException {
            SnmpAgentConfig config = new SnmpAgentConfig();
            config.setAddress(InetAddress.getLocalHost());
            config.setPort(9161);
            config.setReadCommunity("public");
            
            String value = getValue(config, m_storedOID);
            value = checkAcceptableRange(value);
            
            String oid = applyToTemplate(value);
            value = getValue(config, oid);
            return m_propertyMatcher.validate(value);
        }
        
        private String checkAcceptableRange(String value){
            Integer intVal = Integer.parseInt(value);
            if(intVal == 1) {
                return value;
            }else if(intVal > 1 && intVal <=3) {
                return "2";
            }
            return null;
        }
        
        private String applyToTemplate(String index) {
            String retVal = m_oidTemplate.replace("<storedValue>", index);
            return retVal;
        }

        public void checkAndStoreValue(String sysOID) {
            m_storedOID = sysOID;
        }

        public void verifyStatus(String template, PropertyMatcher matchValue) {
            m_oidTemplate = template;
            m_propertyMatcher = matchValue;            
        }
        
    }
    
    class EndPointServiceStatusValidator extends DefaultEndPointStatusValidator{
        
        private String m_oid;
        private PropertyMatcher m_matcher;
        public EndPointServiceStatusValidator(String oid, PropertyMatcher matcher) {
            m_oid = oid;
            m_matcher = matcher;
        }
        
        @Override
        public boolean validate() throws UnknownHostException {
            SnmpAgentConfig config = new SnmpAgentConfig();
            config.setAddress(InetAddress.getLocalHost());
            config.setPort(9161);
            config.setReadCommunity("public");
            
            return m_matcher.validate(getValue(config, m_oid)) ? true :false; 
            
        }
        
    }
    
    class PingableEndPointStatusValidator{
        
        public PingableEndPointStatusValidator(String string) {
            // TODO Auto-generated constructor stub
        }
        
    }
    
    class LinkMonitor{
        
        private List<EndPointStatusValidator> m_validators = new ArrayList<EndPointStatusValidator>();
        
        public boolean isStatusUp(SnmpAgentConfig config) throws UnknownHostException {
            boolean retVal = true;
            
            for(EndPointStatusValidator validator : m_validators) {
                if(!validator.validate()) {
                    retVal = false;
                    break;
                }
            }
            
            return retVal;            
        }

        public void addValidation(EndPointStatusValidator validator) {
            m_validators.add(validator);
        }        
    }
    
    private static final String AIR_PAIR_MODEM_LOSS_OF_SIGNAL = ".1.3.6.1.4.1.7262.1.19.3.1.0";
    private static final String AIR_PAIR_R3_DUPLEX_MISMATCH = ".1.3.6.1.4.1.7262.1.19.2.3.0";
    private static final String AIR_PAIR_R4_MODEM_LOSS_OF_SIGNAL = ".1.3.6.1.4.1.7262.1.19.3.1.0";
    private static final String HORIZON_COMPACT_MODEM_LOSS_OF_SIGNAL = ".1.3.6.1.4.1.7262.2.2.8.4.4.1.0";
    private static final String HORIZON_COMPACT_ETHERNET_LINK_DOWN = ".1.3.6.1.4.1.7262.2.2.8.3.1.9.0";
    private static final String HORIZON_DUO_SYSTEM_CAPACITY = ".1.3.6.1.4.1.7262.2.3.1.1.5.0";
    private static final String HORIZON_DUO_MODEM_LOSS_OF_SIGNAL = ".1.3.6.1.4.1.7262.2.3.7.4.1.1.1.2";
    
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
        
        m_snmpAgent.updateCounter32Value(AIR_PAIR_MODEM_LOSS_OF_SIGNAL, 1);
        m_snmpAgent.updateCounter32Value(AIR_PAIR_R3_DUPLEX_MISMATCH, 1);
        
        LinkMonitor monitor = new LinkMonitor();
        monitor.addValidation(new EndPointServiceStatusValidator(AIR_PAIR_MODEM_LOSS_OF_SIGNAL, matchValue("^1$")));
        monitor.addValidation(new EndPointServiceStatusValidator(AIR_PAIR_R3_DUPLEX_MISMATCH, matchValue("^1$")));
        assertTrue("Status should up, but its not", monitor.isStatusUp(m_config));
        
        m_snmpAgent.updateCounter32Value(AIR_PAIR_MODEM_LOSS_OF_SIGNAL, 2);
        
        assertFalse("Status has been changed and is now down, should return false", monitor.isStatusUp(m_config));
    }
    
    @Test
    public void dwoTestLinkMonitorAirPairR4() throws UnknownHostException {
        ClassPathResource resource = new ClassPathResource("/airPairR4_walk.properties");
        m_snmpAgent.updateValuesFromResource(resource);
        
        m_snmpAgent.updateCounter32Value(AIR_PAIR_MODEM_LOSS_OF_SIGNAL, 1);
        m_snmpAgent.updateCounter32Value(AIR_PAIR_R4_MODEM_LOSS_OF_SIGNAL, 1);
        
        LinkMonitor monitor = new LinkMonitor();
        monitor.addValidation(new EndPointServiceStatusValidator(AIR_PAIR_MODEM_LOSS_OF_SIGNAL, matchValue("^1$")));
        monitor.addValidation(new EndPointServiceStatusValidator(AIR_PAIR_R4_MODEM_LOSS_OF_SIGNAL, matchValue("^1$")));
        assertTrue("Status should up, but its not", monitor.isStatusUp(m_config));
        
        m_snmpAgent.updateCounter32Value(AIR_PAIR_MODEM_LOSS_OF_SIGNAL, 2);
        
        assertFalse("Status has been changed and is now down, should return false", monitor.isStatusUp(m_config));
        
    }
    
    @Test
    public void dwoTestLinkMonitorHorizonCompact() throws UnknownHostException {
        ClassPathResource resource = new ClassPathResource("/horizon_compact_walk.properties");
        m_snmpAgent.updateValuesFromResource(resource);
        
        m_snmpAgent.updateCounter32Value(HORIZON_COMPACT_MODEM_LOSS_OF_SIGNAL, 1);
        m_snmpAgent.updateCounter32Value(HORIZON_COMPACT_ETHERNET_LINK_DOWN, 1);
        
        LinkMonitor monitor = new LinkMonitor();
        monitor.addValidation(new EndPointServiceStatusValidator(HORIZON_COMPACT_MODEM_LOSS_OF_SIGNAL, matchValue("^1$")));
        monitor.addValidation(new EndPointServiceStatusValidator(HORIZON_COMPACT_ETHERNET_LINK_DOWN, matchValue("^1$")));
        assertTrue("Status should up, but its not", monitor.isStatusUp(m_config));
        
        m_snmpAgent.updateCounter32Value(HORIZON_COMPACT_MODEM_LOSS_OF_SIGNAL, 2);
        
        assertFalse("Status has been changed and is now down, should return false", monitor.isStatusUp(m_config));
        
    }
    
    @Test
    public void dwoTestLinkMonitorHorizonDuoCapacity1() throws UnknownHostException {
        ClassPathResource resource = new ClassPathResource("/horizon_duo_walk.properties");
        m_snmpAgent.updateValuesFromResource(resource);
        
        m_snmpAgent.updateCounter32Value(HORIZON_DUO_MODEM_LOSS_OF_SIGNAL, 1);
        
        ComplexEndPointStatusValidator complexValidator = new ComplexEndPointStatusValidator();
        complexValidator.checkAndStoreValue(HORIZON_DUO_SYSTEM_CAPACITY);
        complexValidator.verifyStatus(".1.3.6.1.4.1.7262.2.3.7.4.1.1.1.2.<storedValue>", matchValue("^1$"));
        
        LinkMonitor monitor = new LinkMonitor();
        monitor.addValidation(new EndPointServiceStatusValidator(HORIZON_DUO_MODEM_LOSS_OF_SIGNAL, matchValue("^1$")));
        monitor.addValidation(complexValidator);
        assertTrue("Status should up, but its not", monitor.isStatusUp(m_config));
        
        m_snmpAgent.updateCounter32Value(HORIZON_DUO_MODEM_LOSS_OF_SIGNAL, 2);
        assertFalse("Status should be down", monitor.isStatusUp(m_config));
    }
    
    @Test
    public void dwoTestLinkMonitorHorizonDuoCapacity2() throws UnknownHostException {
        ClassPathResource resource = new ClassPathResource("/horizon_duo_walk.properties");
        m_snmpAgent.updateValuesFromResource(resource);
        
        m_snmpAgent.updateCounter32Value(HORIZON_DUO_MODEM_LOSS_OF_SIGNAL, 1);
        m_snmpAgent.updateCounter32Value(HORIZON_DUO_SYSTEM_CAPACITY, 2);
        
        ComplexEndPointStatusValidator complexValidator = new ComplexEndPointStatusValidator();
        complexValidator.checkAndStoreValue(HORIZON_DUO_SYSTEM_CAPACITY);
        complexValidator.verifyStatus(".1.3.6.1.4.1.7262.2.3.7.4.1.1.1.2.<storedValue>", matchValue("^1$"));
        
        LinkMonitor monitor = new LinkMonitor();
        monitor.addValidation(new EndPointServiceStatusValidator(HORIZON_DUO_MODEM_LOSS_OF_SIGNAL, matchValue("^1$")));
        monitor.addValidation(complexValidator);
        assertTrue("Status should up, but its not", monitor.isStatusUp(m_config));
        
        m_snmpAgent.updateCounter32Value(HORIZON_DUO_MODEM_LOSS_OF_SIGNAL, 2);
        assertFalse("Status should be down", monitor.isStatusUp(m_config));
    }
    
    @Test
    public void dwoTestLinkMonitorHorizonDuoCapacity3() throws UnknownHostException {
        ClassPathResource resource = new ClassPathResource("/horizon_duo_walk.properties");
        m_snmpAgent.updateValuesFromResource(resource);
        
        m_snmpAgent.updateCounter32Value(HORIZON_DUO_MODEM_LOSS_OF_SIGNAL, 1);
        m_snmpAgent.updateCounter32Value(HORIZON_DUO_SYSTEM_CAPACITY, 3);
        
        ComplexEndPointStatusValidator complexValidator = new ComplexEndPointStatusValidator();
        complexValidator.checkAndStoreValue(HORIZON_DUO_SYSTEM_CAPACITY);
        complexValidator.verifyStatus(".1.3.6.1.4.1.7262.2.3.7.4.1.1.1.2.<storedValue>", matchValue("^1$"));
        
        LinkMonitor monitor = new LinkMonitor();
        monitor.addValidation(new EndPointServiceStatusValidator(HORIZON_DUO_MODEM_LOSS_OF_SIGNAL, matchValue("^1$")));
        monitor.addValidation(complexValidator);
        assertTrue("Status should up, but its not", monitor.isStatusUp(m_config));
        
        m_snmpAgent.updateCounter32Value(HORIZON_DUO_MODEM_LOSS_OF_SIGNAL, 2);
        assertFalse("Status should be down", monitor.isStatusUp(m_config));
    }
    
    
    @Test
    public void dwoTestLinkMonitoringPingableDevice() throws UnknownHostException {
        m_snmpAgent.updateCounter32Value(".1.3.6.1.4.1.7262.1.19.3.1.0", 1);
        m_snmpAgent.updateCounter32Value(".1.3.6.1.4.1.7262.1.19.2.3.0", 1);
        
        LinkMonitor monitor = new LinkMonitor();
        monitor.addValidation(new EndPointServiceStatusValidator(".1.3.6.1.4.1.1000.1.19.3.1.0", pingableValue()));
        
        assertFalse("Status has been changed and is now down, should return false", monitor.isStatusUp(m_config));
    }
    
    
    
    private PropertyMatcher matchValue(final String matcher) {
        return new PropertyMatcher() {

            public boolean validate(String value) {
                if(value != null) {
                    return value.matches(matcher);
                }else {
                    return false;
                }
            }
            
        };
    }
    
    private PropertyMatcher pingableValue() {
        return new PropertyMatcher() {

            public boolean validate(String value) {
                return value != null ? true : false;
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
