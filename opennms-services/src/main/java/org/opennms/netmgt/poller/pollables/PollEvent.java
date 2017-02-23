/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
