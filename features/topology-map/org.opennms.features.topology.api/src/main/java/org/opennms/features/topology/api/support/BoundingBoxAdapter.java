/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.support;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.opennms.features.topology.api.BoundingBox;

/**
 */
public class BoundingBoxAdapter extends XmlAdapter<BoundingBoxAdapter.JaxbBoundingBox, BoundingBox> {

	public static final class JaxbBoundingBox {
		@XmlAttribute
		public int x;
		@XmlAttribute
		public int y;
		@XmlAttribute
		public int height;
		@XmlAttribute
		public int width;
	}

	@Override
	public BoundingBoxAdapter.JaxbBoundingBox marshal(BoundingBox v) throws Exception {
		JaxbBoundingBox retval = new JaxbBoundingBox();
		retval.x = v.getX();
		retval.y = v.getY();
		retval.height = v.getHeight();
		retval.width = v.getWidth();
		return retval;
	}

	@Override
	public BoundingBox unmarshal(BoundingBoxAdapter.JaxbBoundingBox v) throws Exception {
		return new BoundingBox(v.x, v.y, v.width, v.height);
	}
}
