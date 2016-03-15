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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.bsm;

import java.util.Set;

import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;

import com.google.common.collect.Sets;

public class ReductionKeyVertex extends AbstractBusinessServiceVertex {

    private final String reductionKey;

    public ReductionKeyVertex(GraphVertex graphVertex) {
        this(graphVertex.getReductionKey(), graphVertex.getLevel(), graphVertex.getStatus());
    }

    protected ReductionKeyVertex(String reductionKey, int level, Status status) {
        super(Type.ReductionKey + ":" + reductionKey, reductionKey, level, status);
        this.reductionKey = reductionKey;
        setTooltipText(String.format("Reduction Key '%s'", reductionKey));
        setIconKey("bsm.reduction-key");
    }

    public String getReductionKey() {
        return reductionKey;
    }

    @Override
    public Type getType() {
        return Type.ReductionKey;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public Set<String> getReductionKeys() {
        return Sets.newHashSet(getReductionKey());
    }

    @Override
    public void accept(BusinessServiceVertexVisitor visitor) {
        visitor.visit(this);
    }
}
