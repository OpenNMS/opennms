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
package org.opennms.netmgt.dao.support;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourcePath;

public class NodeResourceTypeLinkTest {

    private static String linkFor(final String resourceName) {
        final NodeResourceType type = new NodeResourceType(null, null);
        final OnmsResource resource = new OnmsResource(resourceName, resourceName, type, java.util.Collections.emptySet(), ResourcePath.get("snmp", "node"));
        return type.getLinkForResource(resource);
    }

    @Test
    public void linksANodeIdToVueNodeDetails() {
        assertEquals(NodeResourceType.NODE_DETAILS_URI + "42", linkFor("42"));
    }

    @Test
    public void keepsForeignSourceAndIdAndEncodesReservedCharacters() {
        assertEquals(NodeResourceType.NODE_DETAILS_URI + "SmokeTests:TestMachine1", linkFor("SmokeTests:TestMachine1"));
        assertEquals(NodeResourceType.NODE_DETAILS_URI + "My%20Source:a%2Fb%23c", linkFor("My Source:a/b#c"));
    }
}
