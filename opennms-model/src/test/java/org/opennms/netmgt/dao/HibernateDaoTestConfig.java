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

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.opennms.netmgt.dao.AbstractDaoTestCase.DB;
import org.opennms.netmgt.dao.hibernate.AssetRecordDaoHibernate;
import org.opennms.netmgt.dao.hibernate.DistPollerDaoHibernate;
import org.opennms.netmgt.dao.hibernate.IpInterfaceDaoHibernate;
import org.opennms.netmgt.dao.hibernate.MonitoredServiceDaoHibernate;
import org.opennms.netmgt.dao.hibernate.NodeDaoHibernate;
import org.opennms.netmgt.dao.hibernate.ServiceTypeDaoHibernate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

public class HibernateDaoTestConfig extends DaoTestConfig {

    private LocalSessionFactoryBean m_lsfb;
    private boolean m_lsfbInitialized;
    private SessionFactory m_factory;
    private boolean m_usePool = true;
    
    protected PlatformTransactionManager setUp(DB db, boolean createDb) throws IOException {

        DataSource dataSource;
        if (isUsePool()) {
            dataSource = db.getPoolingDataSource();
        } else {
            dataSource = db.getDataSource();
        }
        setLsfb(new LocalSessionFactoryBean());
        getLsfb().setDataSource(dataSource);
        Properties props = new Properties();
        props.put("hibernate.dialect", db.getHibernateDialect());
        props.put("hibernate.show_sql", "true");

        //c3p0 settings
        props.put("hibernate.c3p0.min_size", "1");
        props.put("hibernate.c3p0.max_size", "256");
        props.put("hibernate.c3p0.timeout", "1800");
        props.put("hibernate.pool_size", "0");
        
        //props.put("hibernate.format_sql", "true");
        //props.put("hibernate.cache.use_query_cache", "true");
        //props.put("hibernate.jdbc.batch_size", "0");
        //props.put("hibernate.hbm2ddl.auto", "create-drop");
        getLsfb().setHibernateProperties(props);
        Resource modelDir = new ClassPathResource("org/opennms/netmgt/model");
        getLsfb().setMappingDirectoryLocations(new Resource[] { modelDir });
        
        //m_lsfb.setEventListeners(null);
        
        getLsfb().afterPropertiesSet();
        m_lsfbInitialized = true;
    
        
        setFactory((SessionFactory)getLsfb().getObject());
        
        if (createDb) {
            //m_lsfb.createDatabaseSchema();
            getLsfb().updateDatabaseSchema();
        }
    
        HibernateTransactionManager m_transMgr = new HibernateTransactionManager();
        m_transMgr.setSessionFactory(getFactory());
        m_transMgr.afterPropertiesSet();
        return m_transMgr;
    }

    private boolean isUsePool() {
        return m_usePool ;
    }

    private void setFactory(SessionFactory factory) {
        m_factory = factory;
    }
    
    public SessionFactory getFactory() {
        return m_factory;
    }

    protected void tearDown() {
        if (m_lsfbInitialized)
            getLsfb().destroy();
    }

    protected LocalSessionFactoryBean getLsfb() {
        return m_lsfb;
    }

    protected void setLsfb(LocalSessionFactoryBean lsfb) {
        m_lsfb = lsfb;
    }

    protected DistPollerDao createDistPollerDao() {
        DistPollerDaoHibernate dpDao = new DistPollerDaoHibernate();
        dpDao.setSessionFactory(getFactory());
        return dpDao;
    }

    protected NodeDao createNodeDao() {
        NodeDaoHibernate nodeDao = new NodeDaoHibernate();
        nodeDao.setSessionFactory(getFactory());
        return nodeDao;
    }

    protected IpInterfaceDao createIpInterfaceDao() {
        IpInterfaceDaoHibernate ifDao = new IpInterfaceDaoHibernate();
        ifDao.setSessionFactory(getFactory());
        return ifDao;
    }

    protected MonitoredServiceDao createMonitoredServiceDao() {
        MonitoredServiceDaoHibernate monSvcDao = new MonitoredServiceDaoHibernate();
        monSvcDao.setSessionFactory(getFactory());
        return monSvcDao;
    }

    protected ServiceTypeDao createServiceTypeDao() {
        ServiceTypeDaoHibernate svcTypeDao = new ServiceTypeDaoHibernate();
        svcTypeDao.setSessionFactory(getFactory());
        return svcTypeDao;
    }

    protected AssetRecordDao createAssetRecordDao() {
        AssetRecordDaoHibernate arDao = new AssetRecordDaoHibernate();
        arDao.setSessionFactory(getFactory());
        return arDao;
    }

    public void setUsePool(boolean usePool) {
        m_usePool = usePool;
    }

    protected CategoryDao createCategoryDao() {
        throw new RuntimeException("CategoryDao is not yet implemented in hibernate!");
    }

	protected SnmpInterfaceDao createSnmpInterfaceDao() {
        throw new RuntimeException("SnmpInterfaceDao is not yet implemented in hibernate!");
	}

    protected OutageDao createOutageDao() {
        throw new RuntimeException("OutageDao is not yet implemented in hibernate!");
    }

	protected EventDao createEventDao() {
        throw new RuntimeException("EventDao is not yet implemented in hibernate!");
	}
	
	protected AgentDao createAgentDao() {
        throw new RuntimeException("AgentDao is not yet implemented in hibernate!");
	}

    protected AlarmDao createAlarmDao() {
        throw new RuntimeException("AlarmDao is not yet implemented in hibernate");
    }

    protected NotificationDao createNotificationDao() {
        throw new RuntimeException("NotificationDao is not yet implemented in hibernate");
    }
    

}
