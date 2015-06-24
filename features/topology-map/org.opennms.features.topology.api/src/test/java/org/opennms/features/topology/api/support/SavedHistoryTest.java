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

package org.opennms.features.topology.api.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXB;

import org.junit.Test;
import org.opennms.features.topology.api.BoundingBox;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.VertexRef;

public class SavedHistoryTest {
	@Test
	public void testMarshall() {
		Map<String,String> settings = new HashMap<String,String>();
		settings.put("hello", "world");

		VertexRef vert1 = new DefaultVertexRef("nodes", "1");
		VertexRef vert2 = new DefaultVertexRef("nodes", "2", "HasALabel");

		Map<VertexRef,Point> locations = new HashMap<VertexRef,Point>();
		locations.put(vert1, new Point(0, 0));
		locations.put(vert2, new Point(0, 0));

		SavedHistory savedHistory = new SavedHistory(
				0, 
				new BoundingBox(0,0,100,100), 
				locations,
				Collections.singleton(vert2),
				Collections.<VertexRef>emptySet(),
				settings
		);
		JAXB.marshal(savedHistory, System.out);

		// Specify a focus node
		savedHistory = new SavedHistory(
				0, 
				new BoundingBox(0,0,100,100), 
				locations,
				Collections.singleton(vert2),
				Collections.singleton(vert1),
				settings
		);
		JAXB.marshal(savedHistory, System.out);
	}
}
