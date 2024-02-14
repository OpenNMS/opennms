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
package org.opennms.web.svclayer.model;

import java.util.List;


/**
 * <p>RtcNodeModel class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @since 1.8.1
 */
public class RtcNodeModel {
    private RtcNodeList m_nodeList = new RtcNodeList();

    public void addNode(RtcNode node) {
        m_nodeList.add(node);
    }

    public RtcNodeList getRtcNodeList() {
        return m_nodeList;
    }

    public List<RtcNode> getRtcNodes() {
        return m_nodeList.getObjects();
    }
}
