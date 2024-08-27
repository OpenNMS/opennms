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

package org.opennms.netmgt.telemetry.protocols.common.cache;

import java.util.List;

public class NodeInfo implements org.opennms.integration.api.v1.flows.Flow.NodeInfo {

    // ID of the interface which was selected during IP to node lookup
    private int interfaceId;

    private int nodeId;
    private String foreignId;
    private String foreignSource;
    private List<String> categories = List.of();

    @Override
    public int getInterfaceId() {
        return this.interfaceId;
    }

    public void setInterfaceId(final int interfaceId) {
        this.interfaceId = interfaceId;
    }

    @Override
    public int getNodeId() {
        return this.nodeId;
    }

    public void setNodeId(final int nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String getForeignId() {
        return this.foreignId;
    }

    public void setForeignId(final String foreignId) {
        this.foreignId = foreignId;
    }

    @Override
    public String getForeignSource() {
        return this.foreignSource;
    }

    public void setForeignSource(final String foreignSource) {
        this.foreignSource = foreignSource;
    }

    @Override
    public List<String> getCategories() {
        return this.categories;
    }

    public void setCategories(final List<String> categories) {
        this.categories = categories;
    }
}
