/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.browsers;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.criteria.restrictions.AnyRestriction;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.features.topology.api.topo.Ref;
import org.opennms.features.topology.api.topo.VertexRef;

import java.util.ArrayList;
import java.util.List;

public class NodeIdFocusToRestrictionsConverterTest {
	
	private static class TestVertexRef implements VertexRef {

		private final int id;
		
		private TestVertexRef(int id) {
			this.id = id;
		}
		
		@Override
		public String getId() {
			return Integer.toString(id);
		}

		@Override
		public String getNamespace() {
			return "nodes";
		}

		@Override
		public String getLabel() {
			return getNamespace() + ":" + getId();
		}

		@Override
		public int compareTo(Ref o) {
			return -1;
		}
	}

	@Test
    public void testConvert() {
        NodeIdFocusToRestrictionsConverter converter = new NodeIdFocusToRestrictionsConverter() {
            @Override
            protected Restriction createRestriction(Integer nodeId) {
                return new EqRestriction("dummy", nodeId); // we do not actually need a restriction
            }
        };

        List<VertexRef> vertexRefList = new ArrayList<VertexRef>();
        AnyRestriction anyRestriction = (AnyRestriction)converter.getRestrictions(vertexRefList).get(0);
        Assert.assertFalse(anyRestriction.getRestrictions().isEmpty()); // there should at least be a false restriction
        
        // a Restriction which should always fail
        EqRestriction eqRestriction = (EqRestriction) anyRestriction.getRestrictions().iterator().next();
        Assert.assertEquals("dummy", eqRestriction.getAttribute());
        Assert.assertEquals(-1, eqRestriction.getValue());
        
        // no with a "real" node
        vertexRefList.add(new TestVertexRef(100));
        anyRestriction = (AnyRestriction)converter.getRestrictions(vertexRefList).get(0);
        eqRestriction = (EqRestriction) anyRestriction.getRestrictions().iterator().next();
        Assert.assertFalse(anyRestriction.getRestrictions().isEmpty()); // now a real restriction is there
        Assert.assertEquals("dummy", eqRestriction.getAttribute());
        Assert.assertEquals(100, eqRestriction.getValue());
        
        
    }
}
