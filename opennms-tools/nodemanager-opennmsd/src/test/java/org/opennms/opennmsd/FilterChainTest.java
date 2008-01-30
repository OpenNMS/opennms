/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.opennmsd;

import junit.framework.TestCase;

/**
 * FilterChainTest
 *
 * @author brozow
 */
public class FilterChainTest extends TestCase {
    
    NNMEvent m_event;
    FilterChainBuilder m_chainBldr;
    
    public void setUp() {
        m_chainBldr = new FilterChainBuilder();
        
        m_event = TestNNMEvent.createEvent("Category", "Severity", "name", "1.1.1.1");
    }
    
    public void testNoMatchingFilter() {
        
        m_chainBldr.newFilter();
        m_chainBldr.setCategoryMatchPattern("nomatch");
        m_chainBldr.setAction(Filter.ACCEPT);
        
        assertEquals(Filter.DISCARD, m_chainBldr.getChain().filterEvent(m_event));
    }
    
    public void testOneFilterThatMatches() {
        m_chainBldr.newFilter();
        m_chainBldr.setCategoryMatchPattern("Category");
        m_chainBldr.setAction(Filter.ACCEPT);
        
        assertEquals(Filter.ACCEPT, m_chainBldr.getChain().filterEvent(m_event));
        
    }
    
    public void testTwoFiltersFirstMatches() {
        m_chainBldr.newFilter();
        m_chainBldr.setCategoryMatchPattern("Category");
        m_chainBldr.setAction(Filter.ACCEPT);
        
        m_chainBldr.newFilter();
        m_chainBldr.setAddressMatchPattern("1.1.1.2");
        m_chainBldr.setAction(Filter.PRESERVE);
        
        assertEquals(Filter.ACCEPT, m_chainBldr.getChain().filterEvent(m_event));        
    }

    public void testTwoFiltersSecondMatches() {
        m_chainBldr.newFilter();
        m_chainBldr.setCategoryMatchPattern("Categorx");
        m_chainBldr.setAction(Filter.ACCEPT);
        
        m_chainBldr.newFilter();
        m_chainBldr.setAddressMatchPattern("1.1.1.1");
        m_chainBldr.setAction(Filter.PRESERVE);
        
        assertEquals(Filter.PRESERVE, m_chainBldr.getChain().filterEvent(m_event));        
    }
 
    public void testTwoFiltersBothMatches() {
        m_chainBldr.newFilter();
        m_chainBldr.setCategoryMatchPattern("Category");
        m_chainBldr.setAction(Filter.ACCEPT);
        
        m_chainBldr.newFilter();
        m_chainBldr.setAddressMatchPattern("1.1.1.1");
        m_chainBldr.setAction(Filter.PRESERVE);
        
        // action belongs to first matcher
        assertEquals(Filter.ACCEPT, m_chainBldr.getChain().filterEvent(m_event));        
    }

}
