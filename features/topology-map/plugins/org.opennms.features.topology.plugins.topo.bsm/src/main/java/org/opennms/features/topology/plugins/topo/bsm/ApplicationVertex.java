/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.bsm;

import java.util.Set;

import org.opennms.netmgt.bsm.service.model.Application;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;

public class ApplicationVertex extends AbstractBusinessServiceVertex {

    private final Integer applicationId;
    private final Set<String> reductionKeys;

    public ApplicationVertex(Application application, int level) {
        this(application.getId(),
                application.getApplicationName(),
                application.getReductionKeys(),
                level);
    }

    public ApplicationVertex(GraphVertex graphVertex) {
        this(graphVertex.getApplication(), graphVertex.getLevel());
    }

    private ApplicationVertex(int applicationId, String applicationName, Set<String> reductionKeys, int level) {
        super(Type.Application + ":" + applicationId, applicationName, level);
        this.applicationId = applicationId;
        this.reductionKeys = reductionKeys;
        setTooltipText(String.format("Application '%s'", applicationName));
        setIconKey("bsm.application");
    }

    public Integer getApplicationId() {
        return applicationId;
    }

    @Override
    public Type getType() {
        return Type.Application;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public Set<String> getReductionKeys() {
        return reductionKeys;
    }

    @Override
    public <T> T accept(BusinessServiceVertexVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
