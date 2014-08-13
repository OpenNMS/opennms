/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.snmp;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.snmp.RowCallback;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.TableTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityPhysicalTableTracker extends TableTracker {
    private static final Logger LOG = LoggerFactory.getLogger(EntityPhysicalTableTracker.class);

    private List<OnmsHwEntity> entities = new ArrayList<OnmsHwEntity>();

    public EntityPhysicalTableTracker() {
        super(EntityPhysicalTableRow.ELEMENTS);
    }

    public EntityPhysicalTableTracker(RowCallback rowProcessor) {
        super(rowProcessor, EntityPhysicalTableRow.ELEMENTS);
    }

    @Override
    public SnmpRowResult createRowResult(int columnCount, SnmpInstId instance) {
        return new EntityPhysicalTableRow(columnCount, instance);
    }

    @Override
    public void rowCompleted(SnmpRowResult row) {
        OnmsHwEntity entity = ((EntityPhysicalTableRow) row).getOnmsHwEntity();
        LOG.debug("rowCompleted: found entity {}: {}", entity.getEntPhysicalName(), entity.getEntPhysicalIndex());
        if (entity.getEntPhysicalContainedIn() != 0) {
            for (OnmsHwEntity e : entities) {
                if (e.getEntPhysicalIndex() == entity.getEntPhysicalContainedIn()) {
                    e.addChildEntity(entity);
                    continue;
                }
            }
        }
        entities.add(entity);
    }

    public OnmsHwEntity getRootEntity() {
        for (OnmsHwEntity entity : entities) {
            if (entity.isRoot()) {
                return entity;
            }
        }
        return null;
    }

}
