/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao;

import java.util.Date;

import org.opennms.core.utils.InetAddressUtils;
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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

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
    private LocationMonitorDao m_locationMonitorDao;
    private OnmsMapDao m_onmsMapDao;
    private OnmsMapElementDao m_onmsMapElementDao;
    private DataLinkInterfaceDao m_dataLinkInterfaceDao;
    private AcknowledgmentDao m_acknowledgmentDao;
    private TransactionTemplate m_transTemplate;
    
    private OnmsNode m_node1;
    private OnmsNode m_node2;
    private OnmsNode m_node3;
    private OnmsNode m_node4;
    private OnmsNode m_node5;
    private OnmsNode m_node6;
    
    private static boolean POPULATE_DATABASE_IN_SEPARATE_TRANSACTION = true;

    public void populateDatabase() {
        if (POPULATE_DATABASE_IN_SEPARATE_TRANSACTION) {
            m_transTemplate.execute(new TransactionCallback<Object>() {
                public Object doInTransaction(final TransactionStatus status) {
                    doPopulateDatabase();
                    return null;
                }
            });
        } else {
            doPopulateDatabase();
        }
    }

    private void doPopulateDatabase() {
        final OnmsDistPoller distPoller = getDistPoller("localhost", "127.0.0.1");
        
        final OnmsCategory ac = getCategory("DEV_AC");
        final OnmsCategory mid = getCategory("IMP_mid");
        final OnmsCategory ops = getCategory("OPS_Online");
        
        final OnmsCategory catRouter = getCategory("Routers");
        final OnmsCategory catSwitches = getCategory("Switches");
        final OnmsCategory catServers = getCategory("Servers");
        getCategory("Production");
        getCategory("Test");
        getCategory("Development");
        
        getServiceType("ICMP");
        getServiceType("SNMP");
        getServiceType("HTTP");
        
        final NetworkBuilder builder = new NetworkBuilder(distPoller);
        
        setNode1(builder.addNode("node1").setForeignSource("imported:").setForeignId("1").setType("A").getNode());
        builder.addCategory(ac);
        builder.addCategory(mid);
        builder.addCategory(ops);
        builder.addCategory(catRouter); 
        builder.setBuilding("HQ");
        builder.addInterface("192.168.1.1").setIsManaged("M").setIsSnmpPrimary("P").addSnmpInterface(1)
            .setCollectionEnabled(true)
            .setIfOperStatus(1)
            .setIfSpeed(10000000)
            .setIfDescr("ATM0")
            .setIfAlias("Initial ifAlias value")
            .setIfType(37);
        //getNodeDao().save(builder.getCurrentNode());
        //getNodeDao().flush();
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("SNMP"));
        builder.addInterface("192.168.1.2").setIsManaged("M").setIsSnmpPrimary("S").addSnmpInterface(2)
            .setCollectionEnabled(true)
            .setIfOperStatus(1)
            .setIfSpeed(10000000)
            .setIfName("eth0")
            .setIfType(6);
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("HTTP"));
        builder.addInterface("192.168.1.3").setIsManaged("M").setIsSnmpPrimary("N").addSnmpInterface(3)
            .setCollectionEnabled(false)
            .setIfOperStatus(1)
            .setIfSpeed(10000000);
        builder.addService(getServiceType("ICMP"));
        builder.addInterface("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%5").setIsManaged("M").setIsSnmpPrimary("N").addSnmpInterface(4)
            .setCollectionEnabled(false)
            .setIfOperStatus(1)
            .setIfSpeed(10000000);
        builder.addService(getServiceType("ICMP"));
        final OnmsNode node1 = builder.getCurrentNode();
        getNodeDao().save(builder.getCurrentNode());
        getNodeDao().flush();
        
        builder.addNode("node2").setForeignSource("imported:").setForeignId("2").setType("A");
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
        builder.addAtInterface(node1, "192.168.2.1", "AA:BB:CC:DD:EE:FF").setIfIndex(1).setLastPollTime(new Date()).setStatus('A');
        OnmsNode node2 = builder.getCurrentNode();
        getNodeDao().save(node2);
        getNodeDao().flush();
        setNode2(node2);
        
        builder.addNode("node3").setForeignSource("imported:").setForeignId("3").setType("A");
        builder.addCategory(ops);
        builder.addInterface("192.168.3.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("SNMP"));
        builder.addInterface("192.168.3.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("HTTP"));
        builder.addInterface("192.168.3.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(getServiceType("ICMP"));
        OnmsNode node3 = builder.getCurrentNode();
        getNodeDao().save(node3);
        getNodeDao().flush();
        setNode3(node3);
        
        builder.addNode("node4").setForeignSource("imported:").setForeignId("4").setType("A");
        builder.addCategory(ac);
        builder.addInterface("192.168.4.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("SNMP"));
        builder.addInterface("192.168.4.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("HTTP"));
        builder.addInterface("192.168.4.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(getServiceType("ICMP"));
        OnmsNode node4 = builder.getCurrentNode();
        getNodeDao().save(node4);
        getNodeDao().flush();
        setNode4(node4);
        
        //This node purposely doesn't have a foreignId style assetNumber
        builder.addNode("alternate-node1").setType("A").getAssetRecord().setAssetNumber("5");
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
        OnmsNode node5 = builder.getCurrentNode();
        getNodeDao().save(node5);
        getNodeDao().flush();
        setNode5(node5);
        
        //This node purposely doesn't have a assetNumber and is used by a test to check the category
        builder.addNode("alternate-node2").setType("A").getAssetRecord().setDisplayCategory("category1");
        builder.addCategory(ac);
        builder.addInterface("10.1.2.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("SNMP"));
        builder.addInterface("10.1.2.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(getServiceType("ICMP"));
        builder.addService(getServiceType("HTTP"));
        builder.addInterface("10.1.2.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(getServiceType("ICMP"));
        OnmsNode node6 = builder.getCurrentNode();
        getNodeDao().save(node6);
        getNodeDao().flush();
        setNode6(node6);
        
        final OnmsEvent event = new OnmsEvent();
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
        
        final OnmsNotification notif = new OnmsNotification();
        notif.setEvent(event);
        notif.setTextMsg("This is a test notification");
        notif.setIpAddress(InetAddressUtils.getInetAddress("192.168.1.1"));
        notif.setNode(m_node1);
        notif.setServiceType(getServiceType("ICMP"));
        getNotificationDao().save(notif);
        getNotificationDao().flush();
        
        final OnmsUserNotification userNotif = new OnmsUserNotification();
        userNotif.setUserId("TestUser");
        userNotif.setNotification(notif);
        getUserNotificationDao().save(userNotif);
        getUserNotificationDao().flush();
        
        final OnmsUserNotification userNotif2 = new OnmsUserNotification();
        userNotif2.setUserId("TestUser2");
        userNotif2.setNotification(notif);
        getUserNotificationDao().save(userNotif2);
        getUserNotificationDao().flush();
        
        final OnmsMonitoredService svc = getMonitoredServiceDao().get(node1.getId(), InetAddressUtils.addr("192.168.1.1"), "SNMP");
        final OnmsOutage resolved = new OnmsOutage(new Date(), new Date(), event, event, svc, null, null);
        getOutageDao().save(resolved);
        getOutageDao().flush();
        
        final OnmsOutage unresolved = new OnmsOutage(new Date(), event, svc);
        getOutageDao().save(unresolved);
        getOutageDao().flush();
        
        final OnmsAlarm alarm = new OnmsAlarm();
        alarm.setDistPoller(getDistPollerDao().load("localhost"));
        alarm.setUei(event.getEventUei());
        alarm.setAlarmType(1);
        alarm.setNode(m_node1);
        alarm.setDescription("This is a test alarm");
        alarm.setLogMsg("this is a test alarm log message");
        alarm.setCounter(1);
        alarm.setIpAddr(InetAddressUtils.getInetAddress("192.168.1.1"));
        alarm.setSeverity(OnmsSeverity.NORMAL);
        alarm.setFirstEventTime(event.getEventTime());
        alarm.setLastEvent(event);
        getAlarmDao().save(alarm);
        getAlarmDao().flush();
        
        final OnmsMap map = new OnmsMap("DB_Pop_Test_Map", "admin");
        map.setBackground("fake_background.jpg");
        map.setAccessMode(OnmsMap.ACCESS_MODE_ADMIN);
        map.setType(OnmsMap.USER_GENERATED_MAP);
        map.setMapGroup("admin");
        getOnmsMapDao().save(map);
        getOnmsMapDao().flush();
        
        final OnmsMapElement mapElement = new OnmsMapElement(map, 1,
                OnmsMapElement.NODE_TYPE,
                "Test Node",
                OnmsMapElement.defaultNodeIcon,
                0,
                10);
        getOnmsMapElementDao().save(mapElement);
        getOnmsMapElementDao().flush();
        
        final DataLinkInterface dli = new DataLinkInterface(node1, 1, node1.getId(), 1, "A", new Date());
        getDataLinkInterfaceDao().save(dli);
        getDataLinkInterfaceDao().flush();
        
        final DataLinkInterface dli2 = new DataLinkInterface(node1, 2, node1.getId(), 1, "A", new Date());
        getDataLinkInterfaceDao().save(dli2);
        getDataLinkInterfaceDao().flush();
        
        final DataLinkInterface dli3 = new DataLinkInterface(node2, 1, node1.getId(), 1, "A", new Date());
        getDataLinkInterfaceDao().save(dli3);
        getDataLinkInterfaceDao().flush();
        
        final OnmsAcknowledgment ack = new OnmsAcknowledgment();
        ack.setAckTime(new Date());
        ack.setAckType(AckType.UNSPECIFIED);
        ack.setAckAction(AckAction.UNSPECIFIED);
        ack.setAckUser("admin");
        getAcknowledgmentDao().save(ack);
        getAcknowledgmentDao().flush();
    }

    private OnmsCategory getCategory(final String categoryName) {
        OnmsCategory cat = getCategoryDao().findByName(categoryName);
        if (cat == null) {
            cat = new OnmsCategory(categoryName);
        }
        cat.getAuthorizedGroups().add(categoryName+"Group");
        getCategoryDao().save(cat);
        getCategoryDao().flush();
        return cat;
    }

    private OnmsDistPoller getDistPoller(final String localhost, final String localhostIp) {
    	final OnmsDistPoller distPoller = getDistPollerDao().get(localhost);
        if (distPoller == null) {
            final OnmsDistPoller newDp = new OnmsDistPoller(localhost, localhostIp);
            getDistPollerDao().save(newDp);
            getDistPollerDao().flush();
            return newDp;
        }
        return distPoller;
    }

    private OnmsServiceType getServiceType(final String name) {
    	final OnmsServiceType serviceType = getServiceTypeDao().findByName(name);
        if (serviceType == null) {
            final OnmsServiceType newService = new OnmsServiceType(name);
            getServiceTypeDao().save(newService);
            getServiceTypeDao().flush();
            return newService;
        }
        return serviceType;
    }

    
    public AlarmDao getAlarmDao() {
        return m_alarmDao;
    }


    public void setAlarmDao(final AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }


    public AssetRecordDao getAssetRecordDao() {
        return m_assetRecordDao;
    }


    public void setAssetRecordDao(final AssetRecordDao assetRecordDao) {
        m_assetRecordDao = assetRecordDao;
    }


    public CategoryDao getCategoryDao() {
        return m_categoryDao;
    }


    public void setCategoryDao(final CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }


    public DistPollerDao getDistPollerDao() {
        return m_distPollerDao;
    }


    public void setDistPollerDao(final DistPollerDao distPollerDao) {
        m_distPollerDao = distPollerDao;
    }


    public EventDao getEventDao() {
        return m_eventDao;
    }


    public void setEventDao(final EventDao eventDao) {
        m_eventDao = eventDao;
    }


    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }


    public void setIpInterfaceDao(final IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }


    public MonitoredServiceDao getMonitoredServiceDao() {
        return m_monitoredServiceDao;
    }


    public void setMonitoredServiceDao(final MonitoredServiceDao monitoredServiceDao) {
        m_monitoredServiceDao = monitoredServiceDao;
    }


    public NodeDao getNodeDao() {
        return m_nodeDao;
    }


    public void setNodeDao(final NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }


    public NotificationDao getNotificationDao() {
        return m_notificationDao;
    }


    public void setNotificationDao(final NotificationDao notificationDao) {
        m_notificationDao = notificationDao;
    }


    public OutageDao getOutageDao() {
        return m_outageDao;
    }


    public void setOutageDao(final OutageDao outageDao) {
        m_outageDao = outageDao;
    }


    public ServiceTypeDao getServiceTypeDao() {
        return m_serviceTypeDao;
    }


    public void setServiceTypeDao(final ServiceTypeDao serviceTypeDao) {
        m_serviceTypeDao = serviceTypeDao;
    }


    public SnmpInterfaceDao getSnmpInterfaceDao() {
        return m_snmpInterfaceDao;
    }


    public void setSnmpInterfaceDao(final SnmpInterfaceDao snmpInterfaceDao) {
        m_snmpInterfaceDao = snmpInterfaceDao;
    }


    public UserNotificationDao getUserNotificationDao() {
        return m_userNotificationDao;
    }


    public void setUserNotificationDao(final UserNotificationDao userNotificationDao) {
        m_userNotificationDao = userNotificationDao;
    }
    
    public OnmsNode getNode1() {
        return m_node1;
    }
    
    public OnmsNode getNode2() {
        return m_node2;
    }
    
    public OnmsNode getNode3() {
        return m_node3;
    }
    
    public OnmsNode getNode4() {
        return m_node4;
    }
    
    public OnmsNode getNode5() {
        return m_node5;
    }
    
    public OnmsNode getNode6() {
        return m_node6;
    }
    
    private void setNode1(final OnmsNode node1) {
        m_node1 = node1;
    }

    private void setNode2(final OnmsNode node2) {
        m_node2 = node2;
    }

    private void setNode3(final OnmsNode node3) {
        m_node3 = node3;
    }

    private void setNode4(final OnmsNode node4) {
        m_node4 = node4;
    }

    private void setNode5(final OnmsNode node5) {
        m_node5 = node5;
    }

    private void setNode6(final OnmsNode node6) {
        m_node6 = node6;
    }

    public LocationMonitorDao getLocationMonitorDao() {
        return m_locationMonitorDao;
    }

    public void setLocationMonitorDao(final LocationMonitorDao locationMonitorDao) {
        m_locationMonitorDao = locationMonitorDao;
    }

    public OnmsMapDao getOnmsMapDao() {
        return m_onmsMapDao;
    }

    public void setOnmsMapDao(final OnmsMapDao onmsMapDao) {
        this.m_onmsMapDao = onmsMapDao;
    }

    public OnmsMapElementDao getOnmsMapElementDao() {
        return m_onmsMapElementDao;
    }

    public void setOnmsMapElementDao(final OnmsMapElementDao onmsMapElementDao) {
        this.m_onmsMapElementDao = onmsMapElementDao;
    }

    public DataLinkInterfaceDao getDataLinkInterfaceDao() {
        return m_dataLinkInterfaceDao;
    }

    public void setDataLinkInterfaceDao(final DataLinkInterfaceDao dataLinkInterfaceDao) {
        this.m_dataLinkInterfaceDao = dataLinkInterfaceDao;
    }
    
    public AcknowledgmentDao getAcknowledgmentDao() {
        return m_acknowledgmentDao;
    }

    public void setAcknowledgmentDao(final AcknowledgmentDao acknowledgmentDao) {
        m_acknowledgmentDao = acknowledgmentDao;
    }

    public TransactionTemplate getTransactionTemplate() {
        return m_transTemplate;
    }

    public void setTransactionTemplate(final TransactionTemplate transactionTemplate) {
        m_transTemplate = transactionTemplate;
    }
}
