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
// Modifications:
//
// 2007 May 06: Moved plugin management and database synchronization out
//              of CapsdConfigFactory, use RrdTestUtils to setup RRD
//              subsystem, and move configuration files out of embedded
//              strings into src/test/resources. - dj@opennms.org
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

package org.opennms.netmgt.capsd;

import java.io.InputStream;

import org.opennms.core.concurrent.RunnableConsumerThreadPool;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.config.CapsdConfigFactory;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.DefaultCapsdConfigManager;
import org.opennms.netmgt.config.OpennmsServerConfigFactory;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.dao.support.RrdTestUtils;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.model.events.AnnotationBasedEventListenerAdapter;
import org.opennms.test.ConfigurationTestUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

public class CapsdTest extends OpenNMSTestCase {
    private static final int FOREIGN_NODEID = 77;
    private Capsd m_capsd;
    private MockSnmpAgent m_agent;
    
    @Override
    protected void setUp() throws Exception {

    	super.setUp();

        m_agent = MockSnmpAgent.createAgentAndRun(new ClassPathResource("org/opennms/netmgt/snmp/snmpTestData1.properties"), this.myLocalHost().getHostAddress() + "/9161");

        InputStream configStream = ConfigurationTestUtils.getInputStreamForConfigFile("database-schema.xml");
        DatabaseSchemaConfigFactory.setInstance(new DatabaseSchemaConfigFactory(configStream));
        configStream.close();

        configStream = ConfigurationTestUtils.getInputStreamForResource(this, "/org/opennms/netmgt/capsd/capsd-configuration.xml");
        DefaultCapsdConfigManager capsdConfig = new DefaultCapsdConfigManager(configStream);
        configStream.close();
        CapsdConfigFactory.setInstance(capsdConfig);
        
        configStream = ConfigurationTestUtils.getInputStreamForConfigFile("opennms-server.xml");
        OpennmsServerConfigFactory onmsSvrConfig = new OpennmsServerConfigFactory(configStream);
        configStream.close();
        OpennmsServerConfigFactory.setInstance(onmsSvrConfig);

        configStream = ConfigurationTestUtils.getInputStreamForResource(this, "/org/opennms/netmgt/capsd/poller-configuration.xml");
        PollerConfigFactory.setInstance(new PollerConfigFactory(System.currentTimeMillis(), configStream, onmsSvrConfig.getServerName(), onmsSvrConfig.verifyServer()));
        configStream.close();
        RrdTestUtils.initialize();

        configStream = ConfigurationTestUtils.getInputStreamForResource(this, "/org/opennms/netmgt/capsd/collectd-configuration.xml");
        CollectdConfigFactory.setInstance(new CollectdConfigFactory(configStream, onmsSvrConfig.getServerName(), onmsSvrConfig.verifyServer()));
        configStream.close();
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(m_db);
        
        JdbcCapsdDbSyncer syncer = new JdbcCapsdDbSyncer();
        syncer.setJdbcTemplate(jdbcTemplate);
        syncer.setOpennmsServerConfig(OpennmsServerConfigFactory.getInstance());
        syncer.setCapsdConfig(CapsdConfigFactory.getInstance());
        syncer.setPollerConfig(PollerConfigFactory.getInstance());
        syncer.setCollectdConfig(CollectdConfigFactory.getInstance());
        syncer.setNextSvcIdSql(m_db.getNextServiceIdStatement());
        syncer.afterPropertiesSet();

        PluginManager pluginManager = new PluginManager();
        pluginManager.setCapsdConfig(capsdConfig);
        pluginManager.afterPropertiesSet();
        
        DefaultProcessorFactory defaultProcessorFactory = new DefaultProcessorFactory();
        defaultProcessorFactory.setCapsdDbSyncer(syncer);
        defaultProcessorFactory.setPluginManager(pluginManager);
        
        RunnableConsumerThreadPool suspectRunner = 
            new RunnableConsumerThreadPool("SuspectRunner", 0.0f, 0.0f, 1);
        
        RunnableConsumerThreadPool rescanRunner = 
            new RunnableConsumerThreadPool("RescanRunner", 0.0f, 0.0f, 1);
        
        Scheduler scheduler = new Scheduler(rescanRunner.getRunQueue(), defaultProcessorFactory);
        
        BroadcastEventProcessor eventHandler = new BroadcastEventProcessor();
        eventHandler.setSuspectEventProcessorFactory(defaultProcessorFactory);
        eventHandler.setLocalServer("localhost");
        eventHandler.setScheduler(scheduler);
        eventHandler.setSuspectQueue(suspectRunner.getRunQueue());
        eventHandler.afterPropertiesSet();
        
        AnnotationBasedEventListenerAdapter adapter = 
            new AnnotationBasedEventListenerAdapter(eventHandler, m_eventdIpcMgr);

        m_capsd = new Capsd();
        m_capsd.setCapsdDbSyncer(syncer);
        m_capsd.setSuspectEventProcessorFactory(defaultProcessorFactory);
        m_capsd.setCapsdConfig(capsdConfig);
        m_capsd.setSuspectRunner(suspectRunner);
        m_capsd.setRescanRunner(rescanRunner);
        m_capsd.setScheduler(scheduler);
        m_capsd.setEventListener(adapter);
        m_capsd.afterPropertiesSet();
    }

