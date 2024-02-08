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
