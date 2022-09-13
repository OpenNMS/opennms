/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification.dto;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.google.common.reflect.TypeToken;

/**
 * A classification rule ready for processing on the minion.
 *
 * Representation of a classification rule for transportation to twins.
 * It is an almost identical copy of {@link org.opennms.netmgt.flows.classification.persistence.api.Rule} but with some
 * fields changed. All references to external datastructures are inlined and denormalized. Some fields not required for
 * processing have been omitted.
 */
public class RuleDTO {

    public static final String TWIN_KEY = "flows.classification.rules";

    public static final TypeToken<List<RuleDTO>> TWIN_TYPE = new TypeToken<>() {};

    private String name;

    private String dstAddress;
    private String dstPort;

    private String srcPort;
    private String srcAddress;

    /**
     * Resolved protocol numbers.
     */
    private Set<Integer> protocols;

    /**
     * Resolved set of IPs of all nodes matching the original {@link org.opennms.netmgt.flows.classification.persistence.api.Rule#getExporterFilter()}.
     * The list is reduced to all nodes in the according location.
     */
    private Set<String> exporters;

    /**
     * Global position of the rule in relation to all other rules.
     * This includes the position of the group and the position of the rule in the group.
     */
    private int position;

    public RuleDTO() {
    }

    public RuleDTO(final Builder builder) {
        this.name = Objects.requireNonNull(builder.name);

        this.dstAddress = builder.dstAddress;
        this.dstPort = builder.dstPort;
        this.srcPort = builder.srcPort;
        this.srcAddress = builder.srcAddress;
        this.protocols = builder.protocols;
        this.exporters = builder.exporters;
        this.position = builder.position;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDstAddress() {
        return this.dstAddress;
    }

    public void setDstAddress(final String dstAddress) {
        this.dstAddress = dstAddress;
    }

    public String getDstPort() {
        return this.dstPort;
    }

    public void setDstPort(final String dstPort) {
        this.dstPort = dstPort;
    }

    public String getSrcPort() {
        return this.srcPort;
    }

    public void setSrcPort(final String srcPort) {
        this.srcPort = srcPort;
    }

    public String getSrcAddress() {
        return this.srcAddress;
    }

    public void setSrcAddress(final String srcAddress) {
        this.srcAddress = srcAddress;
    }

    public Set<Integer> getProtocols() {
        return this.protocols;
    }

    public void setProtocols(final Set<Integer> protocols) {
        this.protocols = protocols;
    }

    public Set<String> getExporters() {
        return this.exporters;
    }

    public void setExporters(final Set<String> exporters) {
        this.exporters = exporters;
    }

    public int getPosition() {
        return this.position;
    }

    public void setPosition(final int position) {
        this.position = position;
    }

    public static class Builder {
        private String name;

        private String dstAddress;
        private String dstPort;

        private String srcPort;
        private String srcAddress;

        private final Set<Integer> protocols = Sets.newHashSet();

        private final Set<String> exporters = Sets.newHashSet();

        private int position;

        public Builder withName(final String name) {
            this.name = name;
            return this;
        }

        public Builder withDstAddress(final String dstAddress) {
            this.dstAddress = dstAddress;
            return this;
        }

        public Builder withDstPort(final String dstPort) {
            this.dstPort = dstPort;
            return this;
        }

        public Builder withDstPort(final int dstPort) {
            return this.withDstPort(Integer.toString(dstPort));
        }

        public Builder withSrcPort(final String srcPort) {
            this.srcPort = srcPort;
            return this;
        }

        public Builder withSrcPort(final int srcPort) {
            return this.withSrcPort(Integer.toString(srcPort));
        }

        public Builder withSrcAddress(final String srcAddress) {
            this.srcAddress = srcAddress;
            return this;
        }

        public Builder withProtocols(final Collection<Integer> protocols) {
            this.protocols.addAll(protocols);
            return this;
        }

        public Builder withProtocols(final int... protocols) {
            this.protocols.addAll(Ints.asList(protocols));
            return this;
        }

        public Builder withProtocol(final int protocol) {
            this.protocols.add(protocol);
            return this;
        }

        public Builder withExporters(final Collection<String> exporters) {
            this.exporters.addAll(exporters);
            return this;
        }

        public Builder withExporters(final String... exporters) {
            this.exporters.addAll(Arrays.asList(exporters));
            return this;
        }

        public Builder withExporter(final String exporter) {
            this.exporters.add(exporter);
            return this;
        }

        public Builder withPosition(final int position) {
            this.position = position;
            return this;
        }

        public RuleDTO build() {
            return new RuleDTO(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("name", name)
                          .add("dstAddress", dstAddress)
                          .add("dstPort", dstPort)
                          .add("srcPort", srcPort)
                          .add("srcAddress", srcAddress)
                          .add("protocols", protocols)
                          .add("exporters", exporters)
                          .add("position", position)
                          .toString();
    }
}
