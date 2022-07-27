/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.processing.enrichment;

import java.util.List;

public class NodeInfo {

    // ID of the interface which was selected during IP to node lookup
    private Integer interfaceId;

    private Integer nodeId;
    private String foreignId;
    private String foreignSource;
    private List<String> categories = List.of();

    public Integer getInterfaceId() {
        return this.interfaceId;
    }

    public void setInterfaceId(final Integer interfaceId) {
        this.interfaceId = interfaceId;
    }

    public Integer getNodeId() {
        return this.nodeId;
    }

    public void setNodeId(final Integer nodeId) {
        this.nodeId = nodeId;
    }

    public String getForeignId() {
        return this.foreignId;
    }

    public void setForeignId(final String foreignId) {
        this.foreignId = foreignId;
    }

    public String getForeignSource() {
        return this.foreignSource;
    }

    public void setForeignSource(final String foreignSource) {
        this.foreignSource = foreignSource;
    }

    public List<String> getCategories() {
        return this.categories;
    }

    public void setCategories(final List<String> categories) {
        this.categories = categories;
    }
}
