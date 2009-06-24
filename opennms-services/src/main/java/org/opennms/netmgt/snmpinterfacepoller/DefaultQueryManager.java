/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.snmpinterfacepoller;

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


public class DefaultQueryManager implements QueryManager {

    String m_criteria;
    OnmsSnmpInterface m_snmpInterface;
    private Category log;

    private String getCriteria() {
        return m_criteria;
    }

    private void setCriteria(String criteria) {
        m_criteria = criteria;
    }

    public DefaultQueryManager() {
        log = ThreadCategory.getInstance(this.getClass());
    }

    public List<OnmsSnmpInterface> getSnmpInterfaces(String criteria) {
        
        setCriteria(criteria);
        BeanFactoryReference bf = BeanUtils.getBeanFactory("daoContext");
        final SnmpInterfaceDao snmpInterfaceDao = BeanUtils.getBean(bf,"snmpInterfaceDao", SnmpInterfaceDao.class);
        TransactionTemplate transTemplate = BeanUtils.getBean(bf, "transactionTemplate",TransactionTemplate.class);
        @SuppressWarnings("unchecked")
        List<OnmsSnmpInterface> iface = (List<OnmsSnmpInterface>) transTemplate.execute(
                   new TransactionCallback() {
                        public Object doInTransaction(final TransactionStatus status) {
                            final OnmsCriteria onmsCriteria = new OnmsCriteria(OnmsSnmpInterface.class);
                            onmsCriteria.add(Restrictions.sqlRestriction(getCriteria()));
                            return snmpInterfaceDao.findMatching(onmsCriteria);
                        }
                   }
        );
        log.debug("interfacce trovate: " + iface.size());
        
        return iface;
    }

    public void saveSnmpInterface(OnmsSnmpInterface snmpInterface) {
        setSnmpInterface(snmpInterface);
        BeanFactoryReference bf = BeanUtils.getBeanFactory("daoContext");
        final SnmpInterfaceDao snmpInterfaceDao = BeanUtils.getBean(bf,"snmpInterfaceDao", SnmpInterfaceDao.class);
        TransactionTemplate transTemplate = BeanUtils.getBean(bf, "transactionTemplate",TransactionTemplate.class);
        transTemplate.execute(
                      new TransactionCallback() {
                          public Object doInTransaction(final TransactionStatus status) {
                              snmpInterfaceDao.saveOrUpdate(getSnmpInterface());
                              return new Object();
                          }
                      }
        );
    }

    private OnmsSnmpInterface getSnmpInterface() {
        return m_snmpInterface;
    }

    private void setSnmpInterface(OnmsSnmpInterface snmpInterface) {
        m_snmpInterface = snmpInterface;
    }

}
