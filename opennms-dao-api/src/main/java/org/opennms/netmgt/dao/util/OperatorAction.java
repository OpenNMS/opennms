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

import java.util.List;

import org.opennms.netmgt.events.api.EventDatabaseConstants;
import org.opennms.netmgt.xml.event.Operaction;

/**
 * This is an utility class used to format the event operator actions info - to
 * be inserted into the 'events' table. This class only uses the operator action
 * and operator state information - the operator menu goes into a separate
 * database field
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 */
public abstract class OperatorAction {
    /**
     * Format each operator action entry
     *
     * @param opact
     *            the entry
     * @return the formatted string
     */
    public static String format(Operaction opact) {
        String text = opact.getContent();
        String state = opact.getState();

        return EventDatabaseConstants.escape(text, EventDatabaseConstants.DB_ATTRIB_DELIM) + EventDatabaseConstants.DB_ATTRIB_DELIM + state;

    }

    /**
     * Format the list of operator action entries of the event
     *
     * @param opacts
     *            the list
     * @param sz
     *            the size to which the formatted string is to be limited
     *            to(usually the size of the column in the database)
     * @return the formatted string
     */
    public static String format(List<Operaction> opacts, int sz) {
        final StringBuilder buf = new StringBuilder();
        boolean first = true;

        for (Operaction opact : opacts) {
            if (!first) {
                buf.append(EventDatabaseConstants.MULTIPLE_VAL_DELIM);
            } else {
                first = false;
            }

            buf.append(EventDatabaseConstants.escape(format(opact), EventDatabaseConstants.MULTIPLE_VAL_DELIM));
        }

        if (buf.length() >= sz) {
            buf.setLength(sz - 4);
            buf.append(EventDatabaseConstants.VALUE_TRUNCATE_INDICATOR);
        }

        return buf.toString();
    }
}
