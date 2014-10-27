#!/usr/bin/env groovy

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

import org.opennms.core.utils.*;
import org.springframework.context.support.*;
import org.springframework.transaction.*;
import org.springframework.transaction.support.*;
import org.opennms.netmgt.dao.MonitoredServiceDao;

class InTransaction implements TransactionCallback {
  Closure m_callback;

    InTransaction(Closure callback) {
       m_callback = callback;
    }

    public Object doInTransaction(TransactionStatus status) {
       m_callback.call(status);
    }

}

  

System.setProperty("rrd.base.dir", new File(".").getAbsolutePath());
System.setProperty("rrd.binary", "/usr/bin/rrdtool");

context = BeanUtils.getFactory("daoContext", ClassPathXmlApplicationContext.class);

TransactionTemplate transTemplate = context.getBean("transactionTemplate", TransactionTemplate.class);
MonitoredServiceDao dao = context.getBean("monitoredServiceDao", MonitoredServiceDao.class);

inTransaction = new InTransaction() {
    dao.findAll().each {
        println "${it.nodeId}:${it.ipAddress}:${it.serviceName}"
    }
}

transTemplate.execute(inTransaction);



System.exit(0);


