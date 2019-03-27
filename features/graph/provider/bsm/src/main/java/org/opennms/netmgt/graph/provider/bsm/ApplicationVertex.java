/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.provider.bsm;

import java.util.Set;

import org.opennms.netmgt.bsm.service.model.Application;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;

public class ApplicationVertex extends AbstractBusinessServiceVertex {

    private final static String PROPERTY_APPLICATION_ID = "applicationId";

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
        super(Type.Application + ":" + applicationId, applicationName, level, Type.Application, true, reductionKeys);
        delegate.setProperty(PROPERTY_APPLICATION_ID, applicationId);
    }

    public Integer getApplicationId() { // TODO: do we really need this property?
        return delegate.getProperty(PROPERTY_APPLICATION_ID);
    }

}
