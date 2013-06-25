/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.browsers;
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
import java.util.*;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.features.topology.api.SelectionContext;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;

public class AlarmDaoContainer extends OnmsDaoContainer<OnmsAlarm,Integer> {

	private static final long serialVersionUID = -4026870931086916312L;

	public AlarmDaoContainer(AlarmDao dao) {
		super(OnmsAlarm.class, dao);
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
		Collection<Object> propertyIds = new HashSet<Object>();
		propertyIds.addAll(getContainerPropertyIds());

		// This column is a checkbox so we can't sort on it either
		propertyIds.remove("selection");

		return Collections.unmodifiableCollection(propertyIds);
	}

    @Override
    protected void addAdditionalCriteriaOptions(Criteria criteria, Page page, boolean doOrder) {
        criteria.setAliases(Arrays.asList(new Alias[] {
                new Alias("node", "node", JoinType.LEFT_JOIN)
        }));
    }

    @Override
	public void selectionChanged(SelectionContext selectionContext) {
        List<Restriction> newRestrictions = new SelectionContextToRestrictionConverter() {

            @Override
            protected Restriction createRestriction(VertexRef ref ) {
                return new EqRestriction("node.id", Integer.valueOf(ref.getId()));
            }
        }.getRestrictions(selectionContext);
        if (!getRestrictions().equals(newRestrictions)) { // selection really changed
            setRestrictions(newRestrictions);
            getCache().reload(getPage());
            fireItemSetChangedEvent();
        }
	}

}
