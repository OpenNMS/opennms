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

import com.vaadin.v7.data.Property;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.Table.CellStyleGenerator;

public class AlarmTableCellStyleGenerator implements CellStyleGenerator {

    private static final long serialVersionUID = 5083664924723259566L;

    private final AlarmCellStyleRenderer renderer = new AlarmCellStyleRenderer();

    @Override
    public String getStyle(Table source, Object itemId, Object propertyId) {
        if (propertyId == null && source.getContainerProperty(itemId, "severityId") != null) {
            Integer severity = (Integer) source.getContainerProperty(itemId, "severityId").getValue();
            Property<?> prop = source.getContainerProperty(itemId, "acknowledged");
            Boolean acknowledged = false;
            if (prop != null) {
                acknowledged = (Boolean) prop.getValue();
            }
            return renderer.getStyle(severity, acknowledged.booleanValue());
        } else if ("severity".equals(propertyId)) { 
            return "bright"; 
        }
        return null;
    }


}
