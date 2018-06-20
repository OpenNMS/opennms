/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2017 The OpenNMS Group, Inc.
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
                null);
        assertEquals(parentResource, dashlet.determineResourceByResourceId(resourceId));
    }
}