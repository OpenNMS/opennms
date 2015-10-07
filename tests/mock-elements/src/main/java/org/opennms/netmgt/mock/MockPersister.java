/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.mock;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.Persister;

public class MockPersister implements Persister {

    @Override
    public void visitCollectionSet(CollectionSet set) {
        // pass
    }

    @Override
    public void visitResource(CollectionResource resource) {
        // pass
    }

    @Override
    public void visitGroup(AttributeGroup group) {
        // pass
    }

    @Override
    public void visitAttribute(CollectionAttribute attribute) {
        // pass
    }

    @Override
    public void completeAttribute(CollectionAttribute attribute) {
        // pass 
    }

    @Override
    public void completeGroup(AttributeGroup group) {
        // pass
    }

    @Override
    public void completeResource(CollectionResource resource) {
        // pass
    }

    @Override
    public void completeCollectionSet(CollectionSet set) {
        // pass
    }

    @Override
    public void persistNumericAttribute(CollectionAttribute attribute) {
        // pass
    }

    @Override
    public void persistStringAttribute(CollectionAttribute attribute) {
        // pass
    }
}
