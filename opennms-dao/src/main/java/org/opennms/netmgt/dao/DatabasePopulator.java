/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.NotificationDao;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.dao.api.UserNotificationDao;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsUserNotification;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.collect.Lists;

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
	
	public static interface Extension<T extends OnmsDao<?,?>> {
		DaoSupport<T> getDaoSupport();
		void onPopulate(DatabasePopulator populator, T dao);
		void onShutdown(DatabasePopulator populator, T dao);
	}
	
	public static class DaoSupport<T extends OnmsDao<?,?>> {
		private final Class<T> daoClass;
		private final T daoObject;
		
		public DaoSupport(Class<T> daoClass, T daoObject) {
			this.daoClass = daoClass;
			this.daoObject = daoObject;
		}
		
		public Class<T> getDaoClass() {
			return (Class<T>)this.daoClass;
		}
		
		public T getDao() {
			return this.daoObject;
		}
	}
	
    private static final Logger LOG = LoggerFactory.getLogger(DatabasePopulator.class);

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
    private MonitoringLocationDao m_monitoringLocationDao;
    private LocationMonitorDao m_locationMonitorDao;
    private AcknowledgmentDao m_acknowledgmentDao;
    private TransactionOperations m_transOperation;
    
    private OnmsNode m_node1;
    private OnmsNode m_node2;
    private OnmsNode m_node3;
    private OnmsNode m_node4;
    private OnmsNode m_node5;
    private OnmsNode m_node6;
    
    private boolean m_populateInSeparateTransaction = true;
    private boolean m_resetInSeperateTransaction = true;
    private final List<Extension> extensions = new ArrayList<>();
    
    private Map<Class<? super OnmsDao<?,?>>, OnmsDao<?,?>> daoRegistry = new HashMap<Class<? super OnmsDao<?,?>>, OnmsDao<?,?>>();
    
    public <T extends OnmsDao<?,?>> T lookupDao(Class<? super OnmsDao<?,?>> daoClass) {
    	for (Class<? super OnmsDao<?,?>> eachDaoClass : daoRegistry.keySet()) {
    		if (eachDaoClass.isAssignableFrom(daoClass)) {
    			return (T)daoRegistry.get(eachDaoClass);
    		}
    	}
    	return null;
    }

    public void registerDao(Class<? super OnmsDao<?,?>> daoClass, OnmsDao<?,?> dao) {
    	if (dao == null || daoClass == null) return;
    	// check if not already added
    	for (Class<? super OnmsDao<?,?>> eachDaoClass : daoRegistry.keySet()) {
    		if (eachDaoClass.isAssignableFrom(daoClass)) {
    			return; // a super class for this is already added (ignore)
    		}
    	}
    	// adding
    	daoRegistry.put(daoClass, dao);
    }
    
    public void addExtension(Extension extension) {
    	if (extension == null) return;
    	extensions.add(extension);
    }
    
    public boolean populateInSeparateTransaction() {
        return m_populateInSeparateTransaction;
    }
    
    public void setPopulateInSeparateTransaction(final boolean pop) {
        m_populateInSeparateTransaction = pop;
    }

    public void setResetInSeperateTransaction(boolean resetInSeperateTransaction) {
        m_resetInSeperateTransaction = resetInSeperateTransaction;
    }

    public void populateDatabase() {
        if (m_populateInSeparateTransaction) {
            m_transOperation.execute(new TransactionCallbackWithoutResult() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) {
                    doPopulateDatabase();
                }
            });
        } else {
            doPopulateDatabase();
        }
    }

    public void resetDatabase() {
        if (m_resetInSeperateTransaction) {
            m_transOperation.execute(new TransactionCallbackWithoutResult() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) {
                    doResetDatabase();
                }
            });
        } else {
            doResetDatabase();
        }
    }

    private void doResetDatabase() {
        LOG.debug("==== DatabasePopulator Reset ====");
        for (final OnmsOutage outage : m_outageDao.findAll()) {
            m_outageDao.delete(outage);
        }
        for (final OnmsUserNotification not : m_userNotificationDao.findAll()) {
            m_userNotificationDao.delete(not);
        }
        for (final OnmsNotification not : m_notificationDao.findAll()) {
            m_notificationDao.delete(not);
        }
        for (final OnmsAlarm alarm : m_alarmDao.findAll()) {
            m_alarmDao.delete(alarm);
        }
        for (final OnmsEvent event : m_eventDao.findAll()) {
            m_eventDao.delete(event);
        }

        for (final OnmsSnmpInterface snmpIface : m_snmpInterfaceDao.findAll()) {
            for (OnmsIpInterface eachIf : snmpIface.getIpInterfaces()) {
                eachIf.setSnmpInterface(null);
                snmpIface.getNode().getIpInterfaces().remove(eachIf);
            }
            snmpIface.getNode().getSnmpInterfaces().remove(snmpIface);
            m_snmpInterfaceDao.delete(snmpIface);
        }
        for (final OnmsIpInterface iface : m_ipInterfaceDao.findAll()) {
            iface.setSnmpInterface(null);
            iface.getNode().getIpInterfaces().remove(iface);
            m_ipInterfaceDao.delete(iface);
        }
        for (final OnmsNode node : m_nodeDao.findAll()) {
            m_nodeDao.delete(node);
        }
        for (final OnmsServiceType service : m_serviceTypeDao.findAll()) {
            m_serviceTypeDao.delete(service);
        }
        for (final OnmsMonitoringLocation location : m_monitoringLocationDao.findAll()) {
            // Don't delete the default localhost monitoring location
            if (!MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID.equals(location.getLocationName())) {
                m_monitoringLocationDao.delete(location);
            }
        }
        for (final OnmsCategory category : m_categoryDao.findAll()) {
            m_categoryDao.delete(category);
        }
        
        LOG.debug("= DatabasePopulatorExtension Reset Starting =");
    	for (Extension eachExtension : extensions) {
    			DaoSupport daoSupport = eachExtension.getDaoSupport();
    			OnmsDao<?,?> dao = daoSupport != null && daoSupport.getDaoClass() != null ? lookupDao(daoSupport.getDaoClass()) : null;

    			eachExtension.onShutdown(this, dao);
    			if (dao != null) {
    				dao.flush();
    			}
    	}
    	LOG.debug("= DatabasePopulatorExtension Reset Finished =");
        
        m_outageDao.flush();
        m_userNotificationDao.flush();
        m_notificationDao.flush();
        m_alarmDao.flush();
        m_eventDao.flush();
        m_snmpInterfaceDao.flush();
        m_ipInterfaceDao.flush();
        m_nodeDao.flush();
        m_serviceTypeDao.flush();
        
        LOG.debug("==== DatabasePopulator Reset Finished ====");
    }

    private void doPopulateDatabase() {
        LOG.debug("==== DatabasePopulator Starting ====");

        final NetworkBuilder builder = new NetworkBuilder();

        final OnmsNode node1 = buildNode1(builder);
        getNodeDao().save(node1);
        getNodeDao().flush();

        OnmsNode node2 = buildNode2(builder);
        getNodeDao().save(node2);
        getNodeDao().flush();
        setNode2(node2);
        
        OnmsNode node3 = buildNode3(builder);
        getNodeDao().save(node3);
        getNodeDao().flush();
        setNode3(node3);
        
        OnmsNode node4 = buildNode4(builder);
        getNodeDao().save(node4);
        getNodeDao().flush();
        setNode4(node4);
        
        OnmsNode node5 = buildNode5(builder);
        getNodeDao().save(node5);
        getNodeDao().flush();
        setNode5(node5);
        
        OnmsNode node6 = buildNode6(builder);
        getNodeDao().save(node6);
        getNodeDao().flush();
        setNode6(node6);
        
        final OnmsEvent event = buildEvent(builder.getDistPoller());
        event.setEventCreateTime(new Date(1436881548292L));
        event.setEventTime(new Date(1436881548292L));
        getEventDao().save(event);
        getEventDao().flush();
        
        final OnmsNotification notif = buildTestNotification(builder, event);
        getNotificationDao().save(notif);
        getNotificationDao().flush();
        
        final OnmsUserNotification userNotif = buildTestUserNotification(notif);
        getUserNotificationDao().save(userNotif);
        getUserNotificationDao().flush();
        
        final OnmsUserNotification userNotif2 = buildTestUser2Notification(notif);
        getUserNotificationDao().save(userNotif2);
        getUserNotificationDao().flush();
        
        final OnmsMonitoredService svc = getMonitoredServiceDao().get(node1.getId(), InetAddressUtils.addr("192.168.1.1"), "SNMP");
        final OnmsOutage resolved = new OnmsOutage(new Date(1436881548292L), new Date(1436881548292L), event, event, svc, null, null);
        getOutageDao().save(resolved);
        getOutageDao().flush();
        
        final OnmsOutage unresolved = new OnmsOutage(new Date(1436881548292L), event, svc);
        getOutageDao().save(unresolved);
        getOutageDao().flush();
        
        final OnmsAlarm alarm = buildAlarm(event);
        getAlarmDao().save(alarm);
        getAlarmDao().flush();
        
        final OnmsAcknowledgment ack = new OnmsAcknowledgment();
        ack.setAckTime(new Date(1437073152156L));
        ack.setAckType(AckType.UNSPECIFIED);
        ack.setAckAction(AckAction.UNSPECIFIED);
        ack.setAckUser("admin");
        getAcknowledgmentDao().save(ack);
        getAcknowledgmentDao().flush();
        
        final OnmsMonitoringLocation def = new OnmsMonitoringLocation();
        def.setLocationName("RDU");
        def.setMonitoringArea("East Coast");
        def.setPollingPackageNames(Collections.singletonList("example1"));
        def.setCollectionPackageNames(Collections.singletonList("example1"));
        def.setGeolocation("Research Triangle Park, NC");
        def.setLatitude(35.715751f);
        def.setLongitude(-79.16262f);
        def.setPriority(1L);
        def.setTags(Collections.singletonList("blah"));
        m_monitoringLocationDao.save(def);

        LOG.debug("= DatabasePopulatorExtension Populate Starting =");
        for (Extension eachExtension : extensions) {
        	DaoSupport daoSupport = eachExtension.getDaoSupport();
        	OnmsDao<?,?> dao = daoSupport != null ? daoSupport.getDao() : null;
        	Class<? super OnmsDao<?,?>> daoClass = daoSupport != null ? daoSupport.getDaoClass() : null;
        	registerDao(daoClass, dao);

        	dao = lookupDao(daoClass);
        	eachExtension.onPopulate(this, dao);
        	if (dao != null) {
        		dao.flush();
        	}
        }
        LOG.debug("= DatabasePopulatorExtension Populate Finished =");
        
        LOG.debug("==== DatabasePopulator Finished ====");
    }

    private OnmsCategory getCategory(final String categoryName) {
        OnmsCategory cat = m_categoryDao.findByName(categoryName, true);
        if (cat == null) {
            cat = new OnmsCategory(categoryName);
            m_categoryDao.save(cat);
            m_categoryDao.flush();
        }
        return cat;
    }

    private OnmsServiceType getService(final String serviceName) {
        OnmsServiceType service = m_serviceTypeDao.findByName(serviceName);
        if (service == null) {
            service = new OnmsServiceType(serviceName);
            m_serviceTypeDao.save(service);
            m_serviceTypeDao.flush();
        }
        return service;
    }

    private OnmsNode buildNode1(final NetworkBuilder builder) {
        setNode1(builder.addNode("node1").setForeignSource("imported:").setForeignId("1").setType(NodeType.ACTIVE).getNode());
        builder.addCategory(getCategory("DEV_AC"));
        builder.addCategory(getCategory("IMP_mid"));
        builder.addCategory(getCategory("OPS_Online"));
        builder.addCategory(getCategory("Routers")); 
        builder.setBuilding("HQ");
        builder.addSnmpInterface(1)
            .setCollectionEnabled(true)
            .setIfOperStatus(1)
            .setIfSpeed(10000000)
            .setIfDescr("ATM0")
            .setIfAlias("Initial ifAlias value")
            .setIfType(37)
            .setPhysAddr("34E45604BB69")
            .addIpInterface("192.168.1.1").setIsManaged("M").setIsSnmpPrimary("P");
        //getNodeDao().save(builder.getCurrentNode());
        //getNodeDao().flush();
        builder.addService(getService("ICMP"));
        builder.addService(getService("SNMP"));
        builder.addSnmpInterface(2)
            .setCollectionEnabled(true)
            .setIfOperStatus(1)
            .setIfSpeed(10000000)
            .setIfName("eth0")
            .setIfType(6)
            .setPhysAddr("C9D2DFC7CB68")
            .addIpInterface("192.168.1.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(getService("ICMP"));
        builder.addService(getService("HTTP"));
        builder.addSnmpInterface(3)
            .setCollectionEnabled(false)
            .setIfOperStatus(1)
            .setIfSpeed(10000000)
            .setPhysAddr("EE8CE7F4BE99")
            .addIpInterface("192.168.1.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(getService("ICMP"));
        builder.addSnmpInterface(4)
            .setCollectionEnabled(false)
            .setIfOperStatus(1)
            .setIfSpeed(10000000)
            .setPhysAddr("4AF39F080908")
            .addIpInterface("fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%5").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(getService("ICMP"));
        return builder.getCurrentNode();
    }

    private OnmsNode buildNode2(final NetworkBuilder builder) {
        builder.addNode("node2").setForeignSource("imported:").setForeignId("2").setType(NodeType.ACTIVE);
        builder.addCategory(getCategory("IMP_mid"));
        builder.addCategory(getCategory("Servers"));
        builder.setBuilding("HQ");
        builder.addInterface("192.168.2.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(getService("ICMP"));
        builder.addService(getService("SNMP"));
        builder.addInterface("192.168.2.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(getService("ICMP"));
        builder.addService(getService("HTTP"));
        builder.addInterface("192.168.2.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(getService("ICMP"));
        return builder.getCurrentNode();
    }

    private OnmsNode buildNode3(final NetworkBuilder builder) {
        builder.addNode("node3").setForeignSource("imported:").setForeignId("3").setType(NodeType.ACTIVE);
        builder.addCategory(getCategory("OPS_Online"));
        builder.addInterface("192.168.3.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(getService("ICMP"));
        builder.addService(getService("SNMP"));
        builder.addInterface("192.168.3.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(getService("ICMP"));
        builder.addService(getService("HTTP"));
        builder.addInterface("192.168.3.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(getService("ICMP"));
        return builder.getCurrentNode();
    }

    private OnmsNode buildNode4(final NetworkBuilder builder) {
        builder.addNode("node4").setForeignSource("imported:").setForeignId("4").setType(NodeType.ACTIVE);
        builder.addCategory(getCategory("DEV_AC"));
        builder.addInterface("192.168.4.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(getService("ICMP"));
        builder.addService(getService("SNMP"));
        builder.addInterface("192.168.4.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(getService("ICMP"));
        builder.addService(getService("HTTP"));
        builder.addInterface("192.168.4.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(getService("ICMP"));
        return builder.getCurrentNode();
    }

    private OnmsNode buildNode5(final NetworkBuilder builder) {
        //This node purposely doesn't have a foreignId style assetNumber
        builder.addNode("alternate-node1").setType(NodeType.ACTIVE).getAssetRecord().setAssetNumber("5");
        builder.addCategory(getCategory("DEV_AC"));
        builder.addCategory(getCategory("Switches"));
        builder.addInterface("10.1.1.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(getService("ICMP"));
        builder.addService(getService("SNMP"));
        builder.addInterface("10.1.1.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(getService("ICMP"));
        builder.addService(getService("HTTP"));
        builder.addInterface("10.1.1.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(getService("ICMP"));
        return builder.getCurrentNode();
    }

    private OnmsNode buildNode6(final NetworkBuilder builder) {
        //This node purposely doesn't have a assetNumber and is used by a test to check the category
        builder.addNode("alternate-node2").setType(NodeType.ACTIVE).getAssetRecord().setDisplayCategory("category1");
        builder.addCategory(getCategory("DEV_AC"));
        builder.addInterface("10.1.2.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(getService("ICMP"));
        builder.addService(getService("SNMP"));
        builder.addInterface("10.1.2.2").setIsManaged("M").setIsSnmpPrimary("S");
        builder.addService(getService("ICMP"));
        builder.addService(getService("HTTP"));
        builder.addInterface("10.1.2.3").setIsManaged("M").setIsSnmpPrimary("N");
        builder.addService(getService("ICMP"));
        return builder.getCurrentNode();
    }

    public OnmsEvent buildEvent(final OnmsDistPoller distPoller) {
        final OnmsEvent event = new OnmsEvent();
        event.setDistPoller(distPoller);
        event.setEventCreateTime(new Date(1437061537126L));
        event.setEventDescr("This is the description of a test event.");
        event.setEventDisplay("Y");
        event.setEventHost("127.0.0.1"); // TODO: Figure out exactly what this field is storing
        event.setEventLog("Y");
        event.setEventLogMsg("Test Event Log Message");
        event.setEventParameters(Lists.newArrayList(new OnmsEventParameter(event, "testParm", "HelloWorld", "string")));
        event.setEventSeverity(OnmsSeverity.INDETERMINATE.getId());
        event.setEventSource("test");
        event.setEventTime(new Date(1437061537105L));
        event.setEventUei("uei.opennms.org/test");
        event.setIpAddr(InetAddressUtils.getInetAddress("192.168.1.1"));
        event.setNode(m_node1);
        event.setServiceType(m_serviceTypeDao.findByName("ICMP"));
        return event;
    }

    private OnmsNotification buildTestNotification(final NetworkBuilder builder, final OnmsEvent event) {
        final OnmsNotification notif = new OnmsNotification();
        notif.setEvent(event);
        notif.setTextMsg("This is a test notification");
        notif.setIpAddress(InetAddressUtils.getInetAddress("192.168.1.1"));
        notif.setNode(m_node1);
        notif.setServiceType(getService("ICMP"));
        return notif;
    }

    private OnmsUserNotification buildTestUserNotification(final OnmsNotification notif) {
        final OnmsUserNotification userNotif = new OnmsUserNotification();
        userNotif.setUserId("TestUser");
        userNotif.setNotification(notif);
        return userNotif;
    }

    private OnmsUserNotification buildTestUser2Notification(final OnmsNotification notif) {
        final OnmsUserNotification userNotif2 = new OnmsUserNotification();
        userNotif2.setUserId("TestUser2");
        userNotif2.setNotification(notif);
        return userNotif2;
    }

    private OnmsAlarm buildAlarm(final OnmsEvent event) {
        // TODO: Add reductionKey, suppressedTime, suppressedUntil to this object?

        final OnmsAlarm alarm = new OnmsAlarm();
        alarm.setDistPoller(getDistPollerDao().whoami());
        alarm.setUei(event.getEventUei());
        alarm.setAlarmType(OnmsAlarm.PROBLEM_TYPE);
        alarm.setNode(m_node1);
        alarm.setDescription("This is a test alarm");
        alarm.setLogMsg("this is a test alarm log message");
        alarm.setCounter(1);
        alarm.setIpAddr(InetAddressUtils.getInetAddress("192.168.1.1"));
        alarm.setSeverity(OnmsSeverity.NORMAL);
        alarm.setFirstEventTime(event.getEventTime());
        alarm.setLastEvent(event);
        alarm.setServiceType(m_serviceTypeDao.findByName("ICMP"));
        return alarm;
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

    public MonitoringLocationDao getMonitoringLocationDao() {
        return m_monitoringLocationDao;
    }

    public void setMonitoringLocationDao(final MonitoringLocationDao monitoringLocationDao) {
        m_monitoringLocationDao = monitoringLocationDao;
    }

    public LocationMonitorDao getLocationMonitorDao() {
        return m_locationMonitorDao;
    }

    public void setLocationMonitorDao(final LocationMonitorDao locationMonitorDao) {
        m_locationMonitorDao = locationMonitorDao;
    }

    public AcknowledgmentDao getAcknowledgmentDao() {
        return m_acknowledgmentDao;
    }

    public void setAcknowledgmentDao(final AcknowledgmentDao acknowledgmentDao) {
        m_acknowledgmentDao = acknowledgmentDao;
    }

    public TransactionOperations getTransactionTemplate() {
        return m_transOperation;
    }

    public void setTransactionTemplate(final TransactionOperations transactionOperation) {
        m_transOperation = transactionOperation;
    }
}
