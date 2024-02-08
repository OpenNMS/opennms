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
package org.opennms.netmgt.config;

import java.util.Date;
import java.util.List;

import org.opennms.core.utils.Owner;

/**
 * <p>CalendarEntry class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class CalendarEntry {
    
    private final Date m_start;
    private final Date m_end;
    private final String m_descr;
    private final List<Owner> m_labels;
    
    /**
     * <p>Constructor for CalendarEntry.</p>
     *
     * @param start a {@link java.util.Date} object.
     * @param end a {@link java.util.Date} object.
     * @param descr a {@link java.lang.String} object.
     * @param labels a {@link java.util.List} object.
     */
    public CalendarEntry(Date start, Date end, String descr, List<Owner> labels) {
        m_start = start;
        m_end = end;
        m_descr = descr;
        m_labels = labels;
    }

    /**
     * <p>getStartTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getStartTime() { return m_start; }
    
    /**
     * <p>getEndTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getEndTime() { return m_end; }
    
    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() { return m_descr; }
    
    /**
     * <p>getLabels</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Owner> getLabels() { return m_labels; }
}
