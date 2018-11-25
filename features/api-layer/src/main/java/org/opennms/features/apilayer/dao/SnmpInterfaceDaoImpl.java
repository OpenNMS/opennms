/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.dao;

import java.util.Objects;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.apilayer.utils.ModelMappers;
import org.opennms.integration.api.v1.dao.SnmpInterfaceDao;
import org.opennms.integration.api.v1.model.SnmpInterface;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsSnmpInterface;

public class SnmpInterfaceDaoImpl implements SnmpInterfaceDao {

    private final org.opennms.netmgt.dao.api.SnmpInterfaceDao snmpInterfaceDao;
    private final SessionUtils sessionUtils;

    public SnmpInterfaceDaoImpl(org.opennms.netmgt.dao.api.SnmpInterfaceDao snmpInterfaceDao, SessionUtils sessionUtils) {
        this.snmpInterfaceDao = Objects.requireNonNull(snmpInterfaceDao);
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
    }

    @Override
    public Long getSnmpInterfaceCount() {
        final CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsSnmpInterface.class);
        return sessionUtils.withReadOnlyTransaction(() -> (long)snmpInterfaceDao.countMatching(criteriaBuilder.toCriteria()));
    }

    @Override
    public SnmpInterface findByNodeIdAndDescrOrName(Integer nodeId, String descrOrName) {
        // Note that the SnmpInterfaceDaoHibernate#findByNodeIdAndDescription method actually
        // searches by either the ifName or the ifDescr
        return sessionUtils.withReadOnlyTransaction(() -> ModelMappers.toSnmpInterface(snmpInterfaceDao.findByNodeIdAndDescription(nodeId, descrOrName)));
    }

}
