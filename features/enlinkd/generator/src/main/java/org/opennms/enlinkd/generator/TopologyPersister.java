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

package org.opennms.enlinkd.generator;

import java.util.Arrays;
import java.util.List;

import org.opennms.enlinkd.generator.protocol.UserDefinedProtocol;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.enlinkd.model.BridgeBridgeLink;
import org.opennms.netmgt.enlinkd.model.BridgeElement;
import org.opennms.netmgt.enlinkd.model.BridgeMacLink;
import org.opennms.netmgt.enlinkd.model.CdpElement;
import org.opennms.netmgt.enlinkd.model.CdpLink;
import org.opennms.netmgt.enlinkd.model.IpNetToMedia;
import org.opennms.netmgt.enlinkd.model.IsIsElement;
import org.opennms.netmgt.enlinkd.model.IsIsLink;
import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.model.OspfLink;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;

public class TopologyPersister {

    private final static int BATCH_SIZE = 100;

    private GenericPersistenceAccessor genericPersistenceAccessor;
    private TopologyGenerator.ProgressCallback progressCallback;

    public TopologyPersister(final GenericPersistenceAccessor genericPersistenceAccessor,
                             TopologyGenerator.ProgressCallback progressCallback) {
        this.genericPersistenceAccessor = genericPersistenceAccessor;
        this.progressCallback = progressCallback;
    }

    public <E> void persist(E entity) {
        progressCallback.currentProgress("  Inserting %s:", entity.getClass().getSimpleName());
        this.genericPersistenceAccessor.save(entity);
        progressCallback.currentProgress("    Inserting of %s done.", entity.getClass().getSimpleName());
    }

    public <E> void persist(E ... elements) {
        persist(Arrays.asList(elements));
    }

    public <E> void persist(List<E> elements) {
        if (elements.size() < 1) {
            return; // nothing do do
        }
        progressCallback.currentProgress("  Inserting %s %ss:", elements.size(), elements.get(0).getClass().getSimpleName());

        for (int startBatch = 0; startBatch < elements.size(); startBatch = startBatch + BATCH_SIZE) {
            int endBatch = Math.min(startBatch + BATCH_SIZE, elements.size());
            List<E> batch = elements.subList(startBatch, endBatch);
            this.genericPersistenceAccessor.saveAll(batch);
            progressCallback.currentProgress("    Inserting %s of %s %ss done.", endBatch, elements.size(), elements.get(0).getClass().getSimpleName());
        }
    }

    public void deleteTopology() {
        progressCallback.currentProgress("\nDeleting existing generated topology if present: ");
        // we need to delete in this order to avoid foreign key conflicts:
        List<Class<?>> deleteOperations = Arrays.asList(
                CdpLink.class,
                IsIsLink.class,
                LldpLink.class,
                CdpElement.class,
                IsIsElement.class,
                LldpElement.class,
                OspfLink.class,
                BridgeBridgeLink.class,
                BridgeMacLink.class,
                BridgeElement.class,
                OnmsIpInterface.class,
                OnmsSnmpInterface.class,
                IpNetToMedia.class);

        for (Class<?> clazz : deleteOperations) {
            deleteEntities(clazz);
        }
        deleteUserDefinedLinks();
        deleteNodes();
        deleteCategory();
    }

    private void deleteEntities(Class<?> clazz) {
        deleteEntities(
                clazz,
                String.format("SELECT e FROM %s e JOIN e.node n JOIN n.categories c WHERE c.name = '%s'", clazz.getSimpleName(), TopologyGenerator.CATEGORY_NAME));
    }

    private void deleteUserDefinedLinks() {
        deleteEntities(
                OnmsNode.class,
                String.format("SELECT l FROM UserDefinedLink l WHERE l.owner = '%s'", UserDefinedProtocol.OWNER));
    }

    private void deleteNodes() {
        deleteEntities(
                OnmsNode.class,
                String.format("SELECT n FROM OnmsNode n JOIN n.categories c WHERE c.name = '%s'", TopologyGenerator.CATEGORY_NAME));
    }

    private void deleteCategory() {
        deleteEntities(
                OnmsCategory.class,
                String.format("SELECT c FROM OnmsCategory c WHERE c.name = '%s'", TopologyGenerator.CATEGORY_NAME));
    }

    private <E> void deleteEntities(Class<?> clazz, String sql) {
        List<?> entities = this.genericPersistenceAccessor.find(sql);
        this.genericPersistenceAccessor.deleteAll(entities);
        progressCallback.currentProgress("  %s %ss deleted.", entities.size(), clazz.getSimpleName());
    }

}


