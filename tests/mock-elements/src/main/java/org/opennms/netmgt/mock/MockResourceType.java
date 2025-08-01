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
package org.opennms.netmgt.mock;

import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;

public class MockResourceType implements OnmsResourceType {
    private String m_name = "nothingButFoo";
    private String m_label = "evenMoreFoo";
    private String m_link = "http://www.google.com/search?q=opennms";

    @Override
    public String getLabel() {
        return m_label;
    }

    @Override
    public String getLinkForResource(OnmsResource resource) {
        return m_link;
    }

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public boolean isResourceTypeOnParent(OnmsResource parent) {
        return false;
    }

    @Override
    public List<OnmsResource> getResourcesForParent(OnmsResource parent) {
        return Collections.emptyList();
    }

    @Override
    public OnmsResource getChildByName(OnmsResource parent, String name) {
        return null;
    }

    public void setLink(String link) {
        m_link = link;
    }

    public void setLabel(String label) {
        m_label = label;
    }

    public void setName(String name) {
        m_name = name;
    }

}
