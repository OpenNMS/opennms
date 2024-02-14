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
package org.opennms.features.topology.plugins.browsers;

import org.opennms.netmgt.model.OnmsIpInterface;

import com.vaadin.v7.data.Property;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.Table.ColumnGenerator;

public class OnmsIpInterfaceGenerator implements ColumnGenerator {

	private static final long serialVersionUID = 7806832669018164281L;

	@Override
	public Object generateCell(Table source, Object itemId, Object columnId) {
		Property<OnmsIpInterface> property = source.getContainerProperty(itemId, columnId);
		if (property == null || property.getValue() == null) {
			return null;
		} else {
			return property.getValue().getIpAddressAsString();
		}
	}
}
