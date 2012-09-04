/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.asterisk.utils;

import java.util.Properties;

import org.opennms.core.utils.PropertiesUtils;

/**
 * Given a pattern, expands embedded Asterisk-related property values
 *
 * @author <A HREF="mailto:jeffg@opennms.org">Jeff Gehlbach</A>
 * @param pattern A pattern to expand
 * @version $Id: $
 */
public class AsteriskUtils {
    /**
     * <p>expandPattern</p>
     *
     * @param pattern a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String expandPattern(String pattern) {
        Properties props = new Properties();
        props.put("org.opennms.netmgt.asterisk.agi.listenAddress", System.getProperty("org.opennms.netmgt.asterisk.agi.listenAddress", "127.0.0.1"));
        props.put("org.opennms.netmgt.asterisk.agi.listenPort", System.getProperty("org.opennms.netmgt.asterisk.agi.listenPort", "4573"));
        String expanded = PropertiesUtils.substitute(pattern, props);
        return expanded;
    }
}
