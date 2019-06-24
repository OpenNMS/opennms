/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.support;

import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.Persister;

public class DelegatingPersister implements Persister {

    private final List<Persister> delegates;

    public DelegatingPersister(List<Persister> delegates) {
        this.delegates = Objects.requireNonNull(delegates);
    }

    @Override
    public void persistNumericAttribute(CollectionAttribute attribute) {
        delegates.forEach(p  -> p.persistNumericAttribute(attribute));
    }

    @Override
    public void persistStringAttribute(CollectionAttribute attribute) {
        delegates.forEach(p  -> p.persistStringAttribute(attribute));
    }

    @Override
    public void visitCollectionSet(CollectionSet set) {
        delegates.forEach(p  -> p.visitCollectionSet(set));
    }

    @Override
    public void visitResource(CollectionResource resource) {
        delegates.forEach(p  -> p.visitResource(resource));
    }

    @Override
    public void visitGroup(AttributeGroup group) {
        delegates.forEach(p  -> p.visitGroup(group));
    }

    @Override
    public void visitAttribute(CollectionAttribute attribute) {
        delegates.forEach(p  -> p.visitAttribute(attribute));
    }

    @Override
    public void completeAttribute(CollectionAttribute attribute) {
        delegates.forEach(p  -> p.completeAttribute(attribute));
    }

    @Override
    public void completeGroup(AttributeGroup group) {
        delegates.forEach(p  -> p.completeGroup(group));
    }

    @Override
    public void completeResource(CollectionResource resource) {
        delegates.forEach(p  -> p.completeResource(resource));
    }

    @Override
    public void completeCollectionSet(CollectionSet set) {
        delegates.forEach(p  -> p.completeCollectionSet(set));
    }
}
