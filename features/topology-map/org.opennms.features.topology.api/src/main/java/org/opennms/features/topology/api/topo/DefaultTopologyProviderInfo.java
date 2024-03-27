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
