/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.reporting.availability;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.dao.AcknowledgmentDao;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.NotificationDao;
import org.opennms.netmgt.dao.OnmsMapDao;
import org.opennms.netmgt.dao.OnmsMapElementDao;
import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.dao.UserNotificationDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;

/**
 * <p>Populates a test database with some entities (nodes, interfaces, services). Example usage:</p>
 * 
 * <pre>
 * private AvailabilityDatabasePopulator m_populator;
 *
 * @Override
 * protected String[] getConfigLocations() {
 *     return new String[] {
 *         "classpath:/META-INF/opennms/applicationContext-dao.xml",
 *         "classpath:/META-INF/opennms/applicationContext-availabilityDatabasePopulator.xml"
 *     };
 * }
 * 
 * @Override
 * protected void onSetUpInTransactionIfEnabled() {
 *     m_populator.populateDatabase();
 * }
 * 
 * public void setPopulator(AvailabilityDatabasePopulator populator) {
 *     m_populator = populator;
 * }
 * </pre>
 * 
 * <p>Copied from {@link org.opennms.netmgt.dao.DatabasePopulator}</p>.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class AvailabilityDatabasePopulator {
    private DistPollerDao m_distPollerDao;
    private NodeDao m_nodeDao;
    private IpInterfaceDao m_ipInterfaceDao;
    private SnmpInterfaceDao m_snmpInterfaceDao;
    private MonitoredServiceDao m_monitoredServiceDao;
    private ServiceTypeDao m_serviceTypeDao;
    private AssetRecordDao m_assetRecordDao;
    private CategoryDao m_categoryDao;
    private OutageDao m_outageDao;
    private EventDao m_eventDao;
    private AlarmDao m_alarmDao;
    private NotificationDao m_notificationDao;
    private UserNotificationDao m_userNotificationDao;
    private LocationMonitorDao m_locationMonitorDao;
    private OnmsMapDao m_onmsMapDao;
    private OnmsMapElementDao m_onmsMapElementDao;
    private DataLinkInterfaceDao m_dataLinkInterfaceDao;
    private AcknowledgmentDao m_acknowledgmentDao;
    
    private OnmsNode m_node1;

    public void populateDatabase() {
        OnmsDistPoller distPoller = getDistPoller("localhost", "127.0.0.1");
        
        OnmsCategory ac = getCategory("DEV_AC");
        OnmsCategory mid = getCategory("IMP_mid");
        OnmsCategory ops = getCategory("OPS_Online");
        
        OnmsCategory catRouter = getCategory("Routers");
        @SuppressWarnings("unused")
        OnmsCategory catSwitches = getCategory("Switches");
        OnmsCategory catServers = getCategory("Servers");
        getCategory("Production");
        getCategory("Test");
        getCategory("Development");
        
        getServiceType("ICMP");
        getServiceType("SNMP");
        getServiceType("HTTP");
        
//      m_db.update("insert into node (nodeID, nodelabel, nodeCreateTime, nodeType) values (1,'test1.availability.opennms.org','2004-03-01 09:00:00','A')");
//      m_db.update("insert into node (nodeID, nodelabel, nodeCreateTime, nodeType) values (2,'test2.availability.opennms.org','2004-03-01 09:00:00','A')");
//
//      m_db.update("insert into service (serviceid, servicename) values\n"
//              + "(1, 'ICMP');");
//      m_db.update("insert into service (serviceid, servicename) values\n"
//              + "(2, 'HTTP');");
//      m_db.update("insert into service (serviceid, servicename) values\n"
//              + "(3, 'SNMP');");
//
//      m_db.update("insert into ipinterface (id, nodeid, ipaddr, ismanaged) values\n"
//              + "(1, 1,'192.168.100.1','M');");
//      m_db.update("insert into ipinterface (id, nodeid, ipaddr, ismanaged) values\n"
//              + "(2, 2,'192.168.100.2','M');");
//      m_db.update("insert into ipinterface (id, nodeid, ipaddr, ismanaged) values\n"
//              + "(3, 2,'192.168.100.3','M');");
//
//      m_db.update("insert into ifservices (nodeid, ipaddr, serviceid, status, ipInterfaceId) values "
//              + "(1,'192.168.100.1',1,'A', 1);");
//      m_db.update("insert into ifservices (nodeid, ipaddr, serviceid, status, ipInterfaceId) values "
//              + "(2,'192.168.100.2',1,'A', 2);");
//      /*
//       * m_db.update("insert into ifservices (nodeid, ipaddr, serviceid,
//       * status, ipInterfaceId) values " + "(2,'192.168.100.2',2,'A', 2);");
//       */
//      m_db.update("insert into ifservices (nodeid, ipaddr, serviceid, status, ipInterfaceId) values "
//              + "(2,'192.168.100.3',1,'A', 3);");
        
        NetworkBuilder builder = new NetworkBuilder(distPoller);
        
        setNode1(builder.addNode("test1.availability.opennms.org").
                 setId(1).
                 setType("A").
                 getNode());
        Assert.assertNotNull("newly built node 1 should not be null", getNode1());
        builder.addCategory(ac);
        builder.addCategory(mid);
        builder.addCategory(ops);
        builder.addCategory(catRouter); 
        builder.setBuilding("HQ");
        builder.addInterface("192.168.100.1").setIsManaged("M");
        //getNodeDao().save(builder.getCurrentNode());
        //getNodeDao().flush();
        builder.addService(getServiceType("ICMP")).setStatus("A");
        getNodeDao().save(builder.getCurrentNode());
        getNodeDao().flush();
        
        builder.addNode("test2.availability.opennms.org").
            setId(2).
            //setForeignSource("imported:").
            
            //setForeignId("2").
            setType("A");
        builder.addCategory(mid);
        builder.addCategory(catServers);
        builder.setBuilding("HQ");
        builder.addInterface("192.168.100.2").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(getServiceType("ICMP")).setStatus("A");
        //builder.addService(getServiceType("SNMP")).setStatus("A");;
        builder.addInterface("192.168.100.3").setIsManaged("M");
        builder.addService(getServiceType("ICMP")).setStatus("A");
        //builder.addService(getServiceType("HTTP")).setStatus("A");
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
        
