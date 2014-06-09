/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.browsers;

import com.google.gwt.thirdparty.guava.common.base.Function;
import com.google.gwt.thirdparty.guava.common.collect.Collections2;
import org.opennms.core.criteria.restrictions.AnyRestriction;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.features.topology.api.topo.GroupRef;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

abstract class NodeIdFocusToRestrictionsConverter {

    public List<Restriction> getRestrictions(Collection<VertexRef> vertices) {
        // when there are no vertices, we need to create
        // a restriction which will ALWAYS fail
        // so we create a dummy restriction for node with id -1 (there should never be a node id with -1).
        if (vertices.isEmpty()) {
            List<Integer> nodeIdList = new ArrayList<Integer>();
            nodeIdList.add(Integer.valueOf(-1)); // this will always return an empty result
            return getRestrictions(nodeIdList);
        }
        return getRestrictions(extractNodeIds(vertices)); // we have entries, so wie use them
    }

    protected abstract Restriction createRestriction(Integer nodeId);

    private List<Restriction> getRestrictions(List<Integer> nodeIdFocus) {
        List<Restriction> restrictions = new ArrayList<Restriction>();
        restrictions.add(getAnyRestriction(nodeIdFocus));
        return restrictions;
    }

    private AnyRestriction getAnyRestriction(List<Integer> nodeIdFocus) {
        List<Restriction> restrictions = new ArrayList<Restriction>();
        for (Integer eachNodeId : nodeIdFocus) {
            restrictions.add(createRestriction(eachNodeId));
        }
        return new AnyRestriction(restrictions.toArray(new Restriction[restrictions.size()]));
    }

    /**
     * Gets the node ids from the given vertices. A node id can only be extracted from a vertex with a "nodes"' namespace.
     * For a vertex with namespace "node" the "getId()" method always returns the node id.
     *
     * @param vertices
     * @return
     */
    private List<Integer> extractNodeIds(Collection<VertexRef> vertices) {
        List<Integer> nodeIdList = new ArrayList<Integer>();
        for (VertexRef eachRef : vertices) {
            if ("nodes".equals(eachRef.getNamespace())) {
                try {
                    nodeIdList.add(Integer.valueOf(eachRef.getId()));
                } catch (NumberFormatException e) {
                    LoggerFactory.getLogger(this.getClass()).warn("Cannot filter nodes with ID: {}", eachRef.getId());
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
                    LoggerFactory.getLogger(this.getClass()).warn("Cannot filter category with ID: {} children: {}", eachRef.getId(), ((GroupRef) eachRef).getChildren());

                }
            }
        }
        return nodeIdList;
    }
}
