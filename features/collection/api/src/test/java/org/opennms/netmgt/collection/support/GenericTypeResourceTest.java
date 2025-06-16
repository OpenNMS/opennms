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
        if (elements != null && elements.length > 0) {
            return elements[elements.length - 1];
        }
        return null;
    }
}
