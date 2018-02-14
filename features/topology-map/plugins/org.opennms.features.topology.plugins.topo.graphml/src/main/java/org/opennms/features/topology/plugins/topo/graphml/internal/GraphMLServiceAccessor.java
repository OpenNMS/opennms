/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.graphml.internal;

import java.util.Objects;

import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.measurements.api.MeasurementsService;
import org.springframework.transaction.support.TransactionOperations;

/**
 * Helper class to access services
 */
public class GraphMLServiceAccessor {

    private NodeDao nodeDao;

    private SnmpInterfaceDao snmpInterfaceDao;

    private MeasurementsService measurementsService;

    private TransactionOperations transactionOperations;

    private AlarmDao alarmDao;

    public AlarmDao getAlarmDao() {
        return alarmDao;
    }

    public void setAlarmDao(AlarmDao alarmDao) {
        this.alarmDao = alarmDao;
    }

    public NodeDao getNodeDao() {
        return nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
    }

    public SnmpInterfaceDao getSnmpInterfaceDao() {
        return snmpInterfaceDao;
    }

    public void setSnmpInterfaceDao(SnmpInterfaceDao snmpInterfaceDao) {
        this.snmpInterfaceDao = Objects.requireNonNull(snmpInterfaceDao);
    }

    public MeasurementsService getMeasurementsService() {
        return measurementsService;
    }

    public void setMeasurementsService(MeasurementsService measurementsService) {
        this.measurementsService = Objects.requireNonNull(measurementsService);
    }

    public TransactionOperations getTransactionOperations() {
        return transactionOperations;
    }

    public void setTransactionOperations(TransactionOperations transactionOperations) {
        this.transactionOperations = Objects.requireNonNull(transactionOperations);
    }
}
