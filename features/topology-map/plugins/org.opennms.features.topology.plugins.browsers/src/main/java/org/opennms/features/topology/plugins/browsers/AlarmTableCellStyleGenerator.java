/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc. OpenNMS(R) is a
 * registered trademark of The OpenNMS Group, Inc. OpenNMS(R) is free software:
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version. OpenNMS(R) is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with
 * OpenNMS(R). If not, see: http://www.gnu.org/licenses/ For more information
 * contact: OpenNMS(R) Licensing <license@opennms.org> http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.topology.plugins.browsers;

import com.vaadin.data.Property;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;

public class AlarmTableCellStyleGenerator implements CellStyleGenerator {

    private static final long serialVersionUID = 5083664924723259566L;

    private final AlarmCellStyleRenderer renderer = new AlarmCellStyleRenderer();

    @Override
    public String getStyle(Table source, Object itemId, Object propertyId) {
        if (propertyId == null && source.getContainerProperty(itemId, "severityId") != null) {
            StringBuffer retval = new StringBuffer();
            Integer severity = (Integer) source.getContainerProperty(itemId, "severityId").getValue();
            Property prop = source.getContainerProperty(itemId, "acknowledged");
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
