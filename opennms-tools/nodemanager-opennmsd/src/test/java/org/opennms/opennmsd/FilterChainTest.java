/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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
        
        m_event = MockNNMEvent.createEvent("Category", "Severity", "name", "1.1.1.1");
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