//      m_db.update("insert into outages (outageid, nodeid, ipaddr, serviceid, ifLostService, ifRegainedService) values "
//      + "(1,1,'192.168.100.1',1,'2005-05-01 09:00:00','2005-05-01 09:30:00');");
//m_db.update("insert into outages (outageid, nodeid, ipaddr, serviceid, ifLostService, ifRegainedService) values "
//      + "(2,2,'192.168.100.2',1,'2005-05-01 10:00:00','2005-05-02 10:00:00');");
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            OnmsMonitoredService node1If1Svc1 = getMonitoredServiceDao().get(1, InetAddressUtils.addr("192.168.100.1"), "ICMP");
            OnmsMonitoredService node2If1Svc1 = getMonitoredServiceDao().get(2, InetAddressUtils.addr("192.168.100.2"), "ICMP");
            @SuppressWarnings("unused")
            OnmsMonitoredService node2If1Svc2 = getMonitoredServiceDao().get(2, InetAddressUtils.addr("192.168.100.2"), "SNMP");
            OnmsMonitoredService node2If2Svc1 = getMonitoredServiceDao().get(2, InetAddressUtils.addr("192.168.100.3"), "ICMP");
            @SuppressWarnings("unused")
            OnmsMonitoredService node2If2Svc2 = getMonitoredServiceDao().get(2, InetAddressUtils.addr("192.168.100.3"), "HTTP");
            OnmsOutage outage1 = new OnmsOutage(df.parse("2005-05-01 09:00:00"), df.parse("2005-05-01 09:30:00"), event, event, node1If1Svc1, null, null);
            getOutageDao().save(outage1);
            getOutageDao().flush();
            OnmsOutage outage2 = new OnmsOutage(df.parse("2005-05-01 10:00:00"),df.parse("2005-05-02 10:00:00"), event, event, node2If1Svc1, null, null);
            getOutageDao().save(outage2);
            getOutageDao().flush();
            
            // test data for LastMonthsDailyAvailability report
//          // insert 30 minute outage on one node - 99.3056% availability
//          m_db.update("insert into outages (outageid, nodeid, ipaddr, serviceid, ifLostService, ifRegainedService) values "
//                  + "(3,1,'192.168.100.1',1,'2005-04-02 10:00:00','2005-04-02 10:30:00');");
            OnmsOutage outage3 = new OnmsOutage(df.parse("2005-04-02 10:00:00"),df.parse("2005-04-02 10:30:00"), event, event, node1If1Svc1, null, null);
            getOutageDao().save(outage3);
            getOutageDao().flush();
//          // insert 60 minute outage on one interface and 59 minute outages on
//          // another - 97.2454
//          m_db.update("insert into outages (outageid, nodeid, ipaddr, serviceid, ifLostService, ifRegainedService) values "
//                  + "(4,1,'192.168.100.1',1,'2005-04-03 11:30:00','2005-04-03 12:30:00');");
            OnmsOutage outage4 = new OnmsOutage(df.parse("2005-04-03 11:30:00"),df.parse("2005-04-03 12:30:00"), event, event, node1If1Svc1, null, null);
            getOutageDao().save(outage4);
            getOutageDao().flush();
//          m_db.update("insert into outages (outageid, nodeid, ipaddr, serviceid, ifLostService, ifRegainedService) values "
//                  + "(5,2,'192.168.100.2',1,'2005-04-03 23:00:00','2005-04-03 23:59:00');");
            OnmsOutage outage5 = new OnmsOutage(df.parse("2005-04-03 23:00:00"),df.parse("2005-04-03 23:59:00"), event, event, node2If1Svc1, null, null);
            getOutageDao().save(outage5);
            getOutageDao().flush();
