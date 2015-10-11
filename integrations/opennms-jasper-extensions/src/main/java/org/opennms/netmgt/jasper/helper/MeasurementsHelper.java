/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jasper.helper;

import com.google.common.base.Strings;

/**
 * Provides helper methods for the {@link org.opennms.netmgt.jasper.measurement.MeasurementDataSource}.
 */
public abstract class MeasurementsHelper {

    private MeasurementsHelper() {

    }

    /**
     * Returns the description of the interface (e.g. eth0-000000).
     *
     * @param snmpifname
     * @param snmpifdescr
     * @param snmpphysaddr
     * @return the description of the interface (e.g. eth0-000000).
     */
    public static String getInterfaceDescriptor(String snmpifname, String snmpifdescr, String snmpphysaddr) {
        return RrdLabelUtils.computeLabelForRRD(snmpifname, snmpifdescr, snmpphysaddr);
    }

    /**
     * Returns the descriptor of the node or node source, depending on the input parameters  (e.g. node[&lt;nodeId&gt;] or nodeSource[&lt;foreignSource&gt;:&lt;foreignId&gt;].
     *
     * @param nodeId
     * @param foreignSource
     * @param foreignId
     * @return the descriptor of the node or node source, depending on the input parameters (e.g. node[&lt;nodeId&gt;] or nodeSource[&lt;foreignSource&gt;:&lt;foreignId&gt;].
     */
    public static String getNodeOrNodeSourceDescriptor(String nodeId, String foreignSource, String foreignId) {
        if (!Strings.isNullOrEmpty(foreignSource) && !Strings.isNullOrEmpty(foreignId)) {
            return String.format("nodeSource[%s:%s]", foreignSource, foreignId);
        }
        return String.format("node[%s]", nodeId);
    }
}
