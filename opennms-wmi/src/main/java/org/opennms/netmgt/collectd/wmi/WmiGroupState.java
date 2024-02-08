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
package org.opennms.netmgt.collectd.wmi;

import java.util.Date;

/**
 * <p>WmiGroupState class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class WmiGroupState {
    private boolean available = false;
    private Date lastChecked;

    /**
     * <p>Constructor for WmiGroupState.</p>
     *
     * @param isAvailable a boolean.
     */
    public WmiGroupState(final boolean isAvailable) {
        this(isAvailable, new Date());
    }

    /**
     * <p>Constructor for WmiGroupState.</p>
     *
     * @param isAvailable a boolean.
     * @param lastChecked a {@link java.util.Date} object.
     */
    public WmiGroupState(final boolean isAvailable, final Date lastChecked) {
        this.available = isAvailable;
        this.lastChecked = lastChecked;
    }

    /**
     * <p>isAvailable</p>
     *
     * @return a boolean.
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * <p>Setter for the field <code>available</code>.</p>
     *
     * @param available a boolean.
     */
    public void setAvailable(final boolean available) {
        this.available = available;
    }

    /**
     * <p>Getter for the field <code>lastChecked</code>.</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getLastChecked() {
        return lastChecked;
    }

    /**
     * <p>Setter for the field <code>lastChecked</code>.</p>
     *
     * @param lastChecked a {@link java.util.Date} object.
     */
    public void setLastChecked(final Date lastChecked) {
        this.lastChecked = lastChecked;
    }
}
