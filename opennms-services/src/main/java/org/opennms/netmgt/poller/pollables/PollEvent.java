/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2004-2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.poller.pollables;

import java.util.Date;



/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
abstract public class PollEvent {
    
    Scope m_scope;
    
    protected PollEvent(Scope scope) {
        m_scope = scope;
    }
    
    public Scope getScope() {
        return m_scope;
    }

    abstract public int getEventId();

    abstract public Date getDate();
    
    public boolean isNodeDown() {
        return getScope().equals(Scope.NODE); 
    }

    public boolean isInterfaceDown() {
        return getScope().equals(Scope.INTERFACE);
    }
    
    public boolean isNodeLostService() {
        return getScope().equals(Scope.SERVICE);
    }
    
    public boolean hasLargerScopeThan(PollEvent e) {
        return Scope.isLargerThan(this.getScope(), e.getScope());
    }
    
    public boolean hasSmallerScopeThan(PollEvent e) {
        return Scope.isSmallerThan(this.getScope(), e.getScope());
    }
    
    public boolean hasScopeLargerThan(Scope scope) {
        return Scope.isLargerThan(this.getScope(), scope);
    }
    
    public boolean hasScopeSmallerThan(Scope scope) {
        return Scope.isSmallerThan(this.getScope(), scope);
    }
    
    public boolean hasSameScope(PollEvent e) {
        return this.getScope() == e.getScope();
    }
    
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
