/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.provision.service.operations.ImportOperation;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * DefaultProvisionService
 *
 * @author brozow
 */
public class DefaultProvisionService implements ProvisionService {

    private TransactionTemplate m_transactionTemplate;
    private DistPollerDao m_distPollerDao;
    private NodeDao m_nodeDao;
    private IpInterfaceDao m_ipInterfaceDao;
    private ServiceTypeDao m_serviceTypeDao;
    private MonitoredServiceDao m_monitoredServiceDao;
    private AssetRecordDao m_assetRecordDao;
    private CategoryDao m_categoryDao;
    private EventForwarder m_eventForwarder;
    
    private ThreadLocal<HashMap<String, OnmsServiceType>> m_typeCache = new ThreadLocal<HashMap<String, OnmsServiceType>>();
    /**
     * @return the typeCache
     */
    public ThreadLocal<HashMap<String, OnmsServiceType>> getTypeCache() {
        return m_typeCache;
    }
    /**
     * @param typeCache the typeCache to set
     */
    public void setTypeCache(
            ThreadLocal<HashMap<String, OnmsServiceType>> typeCache) {
        m_typeCache = typeCache;
    }
    /**
     * @return the categoryCache
     */
    public ThreadLocal<HashMap<String, OnmsCategory>> getCategoryCache() {
        return m_categoryCache;
    }
    /**
     * @param categoryCache the categoryCache to set
     */
    public void setCategoryCache(
            ThreadLocal<HashMap<String, OnmsCategory>> categoryCache) {
        m_categoryCache = categoryCache;
    }
    private ThreadLocal<HashMap<String, OnmsCategory>> m_categoryCache = new ThreadLocal<HashMap<String, OnmsCategory>>();

    /**
     * @return the transTemplate
     */
    public TransactionTemplate getTransactionTemplate() {
        return m_transactionTemplate;
    }
    /**
     * @param transactionTemplate the transTemplate to set
     */
    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        m_transactionTemplate = transactionTemplate;
    }
    /**
     * @return the distPollerDao
     */
    public DistPollerDao getDistPollerDao() {
        return m_distPollerDao;
    }
    /**
     * @param distPollerDao the distPollerDao to set
     */
    public void setDistPollerDao(DistPollerDao distPollerDao) {
        m_distPollerDao = distPollerDao;
    }
    /**
     * @return the nodeDao
     */
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }
    /**
     * @param nodeDao the nodeDao to set
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
    /**
     * @return the ipInterfaceDao
     */
    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }
    /**
     * @param ipInterfaceDao the ipInterfaceDao to set
     */
    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }
    /**
     * @return the serviceTypeDao
     */
    public ServiceTypeDao getServiceTypeDao() {
        return m_serviceTypeDao;
    }
    /**
     * @param serviceTypeDao the serviceTypeDao to set
     */
    public void setServiceTypeDao(ServiceTypeDao serviceTypeDao) {
        m_serviceTypeDao = serviceTypeDao;
    }
    /**
     * @return the monitoredServiceDao
     */
    public MonitoredServiceDao getMonitoredServiceDao() {
        return m_monitoredServiceDao;
    }
    /**
     * @param monitoredServiceDao the monitoredServiceDao to set
     */
    public void setMonitoredServiceDao(MonitoredServiceDao monitoredServiceDao) {
        m_monitoredServiceDao = monitoredServiceDao;
    }
    /**
     * @return the assetRecordDao
     */
    public AssetRecordDao getAssetRecordDao() {
        return m_assetRecordDao;
    }
    /**
     * @param assetRecordDao the assetRecordDao to set
     */
    public void setAssetRecordDao(AssetRecordDao assetRecordDao) {
        m_assetRecordDao = assetRecordDao;
    }
    /**
     * @return the categoryDao
     */
    public CategoryDao getCategoryDao() {
        return m_categoryDao;
    }
    /**
     * @param categoryDao the categoryDao to set
     */
    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }
    /**
     * @return the eventForwarder
     */
    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }
    /**
     * @param eventForwarder the eventForwarder to set
     */
    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }
    
    
    public OnmsDistPoller createDistPollerIfNecessary() {
        return (OnmsDistPoller)getTransactionTemplate().execute(new TransactionCallback() {
    
            public Object doInTransaction(TransactionStatus status) {
                OnmsDistPoller distPoller = getDistPollerDao().get("localhost");
                if (distPoller == null) {
                    distPoller = new OnmsDistPoller("localhost", "127.0.0.1");
                    getDistPollerDao().save(distPoller);
                }
                return distPoller;
            }
            
        });
    }
    
    
    public void clearCache() {
        getNodeDao().clear();
    }
    public void sendEvents(ImportOperation importOperation, List<Event> events) {
        EventForwarder eventForwarder = getEventForwarder();
        if (eventForwarder != null && events != null) {
    		importOperation.log().info("Send Events: "+importOperation);
    		// now send the events for the update
    		for (Iterator<Event> eventIt = events.iterator(); eventIt.hasNext();) {
    			Event event = eventIt.next();
    			eventForwarder.sendNow(event);
    		}
    	}
    }
    
    

}
