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
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.opennms.netmgt.dao.AbstractDaoTestCase.DB;
import org.opennms.netmgt.dao.hibernate.AlarmDaoHibernate;
import org.opennms.netmgt.dao.hibernate.AssetRecordDaoHibernate;
import org.opennms.netmgt.dao.hibernate.AvailabilityReportLocatorDaoHibernate;
import org.opennms.netmgt.dao.hibernate.CategoryDaoHibernate;
import org.opennms.netmgt.dao.hibernate.DistPollerDaoHibernate;
import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;
import org.opennms.netmgt.dao.hibernate.IpInterfaceDaoHibernate;
import org.opennms.netmgt.dao.hibernate.MonitoredServiceDaoHibernate;
import org.opennms.netmgt.dao.hibernate.NodeDaoHibernate;
import org.opennms.netmgt.dao.hibernate.NotificationDaoHibernate;
import org.opennms.netmgt.dao.hibernate.OutageDaoHibernate;
import org.opennms.netmgt.dao.hibernate.ServiceTypeDaoHibernate;
import org.opennms.netmgt.dao.hibernate.SnmpInterfaceDaoHibernate;
import org.opennms.netmgt.dao.hibernate.UserNotificationDaoHibernate;
import org.opennms.netmgt.model.AvailabilityReportLocator;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsArpInterface;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsUserNotification;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

public class HibernateDaoTestConfig extends DaoTestConfig {

    private LocalSessionFactoryBean m_lsfb;
    private DataSource m_dataSource;
    private boolean m_lsfbInitialized;
    private SessionFactory m_factory;
    private boolean m_usePool = false;
    private boolean m_createDb = false;

    
    class ReaderEater extends Thread {
        boolean eat = true;
        BufferedReader m_reader;

        ReaderEater(BufferedReader r) {
            m_reader = r;
        }
        
        public BufferedReader getReader() {
            return m_reader;
        }

        public void run() {
            try {
                String s;
                while ((s = m_reader.readLine()) != null) {
                    if (!eat) {
                        System.out.println(s);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();  
            }
        }
    }

    
    protected PlatformTransactionManager setUp(DB db, boolean createDb) throws Exception {
    	m_createDb = createDb;
        if (isUsePool()) {
            m_dataSource = db.getPoolingDataSource();
        } else {
            m_dataSource = db.getDataSource();
        }
        
        Class[] annotatedClasses = {
        	OnmsDistPoller.class,
        	OnmsNode.class,
        	OnmsAssetRecord.class,
        	OnmsIpInterface.class,
            OnmsSnmpInterface.class,
            OnmsArpInterface.class,
        	OnmsMonitoredService.class,
        	OnmsCategory.class,
        	OnmsServiceType.class,
        	OnmsOutage.class,
        	OnmsEvent.class,
        	OnmsAlarm.class,
        	OnmsNotification.class,
        	OnmsUserNotification.class,
        	AvailabilityReportLocator.class,
            OnmsApplication.class

        };
        
        String[] annotatedPackages = {
        		"com.opennms.netmgt.model"
        };
        
        AnnotationSessionFactoryBean sfb = new AnnotationSessionFactoryBean();
        sfb.setAnnotatedClasses(annotatedClasses);
        sfb.setAnnotatedPackages(annotatedPackages);
		setLsfb(sfb);
        getLsfb().setDataSource(m_dataSource);
        Properties props = new Properties();
        props.put("hibernate.dialect", db.getHibernateDialect());
        props.put("hibernate.show_sql", System.getProperty("hibernate.show_sql", "false"));

        //c3p0 settings
        props.put("hibernate.c3p0.min_size", "1");
        props.put("hibernate.c3p0.max_size", "256");
        props.put("hibernate.c3p0.timeout", "1800");
        props.put("hibernate.pool_size", "0");
        
        props.put("hibernate.format_sql", "true");
        props.put("hibernate.cache.use_second_level_cache", "false");
        props.put("hibernate.cache", "false");
        //props.put("hibernate.cache.use_query_cache", "true");
        props.put("hibernate.jdbc.batch_size", "0");
        //props.put("hibernate.hbm2ddl.auto", "create-drop");
        getLsfb().setHibernateProperties(props);
        
        //m_lsfb.setEventListeners(null);
        
        getLsfb().afterPropertiesSet();
        m_lsfbInitialized = true;
    
        
        setFactory((SessionFactory)getLsfb().getObject());
       
        
//        if (createDb) {
//            //m_lsfb.createDatabaseSchema();
//            getLsfb().updateDatabaseSchema();
//        }
        

        if (createDb) {
            File etcDir = new File("../opennms-daemon/src/main/filtered/etc");
            initDatabase(etcDir);
        }
        
        HibernateTransactionManager m_transMgr = new HibernateTransactionManager();
        m_transMgr.setSessionFactory(getFactory());
        m_transMgr.afterPropertiesSet();
        
        
        return m_transMgr;
    }

    private void initDatabase(File etcDir) throws IOException, InterruptedException {
        List<File> sqlFiles = new LinkedList<File>();
        sqlFiles.add(new File(etcDir, "create.sql"));

        FileFilter sqlFilter = new FileFilter() {
            public boolean accept(File pathname) {
                return (pathname.getName().startsWith("get") && pathname.getName().endsWith(".sql"))
                || pathname.getName().endsWith("Trigger.sql");
            }
        };

        File[] list = etcDir.listFiles(sqlFilter);
        sqlFiles.addAll(Arrays.asList(list));

         for (File sqlFile : sqlFiles) {
           String cmd = System.getProperty("psql.command", "psql") ;
           System.err.println("psql.command = " + cmd);
           cmd = cmd+" test -U opennms -f \"" + sqlFile.getAbsolutePath() + '"';

           System.err.println("Executing: " + cmd);
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
    }
    
    public int dbQueryForInt(final String sql) {
        return ((Number)getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                SQLQuery query = session.createSQLQuery(sql);
                return query.list().get(0);
            }
            
        })).intValue();
        
        
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
        if (m_lsfbInitialized) {
        	m_factory.close();
            getLsfb().destroy();
        }
    }

