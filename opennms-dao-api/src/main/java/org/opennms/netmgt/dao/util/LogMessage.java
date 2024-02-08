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
package org.opennms.netmgt.dao.util;

import org.opennms.netmgt.events.api.EventDatabaseConstants;
import org.opennms.netmgt.xml.event.Logmsg;

/**
 * This is an utility class used to format the event log message info - to be
 * inserted into the 'events' table
 *
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 */
public abstract class LogMessage {
    /**
     * Format the logmsg entry
     *
     * @param msg
     *            the logmsg
     * @return the formatted logmsg
     */
    public static String format(Logmsg msg) {
        String txt = EventDatabaseConstants.escape(msg.getContent(), EventDatabaseConstants.DB_ATTRIB_DELIM);
        String log = msg.getDest();

        String fmsg = txt + EventDatabaseConstants.DB_ATTRIB_DELIM + log;
        if (fmsg.length() >= 256)
            fmsg = fmsg.substring(0, 252) + EventDatabaseConstants.VALUE_TRUNCATE_INDICATOR;

        return fmsg;
    }
}
