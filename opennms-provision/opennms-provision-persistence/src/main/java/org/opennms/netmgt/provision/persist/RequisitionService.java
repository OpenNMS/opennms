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

import java.util.Set;

import org.opennms.netmgt.model.requisition.OnmsRequisition;
import org.opennms.netmgt.model.requisition.OnmsRequisitionInterface;
import org.opennms.netmgt.model.requisition.OnmsRequisitionMonitoredService;
import org.opennms.netmgt.model.requisition.OnmsRequisitionNode;
import org.opennms.netmgt.provision.persist.requisition.DeployedRequisitionStats;
import org.opennms.netmgt.provision.persist.requisition.DeployedStats;
import org.opennms.netmgt.provision.persist.requisition.ImportRequest;
import org.springframework.transaction.annotation.Transactional;

public interface RequisitionService {

    OnmsRequisition getRequisition(String foreignSource);

    void deleteRequisition(String foreignSource);

    // TODO MVR merge vs save
    void saveOrUpdateRequisition(OnmsRequisition input);

    // TODO MVR merge vs save
    void saveOrUpdateNode(OnmsRequisition parentPersistedRequisition, OnmsRequisitionNode nodeToUpdateOrReplace);

    // TODO MVR merge vs save
    void saveOrUpdateInterface(OnmsRequisitionNode parentPersistedNode, OnmsRequisitionInterface interfaceToUpdateOrReplace);

    // TODO MVR merge vs save
    void saveOrUpdateService(OnmsRequisitionInterface parentPersistedInterface, OnmsRequisitionMonitoredService serviceToUpdateOrReplace);

    // TODO MVR merge vs save
    void saveOrUpdateNode(OnmsRequisitionNode requisitionNode);

    Set<OnmsRequisition> getRequisitions();

    void triggerImport(ImportRequest importRequest);

    int getDeployedCount();

    // TODO MVR what to do with this?
    @Transactional(readOnly = true)
    DeployedStats getDeployedStats();

    // GLOBAL
    // TODO MVR what to do with these?
    @Transactional(readOnly = true)
    DeployedRequisitionStats getDeployedStats(String foreignSource);
}
