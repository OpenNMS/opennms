/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.RequisitionDao;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.requisition.RequisitionEntity;
import org.opennms.netmgt.provision.persist.requisition.DeployedRequisitionStats;
import org.opennms.netmgt.provision.persist.requisition.DeployedStats;
import org.opennms.netmgt.provision.persist.requisition.ImportRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service access to {@link RequisitionEntity}s.
 *
 * @author mvrueden
 */
public class DefaultRequisitionService implements RequisitionService {

    private Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private RequisitionDao requisitionDao;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private EventProxy eventProxy;

    @Override
    @Transactional(readOnly = true)
    public RequisitionEntity getRequisition(String foreignSource) {
        return requisitionDao.get(foreignSource);
    }

    @Override
    @Transactional
    public void deleteRequisition(String foreignSource) {
        if (foreignSource != null && requisitionDao.get(foreignSource) != null) {
            requisitionDao.delete(foreignSource);
        }
    }

    @Override
    @Transactional
    public void saveOrUpdateRequisition(RequisitionEntity input) {
        validate(input);
        input.updateLastUpdated();
        requisitionDao.saveOrUpdate(input);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<RequisitionEntity> getRequisitions() {
        return new HashSet<>(requisitionDao.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public void triggerImport(ImportRequest importRequest) {
        Objects.requireNonNull(importRequest);
        LOG.debug("importRequisition: Sending import event for {}", importRequest);
        try {
            getEventProxy().send(importRequest.toReloadEvent());
        } catch (final EventProxyException e) {
            throw new DataAccessResourceFailureException("Unable to send event ", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int getDeployedCount() {
        return getRequisitions().size();
    }

    @Override
    // TODO MVR what to do with this?
    @Transactional(readOnly = true)
    public DeployedStats getDeployedStats() {
        final DeployedStats deployedStats = new DeployedStats();
        final Map<String, Date> lastImportedMap = new HashMap<String, Date>();
        getRequisitions().forEach(r -> {
            lastImportedMap.put(r.getForeignSource(), r.getLastImport());
        });
        Map<String, Set<String>> map = nodeDao.getForeignIdsPerForeignSourceMap();
        map.entrySet().forEach(e -> {
            DeployedRequisitionStats stats = new DeployedRequisitionStats();
            stats.setForeignSource(e.getKey());
            stats.setForeignIds(new ArrayList<>(e.getValue()));
            stats.setLastImported(lastImportedMap.get(e.getKey()));
            deployedStats.add(stats);
        });
        return deployedStats;
    }

    // GLOBAL
    @Override
    // TODO MVR what to do with these?
    @Transactional(readOnly = true)
    public DeployedRequisitionStats getDeployedStats(String foreignSource) {
        final DeployedRequisitionStats deployedStats = new DeployedRequisitionStats();
        final RequisitionEntity fs = getRequisition(foreignSource);
        deployedStats.setForeignSource(foreignSource);
        deployedStats.setLastImported(fs.getLastImport());
        deployedStats.addAll(nodeDao.getForeignIdsPerForeignSource(foreignSource));
        return deployedStats;
    }

    private EventProxy getEventProxy() {
        return eventProxy;
    }

    private void validate(RequisitionEntity requisition) {
//        // TODO MVR
//        throw new UnsupportedOperationException("TODO MVR implement me");
    }
}
