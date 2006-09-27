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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.AbstractDaoTestCase.DB;
import org.opennms.netmgt.dao.jdbc.AlarmDaoJdbc;
import org.opennms.netmgt.dao.jdbc.AssetRecordDaoJdbc;
import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.dao.jdbc.CategoryDaoJdbc;
import org.opennms.netmgt.dao.jdbc.DistPollerDaoJdbc;
import org.opennms.netmgt.dao.jdbc.EventDaoJdbc;
import org.opennms.netmgt.dao.jdbc.IpInterfaceDaoJdbc;
import org.opennms.netmgt.dao.jdbc.MonitoredServiceDaoJdbc;
import org.opennms.netmgt.dao.jdbc.NodeDaoJdbc;
import org.opennms.netmgt.dao.jdbc.NotificationDaoJdbc;
import org.opennms.netmgt.dao.jdbc.OutageDaoJdbc;
import org.opennms.netmgt.dao.jdbc.ServiceTypeDaoJdbc;
import org.opennms.netmgt.dao.jdbc.SnmpInterfaceDaoJdbc;
import org.opennms.netmgt.dao.jdbc.UserNotificationDaoJdbc;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

public class JdbcDaoTestConfig extends DaoTestConfig {
    
    private DataSource m_dataSource;
    private boolean m_createDb;

    protected PlatformTransactionManager setUp(DB db, boolean createDb) throws Exception {
        m_createDb = createDb;
        
        class ReaderEater extends Thread {
            BufferedReader m_reader;

            ReaderEater(BufferedReader r) {
                m_reader = r;
            }
            
            public BufferedReader getReader() {
                return m_reader;
            }

            public void run() {
                try {
                    while (m_reader.readLine() != null) {
                    }
                } catch (IOException e) {
                    e.printStackTrace();  
                }
            }
        }

        
        if (createDb) {
        	
        	Resource resource = new ClassPathResource("create.sql");
        	File createSql = resource.getFile();
        	
            String cmd = System.getProperty("psql.command", "psql") ;
            System.err.println("psql.command = "+cmd);
            cmd = cmd+" test -U opennms -f "+createSql.getAbsolutePath();

            System.err.println("Executing: "+cmd);
            Process p = Runtime.getRuntime().exec(cmd);
            ReaderEater inputEater = new ReaderEater(new BufferedReader(new InputStreamReader(p.getInputStream())));
            ReaderEater errorEater = new ReaderEater(new BufferedReader(new InputStreamReader(p.getErrorStream())));
            inputEater.start();
            errorEater.start();
            p.waitFor();
            inputEater.getReader().close();
            errorEater.getReader().close();
            
            System.err.println("Got an exitValue of "+p.exitValue());
            p.destroy();
        }
                
        //m_dataSource = db.getDataSource();
        m_dataSource = db.getPoolingDataSource();
        
        // initialize the factory classs and register them with the Cache
        Cache.registerFactories(m_dataSource);
        
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

    protected OutageDao createOutageDao() {
        return new OutageDaoJdbc(m_dataSource);
    }

    protected EventDao createEventDao() {
        return new EventDaoJdbc(m_dataSource);
    }

    public void prePopulate() {
        if (!m_createDb) return;
        OnmsDistPoller distPoller = getDistPollerDao().get("localhost");
        getDistPollerDao().delete(distPoller); 
    }

    protected AlarmDao createAlarmDao() {
        return new AlarmDaoJdbc(m_dataSource);
    }

    protected NotificationDao createNotificationDao() {
        return new NotificationDaoJdbc(m_dataSource);
    }

    protected UserNotificationDao createUserNotificationDao() {
        return new UserNotificationDaoJdbc(m_dataSource);
    }

    @Override
    public int dbQueryForInt(String sql) {
        throw new UnsupportedOperationException("not yet implmented");
    }

}
