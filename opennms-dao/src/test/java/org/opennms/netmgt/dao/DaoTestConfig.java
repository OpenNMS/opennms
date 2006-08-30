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

import org.opennms.netmgt.dao.AbstractDaoTestCase.DB;
import org.springframework.transaction.PlatformTransactionManager;

public abstract class DaoTestConfig {

    protected abstract AssetRecordDao createAssetRecordDao();

    protected abstract CategoryDao createCategoryDao();

    protected abstract ServiceTypeDao createServiceTypeDao();

    protected abstract MonitoredServiceDao createMonitoredServiceDao();

    protected abstract IpInterfaceDao createIpInterfaceDao();
    
	protected abstract SnmpInterfaceDao createSnmpInterfaceDao();
	
    protected abstract NodeDao createNodeDao();

    protected abstract DistPollerDao createDistPollerDao();
    
    protected abstract OutageDao createOutageDao();
    
    protected abstract EventDao createEventDao();
    
    protected abstract AlarmDao createAlarmDao();

    protected abstract NotificationDao createNotificationDao();

    protected abstract UserNotificationDao createUserNotificationDao();

    protected abstract void tearDown();

    protected abstract PlatformTransactionManager setUp(DB db, boolean createDb) throws Exception;

    private DistPollerDao m_dpDao;
    private NodeDao m_nodeDao;
    private MonitoredServiceDao m_monSvcDao;
    private IpInterfaceDao m_ifDao;
    private ServiceTypeDao m_svcTypeDao;
    private AssetRecordDao m_arDao;
    private CategoryDao m_catDao;
	private SnmpInterfaceDao m_snmpIfDao;
    private OutageDao m_outageDao;
	private EventDao m_eventDao;
    private AlarmDao m_alarmDao;
    private NotificationDao m_notificationDao;
    private UserNotificationDao m_userNotificationDao;

    public DistPollerDao getDistPollerDao() {
        if (m_dpDao == null) {
            m_dpDao = createDistPollerDao();
        }
        return m_dpDao;
    }

    public NodeDao getNodeDao() {
        if (m_nodeDao == null) {
            m_nodeDao = createNodeDao();
        }
        return m_nodeDao;
    }

    public IpInterfaceDao getIpInterfaceDao() {
        if (m_ifDao == null) {
            m_ifDao = createIpInterfaceDao();
        }
        return m_ifDao;
    }

    public MonitoredServiceDao getMonitoredServiceDao() {
        if (m_monSvcDao == null) {
            m_monSvcDao = createMonitoredServiceDao();
        }
        return m_monSvcDao;
    }

    public ServiceTypeDao getServiceTypeDao() {
        if (m_svcTypeDao == null) {
            m_svcTypeDao = createServiceTypeDao();
        }
        return m_svcTypeDao;
    }

    public AssetRecordDao getAssetRecordDao() {
        if (m_arDao == null) {
            m_arDao = createAssetRecordDao();
        }
        return m_arDao;
    }

    public OutageDao getOutageDao() {
        if (m_outageDao == null) {
            m_outageDao = createOutageDao();
        }
        return m_outageDao;
    }
    
    public EventDao getEventDao() {
    	if (m_eventDao == null) {
    		m_eventDao = createEventDao();
    	}
    	return m_eventDao;
    }

    public void prePopulate() {
    }

    public void postPopulate() {
        // TODO Auto-generated method stub
        
    }

    public CategoryDao getCategoryDao() {
        if (m_catDao == null) {
            m_catDao = createCategoryDao();
        }
        return m_catDao;
    }

	public SnmpInterfaceDao getSnmpInterfaceDao() {
		if (m_snmpIfDao == null) {
			m_snmpIfDao = createSnmpInterfaceDao();
		}
		return m_snmpIfDao;
	}

    public AlarmDao getAlarmDao() {
        if (m_alarmDao == null) {
            m_alarmDao = createAlarmDao();
        }
        return m_alarmDao;
    }

    public NotificationDao getNotificationDao() {
        if (m_notificationDao == null) {
            m_notificationDao = createNotificationDao();
        }
        return m_notificationDao;
    }

    public UserNotificationDao getUserNotificationDao() {
        if (m_userNotificationDao == null) {
            m_userNotificationDao = createUserNotificationDao();
        }
        return m_userNotificationDao;
    }

}
