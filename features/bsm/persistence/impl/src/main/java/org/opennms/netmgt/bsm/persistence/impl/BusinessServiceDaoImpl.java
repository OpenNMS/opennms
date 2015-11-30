/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.persistence.impl;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.bsm.persistence.api.BusinessService;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessStatusEntity;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.model.OnmsSeverity;

public class BusinessServiceDaoImpl extends AbstractDaoHibernate<BusinessService, Long> implements BusinessServiceDao {

    public BusinessServiceDaoImpl() {
        super(BusinessService.class);
    }

    @Override
    public List<BusinessStatusEntity> getStatus() {
        final StringBuilder sql = new StringBuilder();
        sql.append("select distinct alarm.node.id, alarm.ipAddr, alarm.serviceType.id, min(alarm.lastEventTime), max(alarm.severity), (count(*) - count(alarm.alarmAckTime)) ");
        sql.append("from OnmsAlarm alarm ");
        sql.append("where alarm.node.id != null and alarm.ipAddr != null and alarm.serviceType.id != null and alarm.alarmAckTime is null ");
        sql.append("group by alarm.node.id, alarm.ipAddr, alarm.serviceType.id");

        List<BusinessStatusEntity> entityList = new ArrayList<>();
        List<Object[][]> objects = (List<Object[][]>) getHibernateTemplate().find(sql.toString());
        for (Object[] eachRow : objects) {
            BusinessStatusEntity entity = new BusinessStatusEntity((Integer)eachRow[0], (InetAddress)eachRow[1], (Integer) eachRow[2], (OnmsSeverity) eachRow[4], (Long) eachRow[5]);
            entityList.add(entity);
        }
        return entityList;
    }

}
