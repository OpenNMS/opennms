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
