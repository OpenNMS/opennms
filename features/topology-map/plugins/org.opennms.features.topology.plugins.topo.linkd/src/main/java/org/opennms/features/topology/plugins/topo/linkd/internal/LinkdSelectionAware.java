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
package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionAware;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.topo.CollapsibleRef;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

public class LinkdSelectionAware implements SelectionAware {

    private final LinkdTopologyFactory m_linkdTopologyFactory;
    public LinkdSelectionAware(LinkdTopologyFactory factory) {
        m_linkdTopologyFactory = factory;
    }

    @Override
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType type) {
        List<Integer> nodeIds = extractNodeIds(selectedVertices);
        if (type == ContentType.Alarm) {
            return new SelectionChangedListener.AlarmNodeIdSelection(nodeIds);
        }
        if (type == ContentType.Node) {
            return new SelectionChangedListener.IdSelection<>(nodeIds);
        }
        return SelectionChangedListener.Selection.NONE;
    }

    @Override
    public boolean contributesTo(ContentType type) {
        return Sets.newHashSet(ContentType.Alarm, ContentType.Node).contains(type);
    }

    /**
     * Gets the node ids from the given vertices. A node id can only be extracted from a vertex with a "nodes"' namespace.
     * For a vertex with namespace "node" the "getId()" method always returns the node id.
     *
     */
    protected List<Integer> extractNodeIds(Collection<VertexRef> vertices) {
        List<Integer> nodeIdList = new ArrayList<>();
        for (VertexRef eachRef : vertices) {
            if (m_linkdTopologyFactory.getActiveNamespace().startsWith(eachRef.getNamespace())) {
                try {
                    nodeIdList.add(Integer.valueOf(eachRef.getId()));
                } catch (NumberFormatException e) {
                    LoggerFactory.getLogger(getClass()).warn("Cannot filter nodes with ID: {}", eachRef.getId());
                }
            } else if("category".equals(eachRef.getNamespace()) && eachRef instanceof CollapsibleRef) {
                CollapsibleRef collapsible = (CollapsibleRef) eachRef;
                nodeIdList.addAll(Collections2.transform(collapsible.getChildren(), input -> Integer.valueOf(input.getId())));
            }
        }
        return nodeIdList;
    }
}
