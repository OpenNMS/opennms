/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;

public class NodeSnmpResourceTypeTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void canGetChildByName() throws IOException {
        final OnmsNode node = createMock(OnmsNode.class);
        final OnmsResource parent = createMock(OnmsResource.class);
        
        expect(node.getId()).andReturn(1);
        expect(parent.getId()).andReturn("node[1]");
        expect(parent.getEntity()).andReturn(node);
        replay(parent, node);

        final DefaultResourceDao resourceDao = new DefaultResourceDao();
        resourceDao.setRrdDirectory(tempFolder.getRoot());

        final NodeSnmpResourceType nodeSnmpResourceType = new NodeSnmpResourceType(resourceDao);
        
        final OnmsResource resource = nodeSnmpResourceType.getChildByName(parent, new String(""));
        assertEquals("node[1].nodeSnmp[]", resource.getId());
        assertEquals(parent, resource.getParent());
    }
}
