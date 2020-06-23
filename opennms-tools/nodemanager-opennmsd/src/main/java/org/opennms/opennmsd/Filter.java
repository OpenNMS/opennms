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

import java.util.regex.Pattern;

public class Filter {
    
    public static final String DISCARD = "discard";
    public static final String ACCEPT = "accept";
    public static final String PRESERVE = "preserve";
    
    private Pattern m_categoryMatcher;
    private Pattern m_eventNameMatcher;
    private Pattern m_severityMatcher;
    private String m_addressMatchSpec;
    private String m_action = Filter.DISCARD;
    
    public String getAction() {
        return m_action;
    }
    
    public void setCategoryMatcher(String categoryMatcherRegexp) {
        m_categoryMatcher = Pattern.compile(categoryMatcherRegexp);
    }

    public void setEventNameMatcher(String eventNameMatcherRegexp) {
        m_eventNameMatcher = Pattern.compile(eventNameMatcherRegexp);
    }

    public void setSeverityMatcher(String severityMatcherRegexp) {
        m_severityMatcher = Pattern.compile(severityMatcherRegexp);
    }

    public void setAddressMatchSpec(String addressMatchSpec) {
        m_addressMatchSpec = addressMatchSpec;
    }

    public void setAction(String action) {
        m_action = action;
    }

    public boolean matches(NNMEvent event) {
        return categoryMatches(event.getCategory())
            && severityMatches(event.getSeverity())
            && eventNameMatches(event.getName())
            && addressMatches(event.getSourceAddress());
    }

    private boolean addressMatches(String sourceAddress) {
        if (m_addressMatchSpec == null) {
            return true;
        }
        return IpAddressUtils.verifyIpMatch(sourceAddress, m_addressMatchSpec);
    }

    private boolean eventNameMatches(String name) {
        if (m_eventNameMatcher == null) {
            return true;
        }
        return m_eventNameMatcher.matcher(name).find();
    }

    private boolean severityMatches(String severity) {
        if (m_severityMatcher == null) {
            return true;
        }
        return m_severityMatcher.matcher(severity).find();
    }

    private boolean categoryMatches(String category) {
        if (m_categoryMatcher == null) {
            return true;
        }
        return m_categoryMatcher.matcher(category).find();

    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer("[ ");
        buf.append(toPatternString(m_categoryMatcher)).append(" ");
        buf.append(toPatternString(m_severityMatcher)).append(" ");
        buf.append(toPatternString(m_eventNameMatcher)).append(" ");
        buf.append(toAddrSpecString(m_addressMatchSpec));
        buf.append(" ]");
        return buf.toString();
    }
    
    private String toPatternString(Pattern p) {
        if (p == null) return "<match-any>";
        return p.pattern();
    }
    
    private String toAddrSpecString(String addrSpec) {
        if (addrSpec == null) return "<match-any>";
        return addrSpec;
    }
    
    
}
