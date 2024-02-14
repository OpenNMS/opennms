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
package org.opennms.features.vaadin.dashboard.dashlets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourceId;

public class KscDashletTest {
    @Test
    public void shouldDeterminNodeIdFromResourceId() {
        String resourceIdString = "node[Test:1525957453778].interfaceSnmp[opennms-jvm]";
        ResourceId resourceId = ResourceId.fromString(resourceIdString);
        ResourceDao resourceDao = Mockito.mock(ResourceDao.class);
        OnmsResource resource = Mockito.mock(OnmsResource.class);
        OnmsResource parentResource = Mockito.mock(OnmsResource.class);

        OnmsNode node = new OnmsNode();
        node.setId(3);

        when(resourceDao.getResourceById(resourceId)).thenReturn(resource);
        when(resource.getParent()).thenReturn(parentResource);
        when(parentResource.getEntity()).thenReturn(node);

        KscDashlet dashlet = new KscDashlet(
                this.getClass().getSimpleName(),
                null,
                null,
                resourceDao,
                null,
                null);
        assertEquals(parentResource, dashlet.determineResourceByResourceId(resourceId));
    }
}