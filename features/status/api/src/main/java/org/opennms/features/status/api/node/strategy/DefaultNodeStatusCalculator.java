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

package org.opennms.features.status.api.node.strategy;

import java.util.Map;

import org.opennms.features.status.api.node.NodeStatusCalculator;
import org.opennms.features.status.api.node.strategy.query.QueryBuilder;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.model.OnmsSeverity;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultNodeStatusCalculator implements NodeStatusCalculator {

    @Autowired
    private GenericPersistenceAccessor genericPersistenceAccessor;

    @Override
    public Status calculateStatus(NodeStatusCalculatorConfig config) {
        final Status status = new QueryBuilder(genericPersistenceAccessor).buildFrom(config).status();
        return status;
    }

    public int countStatus(NodeStatusCalculatorConfig config) {
        int count = new QueryBuilder(genericPersistenceAccessor).buildFrom(config).count();
        return count;
    }

    public Map<OnmsSeverity, Long> calculateStatusOverview(NodeStatusCalculatorConfig config) {
        Map<OnmsSeverity, Long> overview = new QueryBuilder(genericPersistenceAccessor).buildFrom(config).overview();
        return overview;
    }
}
