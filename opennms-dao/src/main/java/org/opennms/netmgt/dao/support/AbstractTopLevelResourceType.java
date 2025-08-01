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
package org.opennms.netmgt.dao.support;

import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;

/**
 * Extended by all top-level resources to ensure consistent behavior.
 *
 * @author jwhite
 */
public abstract class AbstractTopLevelResourceType implements OnmsResourceType {

    @Override
    public boolean isResourceTypeOnParent(OnmsResource parent) {
        return false;
    }

    @Override
    public List<OnmsResource> getResourcesForParent(OnmsResource parent) {
        if (parent == null) {
            return getTopLevelResources();
        }
        return Collections.emptyList();
    }

    @Override
    public OnmsResource getChildByName(OnmsResource parent, String name) {
        if (parent != null) {
            return null;
        }

        return getResourceByName(name);
    }

    public abstract List<OnmsResource> getTopLevelResources();

    public abstract OnmsResource getResourceByName(String name);

}
