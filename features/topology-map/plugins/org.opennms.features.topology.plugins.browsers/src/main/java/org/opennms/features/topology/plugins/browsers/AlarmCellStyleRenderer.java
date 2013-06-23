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

import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;

public class AlarmCellStyleRenderer {

    public String getStyle(Integer severityObject, boolean acknowledged) {
        int severity = severityObject == null ? OnmsSeverity.INDETERMINATE.getId() : severityObject.intValue();
        StringBuffer retVal = new StringBuffer();
        
        if (OnmsSeverity.CLEARED.getId() == severity) {
        } else if (OnmsSeverity.CRITICAL.getId() == severity) {
            retVal.append("alarm-critical");
        } else if (OnmsSeverity.INDETERMINATE.getId() == severity) {
            retVal.append("alarm-indeterminate");
        } else if (OnmsSeverity.MAJOR.getId() == severity) {
            retVal.append("alarm-major");
        } else if (OnmsSeverity.MINOR.getId() == severity) {
            retVal.append("alarm-minor");
        } else if (OnmsSeverity.NORMAL.getId() == severity) {
            retVal.append("alarm-normal");
        } else if (OnmsSeverity.WARNING.getId() == severity) {
            retVal.append("alarm-warning");
        }

        if (!acknowledged) {
            retVal.append("-noack");
        }
        return retVal.toString();
    }

    public String getStyle(OnmsAlarm alarm) {
        if (alarm == null) return getStyle(null, false);
        return getStyle(alarm.getSeverityId(), alarm.isAcknowledged());
    }
}
