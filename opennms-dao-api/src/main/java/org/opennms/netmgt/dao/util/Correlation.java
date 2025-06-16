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

import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.events.api.EventDatabaseConstants;
import org.springframework.dao.DataAccessException;

/**
 * This is an utility class used to format the event correlation info - to be
 * inserted into the 'events' table - it simply returns the correlation as an
 * 'XML' block
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @see org.opennms.netmgt.events.api.EventDatabaseConstants#VALUE_TRUNCATE_INDICATOR
 */
public abstract class Correlation {
    private static final Logger LOG = LoggerFactory.getLogger(Correlation.class);
    /**
     * Format the correlation block to have the xml
     *
     * @param ec
     *            the correlation
     * @param sz
     *            the size to which the formatted string is to be limited
     *            to(usually the size of the column in the database)
     * @return the formatted event correlation
     */
    public static String format(final org.opennms.netmgt.xml.event.Correlation ec, final int sz) {
        StringWriter out = new StringWriter();
        try {
            JaxbUtils.marshal(ec, out);
        } catch (DataAccessException e) {
            LOG.error("Failed to convert new event to XML", e);
            return null;
        }

        String outstr = out.toString();
        if (outstr.length() >= sz) {
            final StringBuilder buf = new StringBuilder(outstr);

            buf.setLength(sz - 4);
            buf.append(EventDatabaseConstants.VALUE_TRUNCATE_INDICATOR);

            return buf.toString();
        } else {
            return outstr;
        }

    }

}
