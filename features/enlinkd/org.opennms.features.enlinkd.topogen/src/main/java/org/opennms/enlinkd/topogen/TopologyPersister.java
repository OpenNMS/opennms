/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.enlinkd.topogen;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.enlinkd.model.CdpElement;
import org.opennms.netmgt.enlinkd.model.CdpLink;
import org.opennms.netmgt.enlinkd.model.IsIsElement;
import org.opennms.netmgt.enlinkd.model.IsIsLink;
import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.model.OspfLink;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopologyPersister {

    private final static int BATCH_SIZE = 100;
    private final static Logger LOG = LoggerFactory.getLogger(TopologyPersister.class);

    private GenericPersistenceAccessor genericPersistenceAccessor;

    public TopologyPersister(final GenericPersistenceAccessor genericPersistenceAccessor) {
        this.genericPersistenceAccessor = genericPersistenceAccessor;
    }

    public <E> void persist(List<E> elements) throws SQLException {
        if (elements.size() < 1) {
            return; // nothing do do
        }
        LOG.info("starting to insert {} {}s", elements.size(), elements.get(0).getClass().getSimpleName());

        for (int startBatch = 0; startBatch < elements.size(); startBatch = startBatch + BATCH_SIZE) {
            int endBatch = Math.min(startBatch + 1 + BATCH_SIZE, elements.size());
            List<E> batch = elements.subList(startBatch, endBatch);
            this.genericPersistenceAccessor.saveAll(batch);
            LOG.info("inserting {} of {} {}s done.", endBatch, elements.size(), elements.get(0).getClass().getSimpleName());
        }
    }

    public void deleteTopology() throws SQLException {
        LOG.info("deleting existing topology");
        // we need to delete in this order to avoid foreign key conflicts:
        List<Class<?>> deleteOperations = Arrays.asList(
                CdpLink.class,
                IsIsLink.class,
                LldpLink.class,
                CdpElement.class,
                IsIsElement.class,
                LldpElement.class,
                OspfLink.class,
                OnmsIpInterface.class,
                OnmsSnmpInterface.class);

        for (Class<?> clazz : deleteOperations) {
            this.genericPersistenceAccessor.deleteAll(clazz);
            LOG.info("{}s deleted", clazz.getSimpleName());
        }
        deleteNodes();
    }

    public void deleteNodes() {
        // We need a specific implementation here since OnmsNode consists of 2 tables and the standard delete implementation
        // doesn't seem to work...
        List<OnmsNode> allNodes = this.genericPersistenceAccessor.findAll(OnmsNode.class);
        this.genericPersistenceAccessor.deleteAll(allNodes);
    }

}


