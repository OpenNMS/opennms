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
package org.opennms.features.collection.dto;

import java.util.List;

import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.Parameter;
import org.opennms.netmgt.collection.api.StorageStrategy;
import org.opennms.netmgt.collection.api.StorageStrategyService;
import org.opennms.netmgt.model.ResourcePath;

public class MockStorageStrategy implements StorageStrategy {

    @Override
    public ResourcePath getRelativePathForAttribute(ResourcePath resourceParent, String resource) {
        return null;
    }

    @Override
    public void setResourceTypeName(String name) {
        // pass
    }

    @Override
    public String getResourceNameFromIndex(CollectionResource resource) {
        return null;
    }

    @Override
    public void setStorageStrategyService(StorageStrategyService agent) {
        // pass
    }

    @Override
    public void setParameters(List<Parameter> parameterCollection) throws IllegalArgumentException {
        // pass
    }

}
