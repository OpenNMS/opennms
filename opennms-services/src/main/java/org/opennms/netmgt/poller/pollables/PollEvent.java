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
 * <p>Abstract PollEvent class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public abstract class PollEvent {
    
    private final Scope m_scope;
    
    /**
     * <p>Constructor for PollEvent.</p>
     *
     * @param scope a {@link org.opennms.netmgt.poller.pollables.Scope} object.
     */
    protected PollEvent(Scope scope) {
        m_scope = scope;
    }
    
    /**
     * <p>getScope</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.Scope} object.
     */
    public final Scope getScope() {
        return m_scope;
    }

    /**
     * <p>getEventId</p>
     *
     * @return a int.
     */
    public abstract int getEventId();

    /**
     * <p>getDate</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public abstract Date getDate();
    
    /**
     * <p>isNodeDown</p>
     *
     * @return a boolean.
     */
    public boolean isNodeDown() {
        return getScope().equals(Scope.NODE); 
    }

    /**
     * <p>isInterfaceDown</p>
     *
     * @return a boolean.
     */
    public boolean isInterfaceDown() {
        return getScope().equals(Scope.INTERFACE);
    }
    
    /**
     * <p>isNodeLostService</p>
     *
     * @return a boolean.
     */
    public boolean isNodeLostService() {
        return getScope().equals(Scope.SERVICE);
    }
    
    /**
     * <p>hasLargerScopeThan</p>
     *
     * @param e a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     * @return a boolean.
     */
    public boolean hasLargerScopeThan(PollEvent e) {
        return Scope.isLargerThan(this.getScope(), e.getScope());
    }
    
    /**
     * <p>hasSmallerScopeThan</p>
     *
     * @param e a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     * @return a boolean.
     */
    public boolean hasSmallerScopeThan(PollEvent e) {
        return Scope.isSmallerThan(this.getScope(), e.getScope());
    }
    
    /**
     * <p>hasScopeLargerThan</p>
     *
     * @param scope a {@link org.opennms.netmgt.poller.pollables.Scope} object.
     * @return a boolean.
     */
    public boolean hasScopeLargerThan(Scope scope) {
        return Scope.isLargerThan(this.getScope(), scope);
    }
    
    /**
     * <p>hasScopeSmallerThan</p>
     *
     * @param scope a {@link org.opennms.netmgt.poller.pollables.Scope} object.
     * @return a boolean.
     */
    public boolean hasScopeSmallerThan(Scope scope) {
        return Scope.isSmallerThan(this.getScope(), scope);
    }
    
    /**
     * <p>hasSameScope</p>
     *
     * @param e a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     * @return a boolean.
     */
    public boolean hasSameScope(PollEvent e) {
        return this.getScope() == e.getScope();
    }
    
    /**
     * <p>withLargestScope</p>
     *
     * @param a a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     * @param b a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     * @return a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     */
    public static PollEvent withLargestScope(PollEvent a, PollEvent b) {
        if (a == null) return b;
        if (b == null) return a;
        if (b.hasLargerScopeThan(a)) {
            return b;
        } else {
            return a;
        }
    }


}
