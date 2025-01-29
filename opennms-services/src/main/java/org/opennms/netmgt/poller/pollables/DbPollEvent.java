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
package org.opennms.netmgt.poller.pollables;

import java.util.Date;


/**
 * Represents a DbPollEvent
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class DbPollEvent extends PollEvent {
    
    private final long m_eventId;
    private final Date m_date;
    
    /**
     * <p>Constructor for DbPollEvent.</p>
     *
     * @param eventId a int.
     * @param uei a {@link java.lang.String} object.
     * @param date a {@link java.util.Date} object.
     */
    public DbPollEvent(int eventId, String uei, Date date) {
        super(Scope.fromUei(uei));
        m_eventId = eventId;
        m_date = date;
    }
    
    /**
     * <p>getEventId</p>
     *
     * @return a int.
     */
    @Override
    public long getEventId() {
        return m_eventId;
    }
    
    /**
     * <p>getDate</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Override
    public Date getDate() {
        return m_date;
    }
    
    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    @Override
    public int hashCode() { return Long.valueOf(m_eventId).hashCode(); }
    
    /**
     * <p>equals</p>
     *
     * @param e a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     * @return a boolean.
     */
    public boolean equals(PollEvent e) {
        if (e == null) return false;
        return m_eventId == e.getEventId();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (o instanceof PollEvent)
            return equals((PollEvent)o);
        return false;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return "DbPollEvent[ id: "+getEventId()+" ]";
    }

}
