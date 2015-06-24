/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.rrd.model;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * The Class LongAdapter.
 * <p>The null representation of some long values inside the XML version of an RRD is expressed as 'U'</p>
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class LongAdapter extends XmlAdapter<String, Long> {

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(Long value) throws Exception {
        if (value == null) {
            return null;
        }
        if (value == Long.MIN_VALUE) {
            return "-inf";
        }
        if (value == Long.MAX_VALUE) {
            return "inf";
        }
        return value.toString();
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public Long unmarshal(String value) throws Exception {
        final String v = value.trim();
        if (v.equalsIgnoreCase("u")) {
            return null;
        }
        if (v.equalsIgnoreCase("-inf")) {
            return Long.MIN_VALUE;
        }
        if (v.equalsIgnoreCase("inf")) {
            return Long.MAX_VALUE;
        }
        return new Long(v);
    }

}
