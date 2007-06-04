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

import java.io.IOException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.CapsdConfigFactory;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.DefaultCapsdConfigManager;
import org.opennms.netmgt.config.OpennmsServerConfigFactory;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.dao.support.RrdTestUtils;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.test.ConfigurationTestUtils;

public class CapsdTest extends OpenNMSTestCase {
    private Capsd m_capsd;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        m_capsd = Capsd.getInstance();
        DatabaseSchemaConfigFactory.setInstance(new DatabaseSchemaConfigFactory(ConfigurationTestUtils.getReaderForConfigFile("database-schema.xml")));
        DefaultCapsdConfigManager capsdConfig = new DefaultCapsdConfigManager(ConfigurationTestUtils.getReaderForResource(this, "/org/opennms/netmgt/capsd/capsd-configuration.xml"));
        CapsdConfigFactory.setInstance(capsdConfig);
        
        OpennmsServerConfigFactory onmsSvrConfig = new OpennmsServerConfigFactory(ConfigurationTestUtils.getReaderForConfigFile("opennms-server.xml"));
        OpennmsServerConfigFactory.setInstance(onmsSvrConfig);
        
        PollerConfigFactory.setInstance(new PollerConfigFactory(System.currentTimeMillis(), ConfigurationTestUtils.getReaderForResource(this, "/org/opennms/netmgt/capsd/poller-configuration.xml"), onmsSvrConfig.getServerName(), onmsSvrConfig.verifyServer()));

        RrdTestUtils.initialize();

        DataCollectionConfigFactory.setInstance(new DataCollectionConfigFactory(ConfigurationTestUtils.getReaderForResource(this, "/org/opennms/netmgt/capsd/datacollection-config.xml")));

        CollectdConfigFactory.setInstance(new CollectdConfigFactory(ConfigurationTestUtils.getReaderForResource(this, "/org/opennms/netmgt/capsd/collectd-configuration.xml"), onmsSvrConfig.getServerName(), onmsSvrConfig.verifyServer()));
        
        CapsdDbSyncerFactory.init();
        ((JdbcCapsdDbSyncer) CapsdDbSyncerFactory.getInstance()).setNextSvcIdSql(m_db.getNextServiceIdStatement());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public final void testStartStop() throws MarshalException, ValidationException, IOException {
        m_capsd.init();
        m_capsd.start();
        m_capsd.stop();
    }
    
}
