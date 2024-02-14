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
package org.opennms.netmgt.graph.provider.bsm;


import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.netmgt.bsm.service.model.Application;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;
import org.opennms.netmgt.graph.api.enrichment.EnrichedProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.Severity;
import org.opennms.netmgt.graph.domain.AbstractDomainVertex;

import com.google.common.collect.ImmutableSet;

public final class BusinessServiceVertex extends AbstractDomainVertex {

    enum Type {
        BusinessService,
        IpService,
        ReductionKey,
        Application
    }

    interface Properties {
        String LEVEL = "level";
        String TYPE = "type";
        String IS_LEAF = "isLeaf";
        String REDUCTION_KEYS = "reductionKeys";

        interface Application {
            String id = "applicationId";
        }

        interface BusinessService {
            String id = "businessServiceId";
        }

        interface IpService {
            String id = "ipServiceId";
        }
    }

    private static final int MAX_LABEL_LENGTH = 27;
    private static final Pattern REDUCTION_KEY_LABEL_PATTERN = Pattern.compile("^.*\\/(.+?):.*:(.+)$");


    public BusinessServiceVertex(GenericVertex genericVertex) {
        super(genericVertex);

        // generic checks
        Objects.requireNonNull(getLevel(), "getLevel() cannot be null.");
        Objects.requireNonNull(isLeaf(), "isLeaf() cannot be null.");
        Objects.requireNonNull(getType(), "getType() cannot be null.");
        Objects.requireNonNull(getReductionKeys(), "getReductionKeys() cannot be null.");

        // Specific checks
        final Type type = genericVertex.getProperty(Properties.TYPE);
        if (Type.Application == type) {
            Objects.requireNonNull(delegate.getProperty(Properties.Application.id), String.format("%s cannot be null", Properties.Application.id));
        } else if (Type.BusinessService == type) {
            Objects.requireNonNull(delegate.getProperty(Properties.BusinessService.id), String.format("%s cannot be null", Properties.BusinessService.id));
        } else if (Type.IpService == type) {
            Objects.requireNonNull(delegate.getProperty(Properties.IpService.id), String.format("%s cannot be null", Properties.IpService.id));
        } else if (Type.ReductionKey == type) {
            Objects.requireNonNull(delegate.getProperty(Properties.REDUCTION_KEYS), String.format("%s cannot be null", Properties.REDUCTION_KEYS));
        } else {
            throw new IllegalArgumentException("Unknown type of BusinessServiceVertex: "  + type);
        }
    }

    public int getLevel() {
        return this.delegate.getProperty(Properties.LEVEL);
    }

    public boolean isLeaf(){
        return this.delegate.getProperty(Properties.IS_LEAF);
    }

    public Type getType() {
        return this.delegate.getProperty(Properties.TYPE);
    }

    public Set<String> getReductionKeys() {
        return this.delegate.getProperty(Properties.REDUCTION_KEYS);
    }

    public final static BusinessServiceVertex from(GenericVertex genericVertex) {
        return new BusinessServiceVertex(genericVertex);
    }

    public static BusinessServiceVertexBuilder builder() {
        return new BusinessServiceVertexBuilder();
    }

    public final static class BusinessServiceVertexBuilder extends AbstractDomainVertexBuilder {
        
        private BusinessServiceVertexBuilder() {}

        public BusinessServiceVertexBuilder reductionKeys(Set<String> reductionKeys) {
            property(Properties.REDUCTION_KEYS, ImmutableSet.copyOf(reductionKeys));
            return this;
        }

        public BusinessServiceVertexBuilder type(Type type) {
            property(Properties.TYPE, type);
            return this;
        }

        public BusinessServiceVertexBuilder isLeaf(boolean isLeaf){
            property(Properties.IS_LEAF, isLeaf);
            return this;
        }

        /** level the level of the vertex in the Business Service Hierarchy. The root element is level 0. */
        public BusinessServiceVertexBuilder level(int level){
            property(Properties.LEVEL, level);
            return this;
        }

        public BusinessServiceVertexBuilder graphVertex(GraphVertex graphVertex) {
            if (graphVertex.getBusinessService() != null) {
                businessService(graphVertex.getBusinessService());
            } else if (graphVertex.getIpService() != null) {
                ipService(graphVertex.getIpService());
            } else if (graphVertex.getReductionKey() != null) {
                reductionKey(graphVertex.getReductionKey());
            } else if (graphVertex.getApplication() != null) {
                application(graphVertex.getApplication());
            } else {
                throw new IllegalArgumentException("Cannot convert GraphVertex to BusinessServiceVertex: " + graphVertex);
            }
            level(graphVertex.getLevel());
            status(graphVertex.getStatus());
            return this;
        }

        private BusinessServiceVertexBuilder status(final Status status) {
            Objects.requireNonNull(status);
            final Severity severity = convert(status);
            property(EnrichedProperties.STATUS, severity);
            return this;
        }

        public BusinessServiceVertexBuilder businessService(BusinessService businessService) {
            properties.clear();
            // First add attributes, so it is not overriding any other data
            for (Map.Entry<String, String> eachEntry : businessService.getAttributes().entrySet()) {
                property(eachEntry.getKey(), eachEntry.getValue());
            }
            id(Type.BusinessService + ":" + businessService.getId());
            type(Type.BusinessService);
            label(businessService.getName());
            isLeaf(false);
            reductionKeys(ImmutableSet.of());
            property("reduceFunction", businessService.getReduceFunction());
            property(Properties.BusinessService.id, businessService.getId());
            return this;
        }

        public BusinessServiceVertexBuilder ipService(IpService ipService) {
            properties.clear();
            id(Type.IpService + ":" + ipService.getId());
            type(Type.IpService);
            label(ipService.getServiceName());
            isLeaf(true);
            reductionKeys(ipService.getReductionKeys());
            nodeRef(ipService.getNodeId());
            property("ipAddr", ipService.getIpAddress());
            property("ipAddress", ipService.getIpAddress());
            property(Properties.IpService.id, ipService.getId());
            return this;
        }

        public BusinessServiceVertexBuilder reductionKey(String reductionKey) {
            properties.clear();
            id(Type.ReductionKey + ":" + reductionKey);
            type(Type.ReductionKey);
            label(getLabelFromReductionKey(reductionKey));
            isLeaf(true);
            reductionKeys(ImmutableSet.of(reductionKey));
            return this;
        }

        public BusinessServiceVertexBuilder application(Application application) {
            properties.clear();
            id(Type.Application + ":" + application.getId());
            type(Type.Application);
            label(application.getApplicationName());
            reductionKeys(application.getReductionKeys());
            isLeaf(true);
            property(Properties.Application.id, application.getId());
            return this;
        }

        public BusinessServiceVertex build() {
            return new BusinessServiceVertex(GenericVertex.builder()
                    .namespace(BusinessServiceGraph.NAMESPACE)
                    .properties(properties).build());
        }
    }

    protected static String getLabelFromReductionKey(String reductionKey) {
        String label;
        Matcher m = REDUCTION_KEY_LABEL_PATTERN.matcher(reductionKey);
        if (m.matches()) {
            label = String.format("%s:%s", m.group(1), m.group(2));
        } else {
            label = reductionKey;
        }
        if (label.length() > MAX_LABEL_LENGTH) {
            return label.substring(0, MAX_LABEL_LENGTH - "...".length()) + "...";
        }
        return label;
    }

    static Severity convert(final Status status) {
        Objects.requireNonNull(status);
        if (status == Status.INDETERMINATE) {
            return Severity.Unknown;
        }
        return Severity.valueOf(status.getLabel());
    }
}
