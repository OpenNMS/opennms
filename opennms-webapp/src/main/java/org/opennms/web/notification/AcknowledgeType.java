/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc. All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.web.notification;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * Convenience class to determine what sort of notices to include in a
 * query.
 */
public enum AcknowledgeType {
    ACKNOWLEDGED("ack"), UNACKNOWLEDGED("unack"), BOTH("both");
    
    private static final Map<String, AcknowledgeType> s_ackTypesString;
    
    private String m_shortName;

    static {
        s_ackTypesString = new HashMap<String, AcknowledgeType>();

        for (AcknowledgeType ackType : AcknowledgeType.values()) {
            s_ackTypesString.put(ackType.getShortName(), ackType);
        }
    }

    private AcknowledgeType(String shortName) {
        m_shortName = shortName;
    }

    public String toString() {
        return "AcknowledgeType." + getName();
    }

    public String getName() {
        return name();
    }

    public String getShortName() {
        return m_shortName;
    }
    
    /**
     * Convenience method for getting the SQL <em>ORDER BY</em> clause related
     * this sort style.
     */
    protected String getAcknowledgeTypeClause() {
        switch (this) {
        case ACKNOWLEDGED:
            return " RESPONDTIME IS NOT NULL";
    
        case UNACKNOWLEDGED:
            return " RESPONDTIME IS NULL";
    
        case BOTH:
            return " (RESPONDTIME IS NULL OR RESPONDTIME IS NOT NULL)";
            
        default:
            throw new IllegalArgumentException("Cannot get clause for AcknowledgeType " + this);
        }
    }

    public static AcknowledgeType getAcknowledgeType(String ackTypeString) {
        Assert.notNull(ackTypeString, "Cannot take null parameters.");

        return s_ackTypesString.get(ackTypeString.toLowerCase());
    }
}
