/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.support;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.ResourceType;
import org.opennms.netmgt.collection.support.builder.GenericTypeResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.model.ResourcePath;

public class GenericTypeResourceTest {

    /**
     * Validates the behavior of instance name sanitization in the GenericTypeResource
     */
    @Test
    public void canSanitizeResourcePath() {
        // Alpha-numerics should always be unfiltered
        assertEquals("instance", getInstanceInResourcePath("instance"));
        assertEquals("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890",
                getInstanceInResourcePath("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"));
        // Spaces are filtered, contiguous segments are replaced with a single _
        assertEquals("instance_", getInstanceInResourcePath("instance "));
        assertEquals("instance_", getInstanceInResourcePath("instance \t"));
        assertEquals("instance_a_", getInstanceInResourcePath("instance \t a \t "));
        // Colons are filtered
        assertEquals("instance_", getInstanceInResourcePath("instance:"));
        assertEquals("instance__", getInstanceInResourcePath("instance::"));
        // Backslashes are filtered
        assertEquals("instance_", getInstanceInResourcePath("instance\\"));
        assertEquals("instance__", getInstanceInResourcePath("instance\\\\"));
        // Square bracket are filtered
        assertEquals("instance_", getInstanceInResourcePath("instance["));
        assertEquals("instance__", getInstanceInResourcePath("instance[["));
        assertEquals("instance_", getInstanceInResourcePath("instance]"));
        assertEquals("instance__", getInstanceInResourcePath("instance]]"));
        // Other characters that may be illegal in Windows paths are NOT currently filtered
        assertEquals("instance/", getInstanceInResourcePath("instance/"));
        assertEquals("instance*", getInstanceInResourcePath("instance*"));
        assertEquals("instance|", getInstanceInResourcePath("instance|"));
        assertEquals("instance<", getInstanceInResourcePath("instance<"));
        assertEquals("instance>", getInstanceInResourcePath("instance>"));
    }

    private String getInstanceInResourcePath(String instance) {
        // Mock the ResourceType
        ResourceType rt = mock(ResourceType.class, RETURNS_DEEP_STUBS);
        when(rt.getName()).thenReturn("type");
        when(rt.getStorageStrategy().getClazz()).thenReturn(IndexStorageStrategy.class.getCanonicalName());
        when(rt.getStorageStrategy().getParameters()).thenReturn(Collections.emptyList());
        when(rt.getPersistenceSelectorStrategy().getClazz()).thenReturn(PersistAllSelectorStrategy.class.getCanonicalName());
        when(rt.getPersistenceSelectorStrategy().getParameters()).thenReturn(Collections.emptyList());

        // Create the GenericTypeResource
        NodeLevelResource nlr = new NodeLevelResource(1);
        GenericTypeResource gtr = new GenericTypeResource(nlr, rt, instance);

        // Mock the CollectionResource
        CollectionResource resource = mock(CollectionResource.class);
        when(resource.getInstance()).thenReturn(gtr.getInstance());

        // Build the resource path, and extract the instance (the last element of the path)
        ResourcePath path = gtr.getPath(resource);
        String[] elements = path.elements();
        return elements[elements.length - 1];
    }
}
