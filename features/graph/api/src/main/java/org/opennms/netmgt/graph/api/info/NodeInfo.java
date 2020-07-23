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

package org.opennms.netmgt.graph.api.info;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.graph.api.NodeRef;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;


public class NodeInfo {

    private final String location;
    private final Integer id;
    private final String foreignSource;
    private final String foreignId;
    private final String label;
    private final Set<String> categories;
    private final List<IpInfo> ipInfos;
    
    public NodeInfo(final String location, final Integer id, final String foreignSource, final String foreignId,
            final String label, Set<String> categories, List<IpInfo> ipInfos) {
        this.location = location;
        this.id = id;
        this.foreignSource = foreignSource;
        this.foreignId = foreignId;
        this.label = label;
        this.categories = ImmutableSet.copyOf(categories);
        this.ipInfos = ImmutableList.copyOf(ipInfos);    
    }
    
    public String getLocation() {
        return location;
    }

    public Integer getId() {
        return id;
    }

    public String getForeignSource() {
        return foreignSource;
    }

    public String getForeignId() {
        return foreignId;
    }

    public String getLabel() {
        return label;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public List<IpInfo> getIpInterfaces() {
        return ipInfos;
    }

    public NodeRef getNodeRef() {
        if (foreignId != null && foreignSource != null) {
            return NodeRef.from(id, foreignSource, foreignId);
        } else {
            return NodeRef.from(id);
        }
    }

    public static NodeInfoBuilder builder() {
        return new NodeInfoBuilder();
    }
    
    public static class NodeInfoBuilder {
        
        private String location;
        private Integer id;
        private String foreignSource;
        private String foreignId;
        private String label;
        private final Set<String> categories = new HashSet<>();
        private final List<IpInfo> ipInfos = new ArrayList<>();
        
        public NodeInfoBuilder location(final String location) {
            this.location = location;
            return this;
        }

        public NodeInfoBuilder id(final Integer id) {
            this.id = id;
            return this;
        }

        public NodeInfoBuilder foreignSource(final String foreignSource) {
            this.foreignSource = foreignSource;
            return this;
        }
        
        public NodeInfoBuilder foreignId(final String foreignId) {
            this.foreignId = foreignId;
            return this;
        }
        
        public NodeInfoBuilder label(final String label) {
            this.label = label;
            return this;
        }
        
        public NodeInfoBuilder categories(Set<String> categories) {
            this.categories.addAll(categories);
            return this;
        }

        public NodeInfoBuilder ipInterfaces(List<IpInfo> ipInfos) {
            this.ipInfos.addAll(ipInfos);
            return this;
        }
        
        public NodeInfo build() {
            return new NodeInfo(location, id, foreignSource, foreignId, label, categories, ipInfos);
        }
    }
}
