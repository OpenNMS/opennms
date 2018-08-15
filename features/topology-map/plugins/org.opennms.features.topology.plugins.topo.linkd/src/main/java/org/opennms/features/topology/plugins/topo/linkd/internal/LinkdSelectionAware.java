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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionAware;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.topo.GroupRef;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.gwt.thirdparty.guava.common.base.Function;
import com.google.gwt.thirdparty.guava.common.collect.Collections2;

public class LinkdSelectionAware implements SelectionAware {
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
     * @param vertices
     * @return
     */
    protected List<Integer> extractNodeIds(Collection<VertexRef> vertices) {
        List<Integer> nodeIdList = new ArrayList<>();
        for (VertexRef eachRef : vertices) {
            if ("nodes".equals(eachRef.getNamespace())) {
                try {
                    nodeIdList.add(Integer.valueOf(eachRef.getId()));
                } catch (NumberFormatException e) {
                    LoggerFactory.getLogger(getClass()).warn("Cannot filter nodes with ID: {}", eachRef.getId());
                }
            } else if( ((Vertex)eachRef).isGroup() && "category".equals(eachRef.getNamespace()) ){
                try{
                    GroupRef group = (GroupRef) eachRef;
                    nodeIdList.addAll(Collections2.transform(group.getChildren(), new Function<VertexRef, Integer>(){
                        @Override
                        public Integer apply(VertexRef input) {
                            return Integer.valueOf(input.getId());
                        }
                    }));
                } catch (ClassCastException e){
                    LoggerFactory.getLogger(getClass()).warn("Cannot filter category with ID: {} children: {}", eachRef.getId(), ((GroupRef) eachRef).getChildren());

                }
            }
        }
        return nodeIdList;
    }
}