//          // test an outage that spans 60 minutes across midnight - 99.3056% on
//          // each day, well, not exactly
//          // its 29 minutes 99.3059 on the fist day and 31 minutes 99.3052 on
//          // the second.
//          m_db.update("insert into outages (outageid, nodeid, ipaddr, serviceid, ifLostService, ifRegainedService) values "
//                  + "(6,2,'192.168.100.3',1,'2005-04-04 23:30:00','2005-04-05 00:30:00');");
            OnmsOutage outage6 = new OnmsOutage(df.parse("2005-04-04 23:30:00"),df.parse("2005-04-05 00:30:00"), event, event, node2If2Svc1, null, null);
            getOutageDao().save(outage6);
            getOutageDao().flush();
            
        } catch (final ParseException e) {
            LogUtils.warnf(this, e, "populating database failed");
        }
        
  
        
    }

    private OnmsCategory getCategory(String categoryName) {
        OnmsCategory cat = getCategoryDao().findByName(categoryName);
        if (cat == null) {
            cat = new OnmsCategory(categoryName);
            cat.getAuthorizedGroups().add(categoryName+"Group");
            getCategoryDao().save(cat);
            getCategoryDao().flush();
        }
        return cat;
    }

    private OnmsDistPoller getDistPoller(String localhost, String localhostIp) {
        OnmsDistPoller distPoller = getDistPollerDao().get(localhost);
        if (distPoller == null) {
            distPoller = new OnmsDistPoller(localhost, localhostIp);
            getDistPollerDao().save(distPoller);
            getDistPollerDao().flush();
        }
        return distPoller;
    }

    private OnmsServiceType getServiceType(String name) {
        OnmsServiceType serviceType = getServiceTypeDao().findByName(name);
        if (serviceType == null) {
            serviceType = new OnmsServiceType(name);
            getServiceTypeDao().save(serviceType);
            getServiceTypeDao().flush();
        }
        return serviceType;
    }

    
    public AlarmDao getAlarmDao() {
        return m_alarmDao;
    }


    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }


    public AssetRecordDao getAssetRecordDao() {
        return m_assetRecordDao;
    }


    public void setAssetRecordDao(AssetRecordDao assetRecordDao) {
        m_assetRecordDao = assetRecordDao;
    }

    
    public CategoryDao getCategoryDao() {
        return m_categoryDao;
    }


    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
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


    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }


    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }


    public MonitoredServiceDao getMonitoredServiceDao() {
        return m_monitoredServiceDao;
    }


    public void setMonitoredServiceDao(MonitoredServiceDao monitoredServiceDao) {
        m_monitoredServiceDao = monitoredServiceDao;
    }


    public NodeDao getNodeDao() {
        return m_nodeDao;
    }


    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }


    public NotificationDao getNotificationDao() {
        return m_notificationDao;
    }


    public void setNotificationDao(NotificationDao notificationDao) {
        m_notificationDao = notificationDao;
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


    public SnmpInterfaceDao getSnmpInterfaceDao() {
        return m_snmpInterfaceDao;
    }


    public void setSnmpInterfaceDao(SnmpInterfaceDao snmpInterfaceDao) {
        m_snmpInterfaceDao = snmpInterfaceDao;
    }


    public UserNotificationDao getUserNotificationDao() {
        return m_userNotificationDao;
    }


    public void setUserNotificationDao(UserNotificationDao userNotificationDao) {
        m_userNotificationDao = userNotificationDao;
    }
    
    public OnmsNode getNode1() {
        return m_node1;
    }
    
    private void setNode1(OnmsNode node1) {
        m_node1 = node1;
    }

    public LocationMonitorDao getLocationMonitorDao() {
        return m_locationMonitorDao;
    }

    public void setLocationMonitorDao(LocationMonitorDao locationMonitorDao) {
        m_locationMonitorDao = locationMonitorDao;
    }

    public OnmsMapDao getOnmsMapDao() {
        return m_onmsMapDao;
    }

    public void setOnmsMapDao(OnmsMapDao onmsMapDao) {
        this.m_onmsMapDao = onmsMapDao;
    }

    public OnmsMapElementDao getOnmsMapElementDao() {
        return m_onmsMapElementDao;
    }

    public void setOnmsMapElementDao(OnmsMapElementDao onmsMapElementDao) {
        this.m_onmsMapElementDao = onmsMapElementDao;
    }

    public DataLinkInterfaceDao getDataLinkInterfaceDao() {
        return m_dataLinkInterfaceDao;
    }

    public void setDataLinkInterfaceDao(DataLinkInterfaceDao dataLinkInterfaceDao) {
        this.m_dataLinkInterfaceDao = dataLinkInterfaceDao;
    }
    
    public AcknowledgmentDao getAcknowledgmentDao() {
        return m_acknowledgmentDao;
    }

    public void setAcknowledgmentDao(AcknowledgmentDao acknowledgmentDao) {
        m_acknowledgmentDao = acknowledgmentDao;
    }

}
