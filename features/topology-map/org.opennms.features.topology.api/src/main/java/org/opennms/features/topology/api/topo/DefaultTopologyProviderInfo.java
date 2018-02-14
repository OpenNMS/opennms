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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.api.topo;

public class DefaultTopologyProviderInfo implements TopologyProviderInfo {
    private boolean hierarchical;
    private boolean supportsCategorySearch;
    protected String name = "Undefined";
    protected String description = "No description available";

    public DefaultTopologyProviderInfo() {
    }

    public DefaultTopologyProviderInfo(String name, String description, Boolean isHierarchichal, boolean supportsCategorySearch) {
        this.name = name;
        this.description = description;
        if (isHierarchichal != null) {
            this.hierarchical = isHierarchichal;
        }
        this.supportsCategorySearch = supportsCategorySearch;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isHierarchical() {
        return hierarchical;
    }

    @Override
    public boolean isSupportsCategorySearch() {
        return this.supportsCategorySearch;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setHierarchical(boolean hierarchical) {
        this.hierarchical = hierarchical;
    }

    public void setSupportsCategorySearch(boolean supportsCategorySearch) {
        this.supportsCategorySearch = supportsCategorySearch;
    }
}
