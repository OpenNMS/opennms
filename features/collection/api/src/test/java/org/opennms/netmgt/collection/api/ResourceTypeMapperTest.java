/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class ResourceTypeMapperTest {

    @Test
    public void lookupWithFallback() {
        ResourceTypeMapper.getInstance().setResourceTypeMapper(k -> {
            if ("other".equals(k)) {
                ResourceType rt = mock(ResourceType.class);
                when(rt.getName()).thenReturn("other");
                return rt;
            }
            return null;
        });

        // The "indexed" type does not exist, so it should return null
        assertNull(ResourceTypeMapper.getInstance().getResourceType("indexed"));

        // The "other" type does exist
        ResourceType rt = ResourceTypeMapper.getInstance().getResourceType("other");
        assertNotNull(rt);
        assertEquals("other", rt.getName());

        // Retrieving the "indexed" type while specifying "other" as a fallback should return a valid resource type
        rt = ResourceTypeMapper.getInstance().getResourceTypeWithFallback("indexed", "other");
        assertNotNull(rt);
        // However, the name of the resource type should reflect the requested type (and not the name of the fallback)
        // (This is done in order to preserve existing behavior.)
        assertEquals("indexed", rt.getName());
    }
}
