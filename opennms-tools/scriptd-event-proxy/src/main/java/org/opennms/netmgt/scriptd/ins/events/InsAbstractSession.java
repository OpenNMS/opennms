/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.scriptd.ins.events;

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public abstract class InsAbstractSession extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(InsAbstractSession.class);

    private String m_criteria;
    
	/**
	 * the shared string for client authentication
	 * If the shared string is not set, then server doesn't require authentication 
	 */
	private String m_sharedAuthAsciiString = null;
	
	/**
	 * the criteria for getting active alarms
	 */
	public String criteriaRestriction = "";
	
	public void setSharedASCIIString(final String sharedASCIIString) {
		this.m_sharedAuthAsciiString = sharedASCIIString;
	}
	
	public String getSharedASCIIString() {
		return m_sharedAuthAsciiString;
	}

	public String getCriteriaRestriction() {
		return criteriaRestriction;
	}

	public void setCriteriaRestriction(final String criteriaRestriction) {
	    LOG.debug("Setting the criteria restriction for active alarm list: {}", criteriaRestriction);
		this.criteriaRestriction = criteriaRestriction;
	}

    public InsAbstractSession() {
        super();
        LOG.debug("InsAbstract Session Constructor: loaded");
    }

    private String getCriteria() {
        return m_criteria;
    }

    private void setCriteria(final String criteria) {
        m_criteria = criteria;
    }
    
    /**
     * @param nodeid
     * @param ifindex
     * @return
     */
    protected String getIfAlias(final int nodeid, final int ifindex) {

        LOG.debug("getting ifalias for nodeid: {} and ifindex: {}", nodeid, ifindex);

        setCriteria("nodeid = " + nodeid + " AND snmpifindex = " + ifindex);

        BeanFactoryReference bf = BeanUtils.getBeanFactory("daoContext");
        final SnmpInterfaceDao snmpInterfaceDao = BeanUtils.getBean(bf, "snmpInterfaceDao", SnmpInterfaceDao.class);
        final TransactionTemplate transTemplate = BeanUtils.getBean(bf, "transactionTemplate", TransactionTemplate.class);

        final OnmsSnmpInterface iface = transTemplate.execute(
            new TransactionCallback<OnmsSnmpInterface>() {
                 public OnmsSnmpInterface doInTransaction(final TransactionStatus status) {
                     return snmpInterfaceDao.findByNodeIdAndIfIndex(nodeid, ifindex);
                 }
            }
        );

        if (iface == null) {
            return "-1";
        } else {
            final String ifAlias = iface.getIfAlias();
            LOG.debug("ifalias found: {}", ifAlias);
            return ifAlias;
        }
    }
}
