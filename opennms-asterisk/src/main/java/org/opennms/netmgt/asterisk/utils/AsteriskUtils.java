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
public abstract class AsteriskUtils {
    /**
     * <p>expandPattern</p>
     *
     * @param pattern a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String expandPattern(final String pattern) {
        final Properties props = new Properties();
        props.put("org.opennms.netmgt.asterisk.agi.listenAddress", System.getProperty("org.opennms.netmgt.asterisk.agi.listenAddress", "127.0.0.1"));
        props.put("org.opennms.netmgt.asterisk.agi.listenPort", System.getProperty("org.opennms.netmgt.asterisk.agi.listenPort", "4573"));
        return PropertiesUtils.substitute(pattern, props);
    }
}
