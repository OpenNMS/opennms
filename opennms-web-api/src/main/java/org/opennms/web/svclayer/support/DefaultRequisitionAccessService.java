/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.support;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.requisition.OnmsRequisition;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.ImportRequest;
import org.opennms.netmgt.provision.persist.requisition.DeployedRequisitionStats;
import org.opennms.netmgt.provision.persist.requisition.DeployedStats;
import org.opennms.web.svclayer.api.RequisitionAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

// TODO MVR refactor to make it work with database
// TODO MVR test this with a lot of requisitions/nodes
public class DefaultRequisitionAccessService implements RequisitionAccessService {
	
	private static final Logger LOG = LoggerFactory.getLogger(DefaultRequisitionAccessService.class);

    // TODO MVR rip out from applicationcontext
//    @Autowired
//    @Qualifier("pending")
//    private ForeignSourceRepository m_pendingForeignSourceRepository;
//
    @Autowired
    @Qualifier("deployed")
    private ForeignSourceRepository m_deployedForeignSourceRepository;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;
//
    private EventProxy getEventProxy() {
        return m_eventProxy;
    }

    // GLOBAL
    @Override
    public int getDeployedCount() {
                return m_deployedForeignSourceRepository.getRequisitions().size();
    }

    // GLOBAL
    @Override
    public List<OnmsRequisition> getRequisitions() {
        return new ArrayList<>(m_deployedForeignSourceRepository.getRequisitions());
    }

    // GLOBAL
    @Override
    public DeployedStats getDeployedStats() {
                final DeployedStats deployedStats = new DeployedStats();
                final Map<String,Date> lastImportedMap = new HashMap<String,Date>();
                m_deployedForeignSourceRepository.getRequisitions().forEach(r -> {
                    lastImportedMap.put(r.getForeignSource(), r.getLastImport());
                });
                Map<String,Set<String>> map = m_nodeDao.getForeignIdsPerForeignSourceMap();
                map.entrySet().forEach(e -> {
                    DeployedRequisitionStats stats = new DeployedRequisitionStats();
                    stats.setForeignSource(e.getKey());
                    stats.setForeignIds(new ArrayList<String>(e.getValue()));
                    stats.setLastImported(lastImportedMap.get(e.getKey()));
                    deployedStats.add(stats);
                });
                return deployedStats;
    }

    // GLOBAL
    @Override
    public DeployedRequisitionStats getDeployedStats(String foreignSource) {
                final DeployedRequisitionStats deployedStats = new DeployedRequisitionStats();
                final OnmsRequisition fs = m_deployedForeignSourceRepository.getRequisition(foreignSource);
                deployedStats.setForeignSource(foreignSource);
                deployedStats.setLastImported(fs.getLastImport());
                deployedStats.addAll(m_nodeDao.getForeignIdsPerForeignSource(foreignSource));
                return deployedStats;
    }

    @Override
    public OnmsRequisition getRequisition(final String foreignSource) {
        return m_deployedForeignSourceRepository.getRequisition(foreignSource);
    }

    @Override
    public void importRequisition(final String foreignSource, final String rescanExisting) {
        m_deployedForeignSourceRepository.triggerImport(
                new ImportRequest("Web")
                    .withRescanExisting(rescanExisting)
                    .withForeignSource(foreignSource));

    }

    @Override
    public void deleteRequisition(String foreignSource) {
        m_deployedForeignSourceRepository.delete(getRequisition(foreignSource));
    }

    @Override
    public void save(OnmsRequisition requisition) {
        m_deployedForeignSourceRepository.save(requisition);
    }
}
