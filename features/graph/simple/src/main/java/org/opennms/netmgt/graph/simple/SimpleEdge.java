/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

// TODO MVR fix package name opennsm vs opennms
package org.opennms.netmgt.graph.simple;

import java.util.Objects;

import org.opennms.netmgt.graph.api.Edge;
import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericVertexRef;

/**
 * Acts as a domain specific view on a Edge.
 * Can be extended by domain specific edge classes.
 * It contains no data of it's own but operates on the data of it's wrapped GenericEdge.
 */
public class SimpleEdge implements Edge {

    protected GenericEdge delegate;

    public SimpleEdge(String namespace, SimpleVertexRef source, SimpleVertexRef target) {
        delegate = new GenericEdge(namespace, new GenericVertexRef(source.getNamespace(), source.getId()),
                new GenericVertexRef(target.getNamespace(), target.getId()));
    }

    public SimpleEdge(String namespace, SimpleVertex source, SimpleVertex target) {
        delegate = new GenericEdge(namespace, source.asGenericVertex().getVertexRef(), target.asGenericVertex().getVertexRef());
    }

    public SimpleEdge(GenericEdge genericEdge) {
        this.delegate = genericEdge;
    }

    public SimpleEdge(SimpleEdge copyMe) {
        // copy the delegate to have a clone down to the properties maps
        this(new GenericEdge(copyMe.asGenericEdge()));

        // TODO: patrick, mvr rework when we support edges that connect to other namespaces
        // We must copy the source and target as well, otherwise changing it's properties will change
        // the "copyMe" properties as well
    }

    @Override
    public String getNamespace() {
        return delegate.getNamespace();
    }

    public void setNamespace(String namespace) {
        delegate.setNamespace(namespace);
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public VertexRef getSource() {
        return delegate.getSource();
    }

    @Override
    public VertexRef getTarget() {
        return delegate.getTarget();
    }

    @Override
    public GenericEdge asGenericEdge() {
        return delegate;
    }

    public void setLabel(String label){
        delegate.setLabel(label);
    }

    public String getLabel(){
        return delegate.getLabel();
    }

    private static VertexRef copyVertex(VertexRef ref) {
        if (ref instanceof SimpleVertex) {
            return new SimpleVertex((SimpleVertex) ref);
        }
        return new SimpleVertexRef(ref);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleEdge that = (SimpleEdge) o;
        return Objects.equals(delegate, that.delegate);
    }

    @Override
    public int hashCode() {

        return Objects.hash(delegate);
    }
}
