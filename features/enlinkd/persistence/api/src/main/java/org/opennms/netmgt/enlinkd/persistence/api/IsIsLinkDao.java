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
package org.opennms.netmgt.enlinkd.persistence.api;

import java.util.List;

import org.opennms.netmgt.enlinkd.model.IsIsLink;
import org.opennms.netmgt.model.OnmsNode;

public interface IsIsLinkDao extends LinkDao<IsIsLink, Integer> {

    IsIsLink get(OnmsNode node, Integer isisCircIndex, Integer isisISAdjIndex);

    IsIsLink get(Integer nodeId, Integer isisCircIndex, Integer isisISAdjIndex);

    /**
     * Returns all IsIsLinks related by the sysId of an intermediary IsIsElement and the
     * adj/circ indexes of some other IsIsLink that is directly related to the given node.
     * Used to retrieve all IsIsLinks that need to be accessed when finding IsIsLinks of a node.
     */
    List<IsIsLink> findBySysIdAndAdjAndCircIndex(int nodeId);

}
