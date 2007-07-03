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
// Modifications:
//
// 2007 Jul 03: Remove unused field m_jdbcTemplate. - dj@opennms.org
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

import java.beans.PropertyVetoException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.Map;

import javax.sql.DataSource;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class AbstractDaoTestCase extends TestCase {
 
    private class RunTestInTransaction implements TransactionCallback {
        public Object doInTransaction(TransactionStatus ts) {
            try {
                AbstractDaoTestCase.super.runTest();
            } catch (RuntimeException e) {
                ts.setRollbackOnly();
                throw e;
            } catch (AssertionFailedError e) {
                ts.setRollbackOnly();
                throw e;
            } catch (Throwable e) {
                ts.setRollbackOnly();
                e.printStackTrace();
                fail("Unexpected throwable! "+e);
            }
            return null;
        }
    }
    
    private class StoreExceptionUncaughtExceptionHandler
        implements UncaughtExceptionHandler {
        
        private Throwable m_throwable = null;

        public void uncaughtException(Thread t, Throwable e) {
            m_throwable = e;
        }
        
        public Throwable getThrowable() {
            return m_throwable;
        }
        
        public boolean hasThrown() {
            return (m_throwable != null);
        }
    
    }
    
    abstract class DB {
        public abstract void createDatabase() throws Exception;
        public abstract void dropDatabase() throws Exception;
        public abstract DataSource getDataSource();
        public abstract DataSource getPoolingDataSource();
        public abstract String getHibernateDialect();
        
        protected void initializeDatabase() throws Exception {
//            JdbcTemplate template = new JdbcTemplate(getDataSource());
//            InputStream stream = getClass().getResourceAsStream("/create.sql");
//            String sql = IOUtils.toString(stream);
//            sql = sql.replaceAll("--.*\n", "");
//            System.out.println(sql);
//            template.execute(sql);
        }
    }
    
    class PostgresqlDB extends DB {

        public void createDatabase() throws Exception {
            JdbcTemplate template = new JdbcTemplate(getAdminDataSource());
            template.execute("create database test");
            
            initializeDatabase();
            
        }

        public void dropDatabase() {
            try {
                JdbcTemplate template = new JdbcTemplate(getAdminDataSource());
                template.execute("drop database test");
            } catch (Exception e) {
                // XXX should we re-throw?
                MockUtil.println("Error dropping Database: "+e);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // ignore
            }    
        }

        public DataSource getDataSource() {
            
            PGSimpleDataSource dataSource = new PGSimpleDataSource();
            dataSource.setDatabaseName("test");
            dataSource.setServerName("localhost");
            dataSource.setUser("opennms");
            dataSource.setPassword("opennms");

            return dataSource;
        }
        
        public DataSource getPoolingDataSource() {
            ComboPooledDataSource pds = new ComboPooledDataSource();
            try {
                pds.setDriverClass("org.postgresql.Driver");
            } catch (PropertyVetoException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            pds.setJdbcUrl("jdbc:postgresql://localhost:5432/test");
            //pds.setJdbcUrl("jdbc:postgresql://localhost:5432/test?loglevel=2");
            pds.setPassword("opennms");
            pds.setUser("opennms");
            return pds;
        }
        
        private DataSource getAdminDataSource() {
            PGSimpleDataSource dataSource = new PGSimpleDataSource();
            dataSource.setServerName("localhost");
            dataSource.setDatabaseName("template1");
            dataSource.setUser("opennms");
            dataSource.setPassword("opennms");

            return dataSource;
        }

        public String getHibernateDialect() {
            return "org.hibernate.dialect.PostgreSQLDialect";
        }
        
    }
    

    protected TransactionTemplate m_transTemplate;
    DaoTestConfig m_testConfig;
    
    protected DB m_db;
    protected boolean m_populate = true;
    protected boolean m_runTestsInTransaction = true;
    private boolean m_createDb = true;
	protected OnmsNode m_node1;

    public boolean isRunTestsInTransaction() {
        return m_runTestsInTransaction;
    }

    public void setRunTestsInTransaction(boolean runTestsInTransaction) {
        m_runTestsInTransaction = runTestsInTransaction;
    }

    public boolean isCreateDb() {
        return m_createDb;
    }

    public void setCreateDb(boolean createDb) {
        m_createDb = createDb;
    }

    @Override
    protected void setUp() throws Exception {
        MockUtil.println("----------- Begin SetUp for "+getName()+" ---------------------");
        MockLogAppender.setupLogging();

        m_db = new PostgresqlDB();
        
        if (isCreateDb()) {
            m_db.dropDatabase();
            m_db.createDatabase();
        }
        
        m_testConfig = new HibernateDaoTestConfig();
        PlatformTransactionManager m_transMgr = m_testConfig.setUp(m_db, isCreateDb());

        m_transTemplate = new TransactionTemplate();
        m_transTemplate.setTransactionManager(m_transMgr);
        m_transTemplate.afterPropertiesSet();

        if (isPopulate()) {
            populateDB();
        }
        
        MockUtil.println("----------- SetUp Complete for "+getName()+" ---------------------");
    }

    @Override
    protected void tearDown() throws Exception {
        MockUtil.println("----------- Begin TearDown for "+getName()+" ---------------------");
        m_testConfig.tearDown();
        MockUtil.println("----------- TearDown Complete for "+getName()+" ---------------------");
     }
    
    public int dbQueryForInt(String sql) {
        return m_testConfig.dbQueryForInt(sql);
    }

    
    @Override
    protected void runTest() throws Throwable {
        if (isRunTestsInTransaction()) {
            m_transTemplate.execute(new RunTestInTransaction());
        } else {
            super.runTest();
        }

        MockLogAppender.assertNoWarningsOrGreater();
        MockUtil.println("------------ End Test "+getName()+" --------------------------");
    }
    
    private void populateDB() throws Exception {
        
        final DistPollerDao dao = getDistPollerDao();
        
        final TransactionCallback populater = new TransactionCallback() {

            public Object doInTransaction(TransactionStatus status) {
                m_testConfig.prePopulate();
                populateDB(dao);
                m_testConfig.postPopulate();
                return null;
            }
            
        };
        
        Runnable doIt = new Runnable() {
            public void run() {
                m_transTemplate.execute(populater);
            }
        };

        Thread t = new Thread(doIt);
        
        StoreExceptionUncaughtExceptionHandler eh =
            new StoreExceptionUncaughtExceptionHandler();
        t.setUncaughtExceptionHandler(eh);
        
        // run in a separate thread so it's in a separate transaction
        t.start();
        t.join();
        
        if (eh.hasThrown()) {
            fail("populateDB failed: " + eh.getThrowable().toString(),
                 eh.getThrowable());
        }
        
    }
    
    public void fail(String message, Throwable t) {
        AssertionFailedError e =
            new AssertionFailedError(message + "; Nested exception is: ["
                                     + t.getClass().getName() + "] "
                                     + t.getMessage());
        e.initCause(t);
        throw e;
    }

    protected DistPollerDao getDistPollerDao() {
        return m_testConfig.getDistPollerDao();
    }
    
    protected NodeDao getNodeDao() {
        return m_testConfig.getNodeDao();
    }
    protected IpInterfaceDao getIpInterfaceDao() {
        return m_testConfig.getIpInterfaceDao();
    }
    protected SnmpInterfaceDao getSnmpInterfaceDao() {
    	return m_testConfig.getSnmpInterfaceDao();
    }
    protected MonitoredServiceDao getMonitoredServiceDao() {
        return m_testConfig.getMonitoredServiceDao();
    }
    protected ServiceTypeDao getServiceTypeDao() {
        return m_testConfig.getServiceTypeDao();
    }
    protected AssetRecordDao getAssetRecordDao() {
        return m_testConfig.getAssetRecordDao();
    }
    protected CategoryDao getCategoryDao() {
        return m_testConfig.getCategoryDao();
    }
    protected OutageDao getOutageDao() {
        return m_testConfig.getOutageDao();
    }
    protected EventDao getEventDao() {
    	return m_testConfig.getEventDao();
    }
    protected AlarmDao getAlarmDao() {
        return m_testConfig.getAlarmDao();
    }
    protected NotificationDao getNotificationDao() {
        return m_testConfig.getNotificationDao();
    }
    protected UserNotificationDao getUserNotificationDao() {
        return m_testConfig.getUserNotificationDao();
    }
    protected AvailabilityReportLocatorDao getAvailabilityReportLocatorDao() {
        return m_testConfig.getAvailabilityReportLocatorDao();
    }
    private void populateDB(DistPollerDao dao) {
        //OnmsDistPoller distPoller = dao.load("localhost");

        getServiceTypeDao().save(new OnmsServiceType("ICMP"));
        getServiceTypeDao().flush();
        getServiceTypeDao().save(new OnmsServiceType("SNMP"));
        getServiceTypeDao().flush();
        getServiceTypeDao().save(new OnmsServiceType("HTTP"));
        getServiceTypeDao().flush();
        
        OnmsDistPoller distPoller = new OnmsDistPoller("localhost", "127.0.0.1");
        getDistPollerDao().save(distPoller);
        getDistPollerDao().flush();
        
        NetworkBuilder builder = new NetworkBuilder(distPoller);
        m_node1 = builder.addNode("node1").getNode();
        m_node1.setForeignSource("imported:");
        m_node1.setForeignId("1");
        builder.addInterface("192.168.1.1").setIsManaged("M").setIsSnmpPrimary("P").setIpStatus(1).addSnmpInterface("192.168.1.1", 1).setIfSpeed(10000000);
        getNodeDao().save(builder.getCurrentNode());
        getNodeDao().flush();
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("SNMP"));
        builder.addInterface("192.168.1.2").setIsManaged("M").setIsSnmpPrimary("S").setIpStatus(1).addSnmpInterface("192.168.1.2", 2).setIfSpeed(10000000);
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("HTTP"));
        builder.addInterface("192.168.1.3").setIsManaged("M").setIsSnmpPrimary("N").setIpStatus(1).addSnmpInterface("192.168.1.3", 3).setIfSpeed(10000000);
        builder.addService(getServiceType("ICMP"));
        getNodeDao().save(builder.getCurrentNode());
        getNodeDao().flush();
        
        builder.addNode("node2").setForeignSource("imported:").setForeignId("2");
        builder.addInterface("192.168.2.1").setIsManaged("M").setIsSnmpPrimary("P").setIpStatus(1);
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("SNMP"));
        builder.addInterface("192.168.2.2").setIsManaged("M").setIsSnmpPrimary("S").setIpStatus(1);
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("HTTP"));
        builder.addInterface("192.168.2.3").setIsManaged("M").setIsSnmpPrimary("N").setIpStatus(1);
        builder.addService(getServiceType("ICMP"));
        getNodeDao().save(builder.getCurrentNode());
        getNodeDao().flush();
        
        builder.addNode("node3").setForeignSource("imported:").setForeignId("3");
        builder.addInterface("192.168.3.1").setIsManaged("M").setIsSnmpPrimary("P").setIpStatus(1);
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("SNMP"));
        builder.addInterface("192.168.3.2").setIsManaged("M").setIsSnmpPrimary("S").setIpStatus(1);
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("HTTP"));
        builder.addInterface("192.168.3.3").setIsManaged("M").setIsSnmpPrimary("N").setIpStatus(1);
        builder.addService(getServiceType("ICMP"));
        getNodeDao().save(builder.getCurrentNode());
        getNodeDao().flush();
        
        builder.addNode("node4").setForeignSource("imported:").setForeignId("4");
        builder.addInterface("192.168.4.1").setIsManaged("M").setIsSnmpPrimary("P").setIpStatus(1);
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("SNMP"));
        builder.addInterface("192.168.4.2").setIsManaged("M").setIsSnmpPrimary("S").setIpStatus(1);
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("HTTP"));
        builder.addInterface("192.168.4.3").setIsManaged("M").setIsSnmpPrimary("N").setIpStatus(1);
        builder.addService(getServiceType("ICMP"));
        getNodeDao().save(builder.getCurrentNode());
        getNodeDao().flush();

        //This node purposely doesn't have a foreignId style assetNumber
        builder.addNode("alternate-node1").getAssetRecord().setAssetNumber("5");
        builder.addInterface("10.1.1.1").setIsManaged("M").setIsSnmpPrimary("P").setIpStatus(1);
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("SNMP"));
        builder.addInterface("10.1.1.2").setIsManaged("M").setIsSnmpPrimary("S").setIpStatus(1);
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("HTTP"));
        builder.addInterface("10.1.1.3").setIsManaged("M").setIsSnmpPrimary("N").setIpStatus(1);
        builder.addService(getServiceType("ICMP"));
        getNodeDao().save(builder.getCurrentNode());
        getNodeDao().flush();
        
        //This node purposely doesn't have a assetNumber and is used by a test to check the category
        builder.addNode("alternate-node2").getAssetRecord().setDisplayCategory("category1");
        builder.addInterface("10.1.2.1").setIsManaged("M").setIsSnmpPrimary("P").setIpStatus(1);
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("SNMP"));
        builder.addInterface("10.1.2.2").setIsManaged("M").setIsSnmpPrimary("S").setIpStatus(1);
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("HTTP"));
        builder.addInterface("10.1.2.3").setIsManaged("M").setIsSnmpPrimary("N").setIpStatus(1);
        builder.addService(getServiceType("ICMP"));
        getNodeDao().save(builder.getCurrentNode());
        getNodeDao().flush();
        
        OnmsEvent event = new OnmsEvent();
        event.setDistPoller(distPoller);
        event.setEventUei("uei.opennms.org/test");
        event.setEventTime(new Date());
        event.setEventSource("test");
        event.setEventCreateTime(new Date());
        event.setEventSeverity(1);
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        getEventDao().save(event);
        getEventDao().flush();
       
        OnmsMonitoredService svc = getMonitoredServiceDao().get(1, "192.168.1.1", "SNMP");
        OnmsOutage resolved = new OnmsOutage(new Date(), new Date(), event, event, svc, null, null);
        getOutageDao().save(resolved);
        getOutageDao().flush();
        
        OnmsOutage unresolved = new OnmsOutage(new Date(), event, svc);
        getOutageDao().save(unresolved);
        getOutageDao().flush();
        

    }

    private OnmsServiceType getServiceType(String svcName) {
        OnmsServiceType svcType = getServiceTypeDao().findByName(svcName);
        return svcType;
    }
    
    public boolean isPopulate() {
        return m_populate;
    }

    public void setPopulate(boolean populate) {
        m_populate = populate;
    }

    protected Map<String, Integer> getAssetNumberMap(String foreignSource) {
//        Map assetNumberMap = new HashMap();
//        assetNumberMap.put(PopulatingVisitor.IMPORTED_ID+"1", new Long(1));
//        assetNumberMap.put(PopulatingVisitor.IMPORTED_ID+"2", new Long(2));
//        assetNumberMap.put(PopulatingVisitor.IMPORTED_ID+"3", new Long(3));
//        assetNumberMap.put(PopulatingVisitor.IMPORTED_ID+"4", new Long(4));
//        return assetNumberMap;
        
        return getNodeDao().getForeignIdToNodeIdMap(foreignSource);
    }

    protected void expectServiceTypeCreate(String string) {
        // TODO Auto-generated method stub
        
    }

    protected void setDeleteExpectations(int i) {
        // TODO Auto-generated method stub
        
    }
    
    public OnmsNode getNode1() {
        return m_node1;
    }
    
}
