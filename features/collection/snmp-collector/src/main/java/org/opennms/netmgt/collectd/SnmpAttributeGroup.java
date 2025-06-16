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
package org.opennms.netmgt.collectd;

import java.util.StringTokenizer;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionResource;

/**
 * This attribute group overrides {@link #doShouldPersist()} so that persistence
 * can be enabled if the SNMP ifType matches the value of ifType on the 
 * {@link CollectionResource}.
 */
public class SnmpAttributeGroup extends AttributeGroup {

	public SnmpAttributeGroup(SnmpCollectionResource resource, AttributeGroupType groupType) {
		super(resource, groupType);
	}

	/**
	 * 
	 */
	@Override
	protected boolean doShouldPersist() {
		boolean shouldPersist = super.doShouldPersist();

		if (shouldPersist) {
			return shouldPersist;
		} else {
			String type = String.valueOf(((SnmpCollectionResource)getResource()).getSnmpIfType());

			if (type.equals(getIfType())) return true;

			StringTokenizer tokenizer = new StringTokenizer(getIfType(), ",");
			while(tokenizer.hasMoreTokens()) {
				if (type.equals(tokenizer.nextToken()))
					return true;
			}
			return false;
		}
	}
}
