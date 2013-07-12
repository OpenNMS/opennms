/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao;

import java.io.File;
import java.net.UnknownHostException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.api.FactoryBasedSnmpConfigDao;
import org.opennms.netmgt.dao.api.SnmpConfigDao;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpConfiguration;


public class SnmpConfigDaoTest extends TestCase {
    
    private SnmpConfigDao m_snmpConfigDao;
    private File m_configFile;
    
    @Override
    public void setUp() throws Exception {
        
        File dir = new File("target/test-work-dir");
        dir.mkdirs();
        
        m_configFile = File.createTempFile("snmp-config-"+getName()+"-", "xml", dir);
//        m_configFile.deleteOnExit();
        
        FileUtils.writeStringToFile(m_configFile, 
                "<?xml version=\"1.0\"?>" +
                "<snmp-config port=\"9161\" retry=\"1\" timeout=\"2000\"\n" + 
                "             read-community=\"myPublic\" \n" + 
                "             version=\"v1\" \n" + 
                "             max-vars-per-pdu=\"27\"  />");
        
        
        SnmpPeerFactory.setFile(m_configFile);
        
        FactoryBasedSnmpConfigDao factoryBasedSnmpConfigDao = new FactoryBasedSnmpConfigDao();
        factoryBasedSnmpConfigDao.afterPropertiesSet();
        
        m_snmpConfigDao = factoryBasedSnmpConfigDao;
        
    }
    
    private void assertConfig(String addr, int maxVarsPerPdu, int version, String community) throws UnknownHostException {
        assertNotNull(m_snmpConfigDao);

        SnmpAgentConfig config = m_snmpConfigDao.getAgentConfig(InetAddressUtils.addr(addr));
        assertNotNull(config);
        
        assertEquals(addr, InetAddressUtils.str(config.getAddress()));
        assertEquals(maxVarsPerPdu, config.getMaxVarsPerPdu());
        assertEquals(version, config.getVersion());
        assertEquals(community, config.getReadCommunity());
        
    }

    public void testGet() throws Exception {
        assertConfig("192.168.1.3", 27, 1, "myPublic");
    }
    
    public void testUpdateDefaults() throws Exception {
        
        // assert original config
        assertConfig("192.168.1.3", 27, 1, "myPublic");
        
        // update defaults
        SnmpConfiguration defaults = new SnmpConfiguration();
        defaults.setVersion(2);
        defaults.setMaxVarsPerPdu(72);
        defaults.setReadCommunity("newcommunity");
        
        m_snmpConfigDao.saveAsDefaults(defaults);
        
        // assert new config
        assertConfig("192.168.1.3", 72, 2, "newcommunity");
    }
    
    public void testUpdateConfig() throws Exception {

        // assert original config
        assertConfig("192.168.1.3", 27, 1, "myPublic");
        assertConfig("192.168.1.7", 27, 1, "myPublic");

        // update range config
        SnmpAgentConfig agentConfig = m_snmpConfigDao.getAgentConfig(InetAddressUtils.addr("192.168.1.3"));
        agentConfig.setVersion(2);
        agentConfig.setReadCommunity("newcommunity");
        
        m_snmpConfigDao.saveOrUpdate(agentConfig);
        
        // assert original config
        assertConfig("192.168.1.3", 27, 2, "newcommunity");
        assertConfig("192.168.1.7", 27, 1, "myPublic");

    }

}
