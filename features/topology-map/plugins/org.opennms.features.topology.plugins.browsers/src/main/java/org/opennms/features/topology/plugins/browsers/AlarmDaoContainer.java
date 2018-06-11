/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.browsers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.features.topology.api.browsers.OnmsVaadinContainer;
import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.springframework.transaction.support.TransactionOperations;

public class AlarmDaoContainer extends OnmsVaadinContainer<OnmsAlarm,Integer> {

    private static final long serialVersionUID = -4026870931086916312L;

    public AlarmDaoContainer(AlarmDao dao, TransactionOperations transactionTemplate) {
        super(OnmsAlarm.class, new OnmsDaoContainerDatasource<>(dao, transactionTemplate));
        addBeanToHibernatePropertyMapping("nodeLabel", "node.label");
    }

    @Override
    protected void updateContainerPropertyIds(Map<Object, Class<?>> properties) {
        // Causes problems because it is a map of values
        properties.remove("details");

        // Causes referential integrity problems
        // @see http://issues.opennms.org/browse/NMS-5750
        properties.remove("distPoller");
    }

    @Override
    protected Integer getId(OnmsAlarm bean){
        return bean == null ? null : bean.getId();
    }

    @Override
    public Collection<?> getSortableContainerPropertyIds() {
        Collection<Object> propertyIds = new HashSet<>();
        propertyIds.addAll(getContainerPropertyIds());

        // This column is a checkbox so we can't sort on it either
        propertyIds.remove("selection");

        return Collections.unmodifiableCollection(propertyIds);
    }

    @Override
    protected void addAdditionalCriteriaOptions(Criteria criteria, Page page, boolean doOrder) {
        // we join table node, to be able eto sort by node.label
        criteria.setAliases(Arrays.asList(new Alias[] {
                new Alias("node", "node", JoinType.LEFT_JOIN)
        }));
    }

    @Override
    protected ContentType getContentType() {
        return ContentType.Alarm;
    }
}