    @Override
    protected void createMockNetwork() {
        super.createMockNetwork();
        
        m_network.addNode(FOREIGN_NODEID, "ForeignNode");
//        m_network.addInterface(myLocalHost());
//        m_network.addService("ICMP");
//        m_network.addService("SNMP");
        m_network.addInterface("172.20.1.201");
        m_network.addInterface("fe80:0000:0000:0000:ffff:eeee:dddd:cccc");
        m_network.addService("ICMP");
        m_network.addService("SNMP");
    }

    @Override
    public String getSnmpConfig() {
        return "<?xml version=\"1.0\"?>\n" + 
                "<snmp-config "+ 
                " retry=\"3\" timeout=\"3000\"\n" + 
                " read-community=\"public\"" +
                " write-community=\"private\"\n" + 
                " port=\"161\"\n" +
                " version=\"v1\">\n" +
                "\n" +
                "   <definition port=\"9161\" version=\"v2c\" " +
                "       security-name=\"opennmsUser\" \n" + 
                "       auth-passphrase=\"0p3nNMSv3\" \n" +
                "       privacy-passphrase=\"0p3nNMSv3\" >\n" +
                "       <specific>"+myLocalHost().getHostAddress()+"</specific>\n" +
                "   </definition>\n" + 
                "\n" + 
                "   <definition version=\"v2c\" port=\"9161\" read-community=\"public\" proxy-host=\""+myLocalHost().getHostAddress()+"\">\n" + 
                "      <specific>172.20.1.201</specific>\n" +
                "      <specific>172.20.1.204</specific>\n" +
                "   </definition>\n" + 
                "</snmp-config>";
    }
     @Override
    protected void tearDown() throws Exception {
        m_agent.shutDownAndWait();
        super.tearDown();
    }

    public final void testRescan() throws Exception {
        
        assertEquals("Initally only 2 interfaces", 2, m_db.countRows("select * from ipinterface where nodeid = ?", FOREIGN_NODEID));

        m_capsd.init();
        m_capsd.start();
        
        m_capsd.rescanInterfaceParent(77);
        
        Thread.sleep(10000);
        
        m_capsd.stop();
        
        assertEquals("after scanning should be 3 interfaces", 3, m_db.countRows("select * from ipinterface where nodeid = ?", FOREIGN_NODEID));
    }
    
    public final void testRescanOfForeignNode() throws Exception {
        
        m_db.getJdbcTemplate().update("update node set foreignSource='testSource', foreignId='123' where nodeid = ?", FOREIGN_NODEID);
        
        assertEquals("Initally only 2 interfaces", 2, m_db.countRows("select * from ipinterface where nodeid = ?", FOREIGN_NODEID));

        m_capsd.init();
        m_capsd.start();
        
        m_capsd.rescanInterfaceParent(77);
        
        Thread.sleep(10000);
        
        m_capsd.stop();
        
        assertEquals("after scanning should still be 2 since its foreign", 2, m_db.countRows("select * from ipinterface where nodeid = ?", FOREIGN_NODEID));

    }
    
}
