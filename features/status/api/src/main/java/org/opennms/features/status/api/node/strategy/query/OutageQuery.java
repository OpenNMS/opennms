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

import org.opennms.features.status.api.node.strategy.NodeStatusCalculatorConfig;
import org.opennms.features.status.api.node.strategy.Status;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.model.OnmsSeverity;

public class OutageQuery extends Query {

    public OutageQuery(GenericPersistenceAccessor genericPersistenceAccessor, NodeStatusCalculatorConfig config) {
        super(genericPersistenceAccessor, config);
    }

    @Override
    public Status status() {
        sql = new StringBuilder();
        sql.append("SELECT ").append(getViewName()).append(".nodeid").append(", ").append(getSeverityColumn()).append(" AS severity ");
        sql.append("FROM ").append(getViewName()).append(" ");
        sql.append("JOIN node on ").append(getViewName()).append(".nodeid = node.nodeid ") ;

        applyRestrictions();
        applyOrder();
        applyLimitAndOffset();

        final Status status = new Status();
        executeQuery((RowHandler<Object[]>) columns -> status.add(
                (int) columns[0], // nodeId
                OnmsSeverity.get((int) columns[1]) // node status
        ));

        return status;
    }

    @Override
    protected String getSeverityColumn() {
        return "max_outage_severity";
    }

    @Override
    protected String getViewName() {
        return "node_outage_status";
    }

}
