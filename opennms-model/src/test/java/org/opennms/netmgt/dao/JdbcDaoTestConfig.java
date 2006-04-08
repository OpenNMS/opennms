//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.dao;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.AbstractDaoTestCase.DB;
import org.opennms.netmgt.dao.jdbc.AssetRecordDaoJdbc;
import org.opennms.netmgt.dao.jdbc.CategoryDaoJdbc;
import org.opennms.netmgt.dao.jdbc.DistPollerDaoJdbc;
import org.opennms.netmgt.dao.jdbc.IpInterfaceDaoJdbc;
import org.opennms.netmgt.dao.jdbc.MonitoredServiceDaoJdbc;
import org.opennms.netmgt.dao.jdbc.NodeDaoJdbc;
import org.opennms.netmgt.dao.jdbc.ServiceTypeDaoJdbc;
import org.opennms.netmgt.dao.jdbc.SnmpInterfaceDaoJdbc;
import org.opennms.netmgt.dao.jdbc.asset.AssetRecordFactory;
import org.opennms.netmgt.dao.jdbc.category.CategoryFactory;
import org.opennms.netmgt.dao.jdbc.distpoller.DistPollerFactory;
import org.opennms.netmgt.dao.jdbc.ipif.IpInterfaceFactory;
import org.opennms.netmgt.dao.jdbc.monsvc.MonitoredServiceFactory;
import org.opennms.netmgt.dao.jdbc.node.NodeFactory;
import org.opennms.netmgt.dao.jdbc.snmpif.SnmpInterfaceFactory;
import org.opennms.netmgt.dao.jdbc.svctype.ServiceTypeFactory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

public class JdbcDaoTestConfig extends DaoTestConfig {
    
    private DataSource m_dataSource;
    private boolean m_createDb;

    protected PlatformTransactionManager setUp(DB db, boolean createDb) throws Exception {
        m_createDb = createDb;
        
        if (createDb) {
            String cmd = System.getProperty("psql.command", "psql");
            Process p = Runtime.getRuntime().exec(cmd+" brozow -U opennms -f etc/create.sql");
            p.waitFor();
            System.err.println("Got an exitValue of "+p.exitValue());
            
        }
        
        
        //m_dataSource = db.getDataSource();
        m_dataSource = db.getPoolingDataSource();
        
        // initialize the factory classs and register them with the Cache
        AssetRecordFactory.register(m_dataSource);
        DistPollerFactory.register(m_dataSource);
        IpInterfaceFactory.register(m_dataSource);
        MonitoredServiceFactory.register(m_dataSource);
        NodeFactory.register(m_dataSource);
        CategoryFactory.register(m_dataSource);
        ServiceTypeFactory.register(m_dataSource);
        SnmpInterfaceFactory.register(m_dataSource);
        
        return new DataSourceTransactionManager(m_dataSource);
    }

    protected void tearDown() {
    }

    protected AssetRecordDao createAssetRecordDao() {
        return new AssetRecordDaoJdbc(m_dataSource);
    }

    protected ServiceTypeDao createServiceTypeDao() {
        return new ServiceTypeDaoJdbc(m_dataSource);
    }

    protected MonitoredServiceDao createMonitoredServiceDao() {
        return new MonitoredServiceDaoJdbc(m_dataSource);
    }

    protected IpInterfaceDao createIpInterfaceDao() {
        return new IpInterfaceDaoJdbc(m_dataSource);
    }

    protected NodeDao createNodeDao() {
        return new NodeDaoJdbc(m_dataSource);
    }

    protected DistPollerDao createDistPollerDao() {
        return new DistPollerDaoJdbc(m_dataSource);
    }

    protected CategoryDao createCategoryDao() {
        return new CategoryDaoJdbc(m_dataSource);
    }

	protected SnmpInterfaceDao createSnmpInterfaceDao() {
		return new SnmpInterfaceDaoJdbc(m_dataSource);
	}

	public void prePopulate() {
        if (!m_createDb) return;
        OnmsDistPoller distPoller = getDistPollerDao().get("localhost");
        getDistPollerDao().delete(distPoller); 
    }




}
