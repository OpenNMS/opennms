/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.provider.bsm;

import static org.opennms.netmgt.graph.provider.bsm.BusinessServiceGraphProvider.NAMESPACE;

import java.util.Set;

import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.simple.SimpleVertex;

public abstract class AbstractBusinessServiceVertex extends SimpleVertex {

    enum Type {
        BusinessService,
        IpService,
        ReductionKey,
        Application
    }

    private final static String PROPERTY_LEVEL = "level";
    private final static String PROPERTY_TYPE = "type";
    private final static String PROPERTY_REDUCTION_KEYS = "reductionKeys";
    private final static String PROPERTY_IS_LEAF = "isLeaf";

    /**
     * Creates a new {@link AbstractBusinessServiceVertex}.
     *  @param id the unique id of this vertex. Must be unique overall the namespace.
     * @param label a human readable label
     * @param level the level of the vertex in the Business Service Hierarchy. The root element is level 0.
     */
    protected AbstractBusinessServiceVertex(String id, String label, int level, Type type, boolean isLeaf,
                                            Set<String> reductionKeys) {
        super(NAMESPACE, id);
        setLabel(label);
        setLevel(level);
        setType(type);
        setIsLeaf(isLeaf);
        setReductionKeys(reductionKeys);
    }

    public int getLevel() {
        return this.delegate.getProperty(PROPERTY_LEVEL);
    }

    public void setLevel(int level){
        this.delegate.setProperty(PROPERTY_LEVEL, level);
    }

    public boolean isLeaf(){
        return this.delegate.getProperty(PROPERTY_IS_LEAF);
    }

    public void setIsLeaf(boolean isLeaf){
        this.delegate.setProperty(PROPERTY_IS_LEAF, isLeaf);
    }

    public Type getType() {
        return this.delegate.getProperty(PROPERTY_TYPE);
    }

    public void setType(Type type) {
        this.delegate.setProperty(PROPERTY_TYPE, type);
    }

    public Set<String> getReductionKeys() {
        return this.delegate.getProperty(PROPERTY_REDUCTION_KEYS);
    }

    public void setReductionKeys(Set<String> reductionKeys) {
        this.delegate.setProperty(PROPERTY_REDUCTION_KEYS, reductionKeys);// TODO MVR collections cannot be persisted
    }

}