    protected LocalSessionFactoryBean getLsfb() {
        return m_lsfb;
    }

    protected void setLsfb(LocalSessionFactoryBean lsfb) {
        m_lsfb = lsfb;
    }
    
    private HibernateTemplate getHibernateTemplate() {
        HibernateTemplate template = new HibernateTemplate(getFactory());
        template.setFlushMode(HibernateTemplate.FLUSH_EAGER);
        return template;
        
    }

    protected DistPollerDao createDistPollerDao() {
        DistPollerDaoHibernate dpDao = new DistPollerDaoHibernate();
        dpDao.setHibernateTemplate(getHibernateTemplate());
        //dpDao.setSessionFactory(getFactory());
        return dpDao;
    }

    protected NodeDao createNodeDao() {
        NodeDaoHibernate nodeDao = new NodeDaoHibernate();
        nodeDao.setHibernateTemplate(getHibernateTemplate());
        //nodeDao.setSessionFactory(getFactory());
        return nodeDao;
    }

    protected IpInterfaceDao createIpInterfaceDao() {
        IpInterfaceDaoHibernate ifDao = new IpInterfaceDaoHibernate();
        ifDao.setHibernateTemplate(getHibernateTemplate());
        //ifDao.setSessionFactory(getFactory());
        return ifDao;
    }

    protected MonitoredServiceDao createMonitoredServiceDao() {
        MonitoredServiceDaoHibernate monSvcDao = new MonitoredServiceDaoHibernate();
        monSvcDao.setHibernateTemplate(getHibernateTemplate());
        //monSvcDao.setSessionFactory(getFactory());
        return monSvcDao;
    }

    protected ServiceTypeDao createServiceTypeDao() {
        ServiceTypeDaoHibernate svcTypeDao = new ServiceTypeDaoHibernate();
        svcTypeDao.setHibernateTemplate(getHibernateTemplate());
        //svcTypeDao.setSessionFactory(getFactory());
        return svcTypeDao;
    }

    protected AssetRecordDao createAssetRecordDao() {
        AssetRecordDaoHibernate arDao = new AssetRecordDaoHibernate();
        arDao.setHibernateTemplate(getHibernateTemplate());
        //arDao.setSessionFactory(getFactory());
        return arDao;
    }

    public void setUsePool(boolean usePool) {
        m_usePool = usePool;
    }

    protected CategoryDao createCategoryDao() {
    	CategoryDaoHibernate catDao = new CategoryDaoHibernate();
        catDao.setHibernateTemplate(getHibernateTemplate());
        //catDao.setSessionFactory(getFactory());
    	return catDao;
    }

	protected SnmpInterfaceDao createSnmpInterfaceDao() {
		SnmpInterfaceDaoHibernate snmpDao = new SnmpInterfaceDaoHibernate();
        snmpDao.setHibernateTemplate(getHibernateTemplate());
        //snmpDao.setSessionFactory(getFactory());
		return snmpDao;
	}

    protected OutageDao createOutageDao() {
    	OutageDaoHibernate outageDao = new OutageDaoHibernate();
        outageDao.setHibernateTemplate(getHibernateTemplate());
        //outageDao.setSessionFactory(getFactory());
    	return outageDao;
    }

	protected EventDao createEventDao() {
		EventDaoHibernate eventDao = new EventDaoHibernate();
        eventDao.setHibernateTemplate(getHibernateTemplate());
        //eventDao.setSessionFactory(getFactory());
		return eventDao;
		
	}
	
    protected AlarmDao createAlarmDao() {
        AlarmDaoHibernate alarmDao = new AlarmDaoHibernate();
        alarmDao.setHibernateTemplate(getHibernateTemplate());
        //alarmDao.setSessionFactory(getFactory());
        return alarmDao;
    }

    protected NotificationDao createNotificationDao() {
    	NotificationDaoHibernate notificationDao = new NotificationDaoHibernate();
        notificationDao.setHibernateTemplate(getHibernateTemplate());
        //notificationDao.setSessionFactory(getFactory());
    	return notificationDao;
    }

    protected UserNotificationDao createUserNotificationDao() {
    	UserNotificationDaoHibernate userNotifDao = new UserNotificationDaoHibernate();
        userNotifDao.setHibernateTemplate(getHibernateTemplate());
        //userNotifDao.setSessionFactory(getFactory());
    	return userNotifDao;
    }
    
    protected AvailabilityReportLocatorDao createAvailabilityReportLocatorDao() {
    	AvailabilityReportLocatorDaoHibernate availabilityReportLocatorDao = new AvailabilityReportLocatorDaoHibernate();
    	availabilityReportLocatorDao.setHibernateTemplate(getHibernateTemplate());
        //userNotifDao.setSessionFactory(getFactory());
    	return availabilityReportLocatorDao;
    }

	@Override
	public void prePopulate() {
        if (!m_createDb) return;
        OnmsDistPoller distPoller = getDistPollerDao().get("localhost");
        if (distPoller == null) return;
        getDistPollerDao().delete(distPoller); 
	}
    

}
