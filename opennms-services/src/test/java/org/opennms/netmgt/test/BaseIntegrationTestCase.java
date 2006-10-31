package org.opennms.netmgt.test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.opennms.core.utils.ProcessExec;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

public abstract class BaseIntegrationTestCase extends AbstractDependencyInjectionSpringContextTests {

    private ServiceTypeDao m_serviceTypeDao;
    private NodeDao m_nodeDao;
    private DistPollerDao m_distPollerDao;
    private EventDao m_eventDao;
    private OutageDao m_outageDao;
    private MonitoredServiceDao m_monitoredServiceDao;
    private TransactionTemplate m_transTemplate;
    private String m_databaseName;
    
    private boolean m_creatingDbOnce = false;
    private boolean m_dbCreated = false;
    private boolean m_dbPopulated = false;

    protected JdbcTemplate jdbcTemplate;

    
    protected void setCreateDbOnce(boolean createDbOnce) {
        m_creatingDbOnce = createDbOnce;
    }
    
    protected void beforeSetUp() throws Exception {
        if (!m_dbCreated || !m_creatingDbOnce) {
            if (m_databaseName == null) {
                m_databaseName = "test";
            }
            initDatabase(new File("../opennms-daemon/src/main/filtered/etc"));
            m_dbCreated = true;
        }
    }
    
    
    
    
    @Override
    protected void onSetUp() throws Exception {
        if (!m_dbPopulated || !m_creatingDbOnce) {
            System.err.println("Populating Database");
            populateDatabase();
            m_dbPopulated = true;
        }
    }




    @Override
    public void runBare() throws Throwable {
        beforeSetUp();
        super.runBare();
    }
    
    
    @Override
    protected void runTest() throws Throwable {
        try {
            super.runTest();
        } finally {
            if (!m_creatingDbOnce) {
                setDirty();
            }
        }
    }

    protected void setDatabaseName(String dbName) {
        m_databaseName = dbName;
    }
    
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    protected int queryForInt(String sql, Object... args) {
       return jdbcTemplate.queryForInt(sql, args); 
    }
    
    public void setTransactionTemplate(TransactionTemplate transTemplate) {
        m_transTemplate = transTemplate;
    }
    
    public DistPollerDao getDistPollerDao() {
        return m_distPollerDao;
    }


    public void setDistPollerDao(DistPollerDao distPollerDao) {
        m_distPollerDao = distPollerDao;
    }


    public EventDao getEventDao() {
        return m_eventDao;
    }


    public void setEventDao(EventDao eventDao) {
        m_eventDao = eventDao;
    }


    public NodeDao getNodeDao() {
        return m_nodeDao;
    }


    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }


    public OutageDao getOutageDao() {
        return m_outageDao;
    }


    public void setOutageDao(OutageDao outageDao) {
        m_outageDao = outageDao;
    }


    public ServiceTypeDao getServiceTypeDao() {
        return m_serviceTypeDao;
    }


    public void setServiceTypeDao(ServiceTypeDao serviceTypeDao) {
        m_serviceTypeDao = serviceTypeDao;
    }
    
    private void populateDatabase() {
        m_transTemplate.execute(new TransactionCallback() {

            public Object doInTransaction(TransactionStatus arg0) {
                doPopulateDatabase();
                return null;
            }
            
        });
    }


    private void doPopulateDatabase() {

        getServiceTypeDao().save(new OnmsServiceType("ICMP"));
        getServiceTypeDao().flush();
        getServiceTypeDao().save(new OnmsServiceType("SNMP"));
        getServiceTypeDao().flush();
        getServiceTypeDao().save(new OnmsServiceType("HTTP"));
        getServiceTypeDao().flush();
        
        
        OnmsDistPoller distPoller = getDistPollerDao().get("localhost");
        if (distPoller == null) {
            distPoller = new OnmsDistPoller("localhost", "127.0.0.1");
            getDistPollerDao().save(distPoller);
            getDistPollerDao().flush();
        }
        
        NetworkBuilder builder = new NetworkBuilder(distPoller);
        builder.addNode("node1").setForeignSource("imported:").setForeignId("1");
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
        
        builder.addNode("google.com");
        builder.addInterface("72.14.207.99").setIsManaged("M").setIsSnmpPrimary("N").setIpStatus(1);
        builder.addService(getServiceType("HTTP"));
        getNodeDao().save(builder.getCurrentNode());
        getNodeDao().flush();
        
        builder.addNode("dave.opennms.com");
        builder.addInterface("172.20.1.171").setIsManaged("M").setIsSnmpPrimary("N").setIpStatus(1);
        builder.addService(getServiceType("HTTP"));
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

    private OnmsServiceType getServiceType(String name) {
        return m_serviceTypeDao.findByName(name);
    }


    protected void initDatabase(File etcDir) throws Exception {
        
        runSql("template1", "drop database "+m_databaseName+";");
        runSql("template1", "create database "+m_databaseName+" with encoding='unicode';");

        List<File> sqlFiles = locateSqlFiles(etcDir);
        for (File sqlFile : sqlFiles) {
            assertTrue(loadSqlFile(m_databaseName, sqlFile));
        }
        
        assertTrue(runSql(m_databaseName, 
                "CREATE OR REPLACE FUNCTION iplike(text, text) RETURNS bool AS ' BEGIN RETURN true; END; ' LANGUAGE 'plpgsql';"));
    }


    protected List<File> locateSqlFiles(File etcDir) {
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
        return sqlFiles;
    }


    protected boolean execute(String[] cmd) throws IOException, InterruptedException {
        ProcessExec proc = new ProcessExec(getOut(), getErr());
        System.err.println("Executing: " + StringUtils.collectionToDelimitedString(Arrays.asList(cmd), " "));
        int exitVal = proc.exec(cmd);

        System.err.println("Got an exitValue of "+exitVal);
        return exitVal == 0;
    }
    
    private class SinkStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {
        }
        
    }
    
    protected PrintStream getOut() {
       return new PrintStream(new SinkStream());
       //return System.out;
    }
    
    protected PrintStream getErr() {
        return new PrintStream(new SinkStream());
        //return System.err;
    }
    
    
    protected boolean runSql(String db, String sql) throws Exception {
        String psql = System.getProperty("psql.command", "psql") ;
        System.err.println("psql.command = " + psql);
        String[] cmd = {
                psql,
                db,
                "-U",
                "opennms",
                "-c",
                sql,
        };
        return execute(cmd);
    }
    
    protected boolean loadSqlFile(String db, File sqlFile) throws Exception {
        String psql = System.getProperty("psql.command", "psql") ;
        System.err.println("psql.command = " + psql);
        String[] cmd = {
                psql,
                db,
                "-U",
                "opennms",
                "-f",
                sqlFile.getAbsolutePath()
        };
        return execute(cmd);
    }


    public MonitoredServiceDao getMonitoredServiceDao() {
        return m_monitoredServiceDao;
    }


    public void setMonitoredServiceDao(MonitoredServiceDao monitoredServiceDao) {
        m_monitoredServiceDao = monitoredServiceDao;
    }



}
