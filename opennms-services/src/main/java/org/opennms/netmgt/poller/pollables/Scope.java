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

import org.opennms.netmgt.events.api.EventConstants;

/**
 * <p>Scope class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public enum Scope {
    SERVICE,
    INTERFACE,
    NODE,
    NETWORK;
    
    /**
     * <p>fromUei</p>
     *
     * @param uei a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.poller.pollables.Scope} object.
     */
    public static Scope fromUei(String uei) {
        if (EventConstants.NODE_DOWN_EVENT_UEI.equals(uei)) {
            return NODE;
        } else if (EventConstants.INTERFACE_DOWN_EVENT_UEI.equals(uei)) {
            return INTERFACE;
        } else  if (EventConstants.NODE_LOST_SERVICE_EVENT_UEI.equals(uei)) {
            return SERVICE;
        }
        return null;
    }
    
    /**
     * <p>isLargerThan</p>
     *
     * @param s a {@link org.opennms.netmgt.poller.pollables.Scope} object.
     * @return a boolean.
     */
    public boolean isLargerThan(Scope s) {
        if (s == null) return true;
        return this.ordinal() > s.ordinal();
    }
    
    /**
     * <p>isSmallerThan</p>
     *
     * @param s a {@link org.opennms.netmgt.poller.pollables.Scope} object.
     * @return a boolean.
     */
    public boolean isSmallerThan(Scope s) {
        if (s == null) return false;
        return this.ordinal() < s.ordinal();
    }
    
    /**
     * <p>isLargerThan</p>
     *
     * @param a a {@link org.opennms.netmgt.poller.pollables.Scope} object.
     * @param b a {@link org.opennms.netmgt.poller.pollables.Scope} object.
     * @return a boolean.
     */
    public static boolean isLargerThan(Scope a, Scope b) {
        if (a == null) {
            return false;
        } else {
            if (b == null) {
                return true;
            } else {
                return a.isLargerThan(b);
            }
        }
    }
    
    /**
     * <p>isSmallerThan</p>
     *
     * @param a a {@link org.opennms.netmgt.poller.pollables.Scope} object.
     * @param b a {@link org.opennms.netmgt.poller.pollables.Scope} object.
     * @return a boolean.
     */
    public static boolean isSmallerThan(Scope a, Scope b) {
        return a != b && !isLargerThan(a, b);
    }
    
}
