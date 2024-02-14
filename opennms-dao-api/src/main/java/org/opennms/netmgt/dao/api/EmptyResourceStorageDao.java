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
package org.opennms.netmgt.dao.api;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.ResourcePath;

public class EmptyResourceStorageDao implements ResourceStorageDao {

    @Override
    public boolean exists(ResourcePath path, int depth) {
        return false;
    }

    @Override
    public boolean existsWithin(ResourcePath path, int depth) {
        return false;
    }

    @Override
    public Set<ResourcePath> children(ResourcePath path, int depth) {
        return Collections.emptySet();
    }

    @Override
    public boolean delete(ResourcePath path) {
        return false;
    }

    @Override
    public Set<OnmsAttribute> getAttributes(ResourcePath path) {
        return Collections.emptySet();
    }

    @Override
    public void setStringAttribute(ResourcePath path, String key, String value) {
        // pass
    }

    @Override
    public String getStringAttribute(ResourcePath path, String key) {
        return null;
    }

    @Override
    public Map<String, String> getStringAttributes(ResourcePath path) {
        return Collections.emptyMap();
    }

    @Override
    public void updateMetricToResourceMappings(ResourcePath path, Map<String, String> metricsNameToResourceNames) {
        // pass
    }

    @Override
    public Map<String, String> getMetaData(ResourcePath path) {
        return Collections.emptyMap();
    }

}
