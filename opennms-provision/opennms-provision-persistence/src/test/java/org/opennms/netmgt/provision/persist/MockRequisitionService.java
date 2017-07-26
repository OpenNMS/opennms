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

package org.opennms.netmgt.provision.persist;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.requisition.RequisitionEntity;
import org.opennms.netmgt.provision.persist.requisition.ImportRequest;
import org.springframework.util.Assert;

public class MockRequisitionService implements RequisitionService {
    private final Map<String,RequisitionEntity> m_requisitions = new HashMap<>();

    @Override
    public Set<RequisitionEntity> getRequisitions() {
        return new TreeSet<>(m_requisitions.values());
    }

    @Override
    public RequisitionEntity getRequisition(final String foreignSourceName) {
        Assert.notNull(foreignSourceName);
        return m_requisitions.get(foreignSourceName);
    }

    @Override
    public void deleteRequisition(String foreignSource) {
        m_requisitions.remove(foreignSource);
    }

    @Override
    public void saveOrUpdateRequisition(final RequisitionEntity requisition) {
        Assert.notNull(requisition);
        Assert.notNull(requisition.getForeignSource());

        m_requisitions.put(requisition.getForeignSource(), requisition);
    }

    @Override
    public void triggerImport(ImportRequest request) {
        try {
            EventIpcManagerFactory.getIpcManager().send(request.toReloadEvent());
        } catch (EventProxyException e) {
            throw new RuntimeException("Could not send event", e);
        }
    }

    @Override
    public int getDeployedCount() {
        return m_requisitions.size();
    }

}
