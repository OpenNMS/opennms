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
