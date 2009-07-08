/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.scriptd.ins.events;

import java.util.List;

import org.apache.log4j.Category;
import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public abstract class InsAbstractSession extends Thread {

    private String m_criteria;
    
    private Category log;
	/**
	 * the shared string for client authentication
	 * If the shared string is not set, then server doesn't require authentication 
	 */
	public String sharedAuthAsciiString = null;
	
	/**
	 * the criteria for getting active alarms
	 */
	public String criteriaRestriction = "";
	
	public void setSharedASCIIString(String sharedASCIIString) {
		this.sharedAuthAsciiString = sharedASCIIString;
	}
	
	public String getSharedASCIIString() {
		return sharedAuthAsciiString;
	}

	public String getCriteriaRestriction() {
		return criteriaRestriction;
	}

	public void setCriteriaRestriction(String criteriaRestriction) {
	    getLog().debug("Setting the criteria restriction for active alarm list: " + criteriaRestriction);
		this.criteriaRestriction = criteriaRestriction;
	}

    public InsAbstractSession() {
        super();
        ThreadCategory.setPrefix("OpenNMS.InsProxy");
        log=ThreadCategory.getInstance(this.getClass());
        log.debug("InsAbstract Session Constructor: loaded");
    }

    public Category getLog() {
        return log;
    }
    
    public void setLog(Category log) {
        this.log = log;
    }
	
	
    private String getCriteria() {
        return m_criteria;
    }

    private void setCriteria(String criteria) {
        m_criteria = criteria;
    }
    
    @SuppressWarnings("unchecked")
    protected String getIfAlias(int nodeid, int ifindex) {

        log.debug("getting ifalias for nodeid: " +nodeid + " and ifindex: " + ifindex);

        setCriteria("nodeid = " + nodeid + " AND snmpifindex = " + ifindex);
        BeanFactoryReference bf = BeanUtils.getBeanFactory("daoContext");
        final SnmpInterfaceDao snmpInterfaceDao = BeanUtils.getBean(bf,"snmpInterfaceDao", SnmpInterfaceDao.class);
        TransactionTemplate transTemplate = BeanUtils.getBean(bf, "transactionTemplate",TransactionTemplate.class);
        List<OnmsSnmpInterface> iface = (List<OnmsSnmpInterface>) transTemplate.execute(
                   new TransactionCallback() {
                        public Object doInTransaction(final TransactionStatus status) {
                            final OnmsCriteria onmsCriteria = new OnmsCriteria(OnmsSnmpInterface.class);
                            onmsCriteria.add(Restrictions.sqlRestriction(getCriteria()));
                            return snmpInterfaceDao.findMatching(onmsCriteria);
                        }
                   }
        );
        log.debug("interfaces found: " + iface.size());

        String ifAlias = iface.get(0).getIfAlias();
        log.debug("ifalias found: " + ifAlias);
        
        return ifAlias;
    }

    @SuppressWarnings("unchecked")
    protected OnmsSnmpInterface getIfAlias(int nodeid, String ipaddr) {

        log.debug("getting ifalias for nodeid: " +nodeid + " and ipaddress: " + ipaddr);
        setCriteria("nodeid = " + nodeid + " AND ipaddr = '" + ipaddr +"'");
        BeanFactoryReference bf = BeanUtils.getBeanFactory("daoContext");
        final SnmpInterfaceDao snmpInterfaceDao = BeanUtils.getBean(bf,"snmpInterfaceDao", SnmpInterfaceDao.class);
        TransactionTemplate transTemplate = BeanUtils.getBean(bf, "transactionTemplate",TransactionTemplate.class);
        List<OnmsSnmpInterface> iface = (List<OnmsSnmpInterface>) transTemplate.execute(
                   new TransactionCallback() {
                        public Object doInTransaction(final TransactionStatus status) {
                            final OnmsCriteria onmsCriteria = new OnmsCriteria(OnmsSnmpInterface.class);
                            onmsCriteria.add(Restrictions.sqlRestriction(getCriteria()));
                            return snmpInterfaceDao.findMatching(onmsCriteria);
                        }
                   }
        );
        log.debug("interfaces found: " + iface.size());

        String ifAlias = iface.get(0).getIfAlias();
        log.debug("ifalias found: " + ifAlias);
        
        return iface.get(0);
    }

}
