/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
