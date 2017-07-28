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

package org.opennms.features.status.api.node.strategy.query;

import java.math.BigInteger;

import org.opennms.features.status.api.node.strategy.NodeStatusCalculatorConfig;
import org.opennms.features.status.api.node.strategy.Status;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.model.OnmsSeverity;

public class AlarmQuery extends Query {

    public AlarmQuery(GenericPersistenceAccessor genericPersistenceAccessor, NodeStatusCalculatorConfig config) {
        super(genericPersistenceAccessor, config);
    }

    @Override
    public Status status() {
        sql = new StringBuilder();
        sql.append("SELECT ").append(getViewName()).append(".nodeid").append(", ").append(getSeverityColumn()).append(" AS severity, alarm_count, alarm_count_unack ");
        sql.append("FROM ").append(getViewName()).append(" ");
        sql.append("JOIN node on ").append(getViewName()).append(".nodeid = node.nodeid ") ;

        applyRestrictions();
        applyOrder();
        applyLimitAndOffset();

        final Status status = new Status();
        executeQuery((RowHandler<Object[]>) columns -> status.add(
                (int) columns[0], // nodeId
                OnmsSeverity.get((int) columns[1]), // node status
                columns[2] != null ? ((BigInteger) columns[2]).longValue() : 0, // alarm count
                columns[3] != null ? ((BigInteger) columns[3]).longValue() : 0 // unacknowledged count
        ));

        return status;
    }

    @Override
    protected String getSeverityColumn() {
        if (config.isIncludeAcknowledgedAlarms()) {
            return "max_alarm_severity";
        }
        return "max_alarm_severity_unack";
    }

    @Override
    protected String getViewName() {
        return "node_alarm_status";
    }

}
