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
package org.opennms.netmgt.collection.support;

import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import org.opennms.netmgt.collection.api.TimeKeeper;

/**
 * The Class ConstantTimeKeeper.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class ConstantTimeKeeper implements TimeKeeper {

    /** The Fixed Date. */
    private Date m_date;
    
    /**
     * Instantiates a new constant time keeper.
     *
     * @param timestamp the timestamp
     */
    public ConstantTimeKeeper(Date timestamp) {
        m_date = Objects.requireNonNull(timestamp);
    }
    
    /* (non-Javadoc)
     * @see org.opennms.core.utils.TimeKeeper#getCurrentTime()
     */
    @Override
    public long getCurrentTime() {
        return m_date.getTime();
    }

    /* (non-Javadoc)
     * @see org.opennms.core.utils.TimeKeeper#getCurrentDate()
     */
    @Override
    public Date getCurrentDate() {
        return m_date;
    }

    /* (non-Javadoc)
     * @see org.opennms.core.utils.TimeKeeper#getTimeZone()
     */
    @Override
    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }
  
}