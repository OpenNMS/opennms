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
package org.opennms.features.topology.app.internal;

import javax.servlet.http.HttpServletRequest;

import org.opennms.features.topology.link.TopologyLinkBuilder;
import org.opennms.features.topology.link.TopologyProvider;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.navigate.ConditionalPageNavEntry;
import org.opennms.web.navigate.DisplayStatus;

public class TopoMapNavEntry implements ConditionalPageNavEntry {
    private String m_name;

    @Override
    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name;
    }

    @Override
    public String getUrl() {
        return new TopologyLinkBuilder()
                .provider(TopologyProvider.ENLINKD)
                .focus("%nodeid%")
                .szl(1).getLink();
    }

    @Override
    public DisplayStatus evaluate(final HttpServletRequest request, final Object target) {
        if (target instanceof OnmsNode) {
            final OnmsNode node = (OnmsNode)target;
            if (node != null && node.getId() != null && node.getId() > 0) {
                return DisplayStatus.DISPLAY_LINK;
            }
        }
        return DisplayStatus.NO_DISPLAY;
    }

    @Override
    public String toString() {
        return "TopoMapNavEntry[url=" + getUrl() + ",name=" + m_name +"]";
    }
}
