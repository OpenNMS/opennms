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

import java.util.Objects;

import org.opennms.netmgt.bsm.service.model.Application;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;
import org.opennms.netmgt.graph.api.generic.GenericVertex;

import static com.google.common.base.Preconditions.checkArgument;

public final class ApplicationVertex extends AbstractBusinessServiceVertex {

    private final static String PROPERTY_APPLICATION_ID = "applicationId";

    public ApplicationVertex(GenericVertex genericVertex) {
        super(genericVertex);
        Objects.requireNonNull(getApplicationId(), String.format("%s cannot be null", PROPERTY_APPLICATION_ID));
        checkArgument(Type.Application == genericVertex.getProperty(PROPERTY_TYPE), "%s must be %s for %s", PROPERTY_TYPE, Type.Application, getClass());
    }
    
    public Integer getApplicationId() {
        return delegate.getProperty(PROPERTY_APPLICATION_ID);
    }
    
    public static ApplicationVertexBuilder builder() {
        return new ApplicationVertexBuilder();
    }

    public final static class ApplicationVertexBuilder extends AbstractBusinessServiceVertexBuilder<ApplicationVertexBuilder, ApplicationVertex> {
        
        private ApplicationVertexBuilder() {}
        
        public ApplicationVertexBuilder applicationId(Integer applicationId) {
            this.properties.put(PROPERTY_APPLICATION_ID, applicationId);
            this.id(Type.Application + ":" + applicationId);
            return this;
        }

        public ApplicationVertexBuilder graphVertex(GraphVertex graphVertex) {
            this.application(graphVertex.getApplication());
            level(graphVertex.getLevel());
            return this;
        }
        
        public ApplicationVertexBuilder application(Application application) {
            this.label(application.getApplicationName());
            this.reductionKeys(application.getReductionKeys());
            this.applicationId(application.getId());
            this.isLeaf(true);
            return this;
        }
        
        public ApplicationVertex build() {
            this.type(Type.Application);
            return new ApplicationVertex(GenericVertex.builder()
                    .namespace(BusinessServiceGraph.NAMESPACE) // default but can still be changed by properties
                    .properties(properties).build());
        }
    }
    
}
