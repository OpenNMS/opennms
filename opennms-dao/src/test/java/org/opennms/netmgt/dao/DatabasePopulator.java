/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2008 Oct 04: Use new OnmsSeverity object when setting alarm severity. - dj@opennms.org
 * 
 * Created: April 5, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.dao;

import java.util.Date;

import junit.framework.Assert;

import org.opennms.netmgt.dao.hibernate.LocationMonitorDaoHibernate;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMap;
import org.opennms.netmgt.model.OnmsMapElement;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsUserNotification;

/**
 * Populates a test database with some entities (nodes, interfaces, services).
 * 
 * Example usage:
 * <pre>
 * private DatabasePopulator m_populator;
 *
 * @Override
 * protected String[] getConfigLocations() {
 *     return new String[] {
 *         "classpath:/META-INF/opennms/applicationContext-dao.xml",
 *         "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
 *     };
 * }
 * 
 * @Override
 * protected void onSetUpInTransactionIfEnabled() {
 *     m_populator.populateDatabase();
 * }
 * 
 * public void setPopulator(DatabasePopulator populator) {
 *     m_populator = populator;
 * }
 * </pre>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class DatabasePopulator {
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
    private LocationMonitorDaoHibernate m_locationMonitorDao;
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
        OnmsCategory catSwitches = getCategory("Switches");
        OnmsCategory catServers = getCategory("Servers");
        getCategory("Production");
        getCategory("Test");
        getCategory("Development");
        
        getServiceType("ICMP");
        getServiceType("SNMP");
        getServiceType("HTTP");
        
        NetworkBuilder builder = new NetworkBuilder(distPoller);
        
        setNode1(builder.addNode("node1").setForeignSource("imported:").setForeignId("1").getNode());
        Assert.assertNotNull("newly built node 1 should not be null", getNode1());
        builder.addCategory(ac);
        builder.addCategory(mid);
        builder.addCategory(ops);
        builder.addCategory(catRouter); 
        builder.setBuilding("HQ");
        builder.addInterface("192.168.1.1").setIsManaged("M").setIsSnmpPrimary("P").addSnmpInterface("192.168.1.1", 1)
            .setCollectionEnabled(true)
            .setIfOperStatus(1)
            .setIfSpeed(10000000)
            .setIfDescr("ATM0")
            .setIfType(37);
        //getNodeDao().save(builder.getCurrentNode());
        //getNodeDao().flush();
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("SNMP"));
        builder.addInterface("192.168.1.2").setIsManaged("M").setIsSnmpPrimary("S").addSnmpInterface("192.168.1.2", 2)
            .setCollectionEnabled(true)
            .setIfOperStatus(1)
            .setIfSpeed(10000000)
            .setIfName("eth0")
            .setIfType(6);
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("HTTP"));
        builder.addInterface("192.168.1.3").setIsManaged("M").setIsSnmpPrimary("N").addSnmpInterface("192.168.1.3", 3)
            .setCollectionEnabled(false)
            .setIfOperStatus(1)
            .setIfSpeed(10000000);
        builder.addService(getServiceType("ICMP"));
        builder.addInterface("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%5").setIsManaged("M").setIsSnmpPrimary("N").addSnmpInterface("", 4)
        .setCollectionEnabled(false)
        .setIfOperStatus(1)
        .setIfSpeed(10000000);
        builder.addService(getServiceType("ICMP"));
        getNodeDao().save(builder.getCurrentNode());
        getNodeDao().flush();
        
        builder.addNode("node2").setForeignSource("imported:").setForeignId("2");
        builder.addCategory(mid);
        builder.addCategory(catServers);
        builder.setBuilding("HQ");
        builder.addInterface("192.168.2.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("SNMP"));
        builder.addInterface("192.168.2.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("HTTP"));
        builder.addInterface("192.168.2.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(getServiceType("ICMP"));
        getNodeDao().save(builder.getCurrentNode());
        getNodeDao().flush();
        
        builder.addNode("node3").setForeignSource("imported:").setForeignId("3");
        builder.addCategory(ops);
        builder.addInterface("192.168.3.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("SNMP"));
        builder.addInterface("192.168.3.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("HTTP"));
        builder.addInterface("192.168.3.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(getServiceType("ICMP"));
        getNodeDao().save(builder.getCurrentNode());
        getNodeDao().flush();
        
        builder.addNode("node4").setForeignSource("imported:").setForeignId("4");
        builder.addCategory(ac);
        builder.addInterface("192.168.4.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("SNMP"));
        builder.addInterface("192.168.4.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("HTTP"));
        builder.addInterface("192.168.4.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(getServiceType("ICMP"));
        getNodeDao().save(builder.getCurrentNode());
        getNodeDao().flush();

        //This node purposely doesn't have a foreignId style assetNumber
        builder.addNode("alternate-node1").getAssetRecord().setAssetNumber("5");
        builder.addCategory(ac);
        builder.addCategory(catSwitches);
        builder.addInterface("10.1.1.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("SNMP"));
        builder.addInterface("10.1.1.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("HTTP"));
        builder.addInterface("10.1.1.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(getServiceType("ICMP"));
        getNodeDao().save(builder.getCurrentNode());
        getNodeDao().flush();
        
        //This node purposely doesn't have a assetNumber and is used by a test to check the category
        builder.addNode("alternate-node2").getAssetRecord().setDisplayCategory("category1");
        builder.addCategory(ac);
        builder.addInterface("10.1.2.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("SNMP"));
        builder.addInterface("10.1.2.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("HTTP"));
        builder.addInterface("10.1.2.3").setIsManaged("M").setIsSnmpPrimary("N");
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
        
        OnmsNotification notif = new OnmsNotification();
        notif.setEvent(event);
        notif.setTextMsg("This is a test notification");
        notif.setIpAddress("192.168.1.1");
        notif.setNode(m_node1);
        notif.setServiceType(getServiceType("ICMP"));
        getNotificationDao().save(notif);
        getNotificationDao().flush();
       
        OnmsUserNotification userNotif = new OnmsUserNotification();
        userNotif.setUserId("TestUser");
        userNotif.setNotification(notif);
        getUserNotificationDao().save(userNotif);
        getUserNotificationDao().flush();
        
        OnmsUserNotification userNotif2 = new OnmsUserNotification();
        userNotif2.setUserId("TestUser2");
        userNotif2.setNotification(notif);
        getUserNotificationDao().save(userNotif2);
        getUserNotificationDao().flush();
        
        OnmsMonitoredService svc = getMonitoredServiceDao().get(1, "192.168.1.1", "SNMP");
        OnmsOutage resolved = new OnmsOutage(new Date(), new Date(), event, event, svc, null, null);
        getOutageDao().save(resolved);
        getOutageDao().flush();
        
        OnmsOutage unresolved = new OnmsOutage(new Date(), event, svc);
        getOutageDao().save(unresolved);
        getOutageDao().flush();
        
        OnmsCategory category = new OnmsCategory();
        category.setName("some category");
        getCategoryDao().save(category);
        getCategoryDao().flush();
        
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setDistPoller(getDistPollerDao().load("localhost"));
        alarm.setUei(event.getEventUei());
        alarm.setAlarmType(1);
        alarm.setNode(m_node1);
        alarm.setDescription("This is a test alarm");
        alarm.setLogMsg("this is a test alarm log message");
        alarm.setCounter(1);
        alarm.setIpAddr("192.168.1.1");
        alarm.setSeverity(OnmsSeverity.NORMAL);
        alarm.setFirstEventTime(event.getEventTime());
        alarm.setLastEvent(event);
        getAlarmDao().save(alarm);
        getAlarmDao().flush();
        
        OnmsMap map = new OnmsMap("DB_Pop_Test_Map", "admin");
        map.setBackground("fake_background.jpg");
        map.setAccessMode(OnmsMap.ACCESS_MODE_ADMIN);
        map.setType(OnmsMap.USER_GENERATED_MAP);
        map.setMapGroup("admin");
        getOnmsMapDao().save(map);
        getOnmsMapDao().flush();

        OnmsMapElement mapElement = new OnmsMapElement(map, 1,
                OnmsMapElement.NODE_TYPE,
                "Test Node",
                OnmsMapElement.defaultNodeIcon,
                0,
                10);
        getOnmsMapElementDao().save(mapElement);
        getOnmsMapElementDao().flush();

        DataLinkInterface dli = new DataLinkInterface(1, 1, 1, 1, "A", new Date());
        getDataLinkInterfaceDao().save(dli);
        getDataLinkInterfaceDao().flush();

        DataLinkInterface dli2 = new DataLinkInterface(1, 2, 1, 1, "A", new Date());
        getDataLinkInterfaceDao().save(dli2);
        getDataLinkInterfaceDao().flush();

        DataLinkInterface dli3 = new DataLinkInterface(2, 1, 1, 1, "A", new Date());
        getDataLinkInterfaceDao().save(dli3);
        getDataLinkInterfaceDao().flush();
        
        OnmsAcknowledgment ack = new OnmsAcknowledgment();
        ack.setAckTime(new Date());
        ack.setAckType(AckType.UNSPECIFIED);
        ack.setAckAction(AckAction.UNSPECIFIED);
        ack.setAckUser("admin");
        getAcknowledgmentDao().save(ack);
        getAcknowledgmentDao().flush();
        
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

    public LocationMonitorDaoHibernate getLocationMonitorDao() {
        return m_locationMonitorDao;
    }

    public void setLocationMonitorDao(LocationMonitorDaoHibernate locationMonitorDao) {
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
