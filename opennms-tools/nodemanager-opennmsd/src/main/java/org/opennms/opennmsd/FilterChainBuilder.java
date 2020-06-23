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

public class FilterChainBuilder {
    
    private FilterChain m_chain;
    private Filter m_currentFilter;
    
    public FilterChainBuilder() {
        m_chain = new FilterChain();
    }
    
    public FilterChain getChain() {
        return m_chain;
    }
    
    public FilterChainBuilder newFilter() {
        m_currentFilter = new Filter();
        m_chain.addFilter(m_currentFilter);
        return this;
    }
    
    public FilterChainBuilder setCategoryMatchPattern(String regexp) {
        m_currentFilter.setCategoryMatcher(regexp);
        return this;
    }
    
    public FilterChainBuilder setSeverityMatchPattern(String regexp) {
        m_currentFilter.setSeverityMatcher(regexp);
        return this;
    }
    
    public FilterChainBuilder setEventNameMatchPattern(String regexp) {
        m_currentFilter.setEventNameMatcher(regexp);
        return this;
    }
    
    public FilterChainBuilder setAddressMatchPattern(String iplikeStyleAddressPattern) {
        m_currentFilter.setAddressMatchSpec(iplikeStyleAddressPattern);
        return this;
    }
    
    public FilterChainBuilder setAction(String action) {
        m_currentFilter.setAction(action);
        return this;
    }

}
