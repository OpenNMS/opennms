/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.topo;

import java.net.MalformedURLException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;

public class AbstractTopologyProviderTest {

    @Test
    public void testIdGenerator() throws MalformedURLException, JAXBException {
        AbstractTopologyProvider provider = new AbstractTopologyProvider("test") {

            @Override
            public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType type) {
                return SelectionChangedListener.Selection.NONE;
            }

            @Override
            public boolean contributesTo(ContentType type) {
                return true;
            }

            @Override
            public Defaults getDefaults() {
                return new Defaults();
            }

            @Override
            public void refresh() {
                for (int i=0; i<10; i++) 
                    addVertex(0, i);
                
                for (int i=0; i<5; i++)
                    addGroup("group"+i, "group");
                
                for (int i=0; i<2; i++)
                    addEdges(new AbstractEdge("test", getNextEdgeId(), getVertices().get(i), getVertices().get(i+1)));
            }
        };
        provider.refresh();
        
        Assert.assertEquals(10, provider.getVerticesWithoutGroups().size());
        Assert.assertEquals(5,  provider.getGroups().size());
        Assert.assertEquals(15, provider.getVertices().size());
        Assert.assertEquals(2,  provider.getEdges().size());
        
        Assert.assertEquals("e2", provider.getNextEdgeId());
        Assert.assertEquals("g5", provider.getNextGroupId());
        Assert.assertEquals("v10", provider.getNextVertexId());
    }
}
