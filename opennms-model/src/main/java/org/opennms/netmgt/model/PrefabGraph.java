//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Apr 23: Use asserts to check for arguments required to be non-null in constructor,
//              convert width and height to Integers, and add suppress String array. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.model;

import org.springframework.util.Assert;

public class PrefabGraph extends Object implements Comparable<PrefabGraph> {
    private String m_name;

    private String m_title;

    private String[] m_columns;

    private String m_command;

    private String[] m_externalValues;
    
    private String[] m_propertiesValues;

    private int m_order;

    private String[] m_types = new String[0];

    private String m_description;

    private Integer m_graphWidth;

    private Integer m_graphHeight;
    
    private String[] m_suppress;

    public PrefabGraph(String name, String title, String[] columns,
            String command, String[] externalValues,
            String[] propertiesValues, int order, String[] types,
            String description, Integer graphWidth, Integer graphHeight,
            String[] suppress) {
        Assert.notNull(name, "name argument cannot be null");
        Assert.notNull(title, "title argument cannot be null");
        Assert.notNull(columns, "columns argument cannot be null");
        Assert.notNull(command, "command argument cannot be null");
        Assert.notNull(externalValues, "externalValues argument cannot be null");
        Assert.notNull(propertiesValues, "propertiesValues argument cannot be null");
        Assert.notNull(suppress, "suppress argument cannot be null");

        m_name = name;
        m_title = title;
        m_columns = columns;
        m_command = command;
        m_externalValues = externalValues;
        m_propertiesValues = propertiesValues;
        m_order = order;
        m_suppress = suppress;

        m_types = types;
        m_description = description;
        m_graphWidth = graphWidth;
        m_graphHeight = graphHeight;
    }

    public String getName() {
        return m_name;
    }

    public String getTitle() {
        return m_title;
    }

    public int getOrder() {
        return m_order;
    }

    public String[] getColumns() {
        return m_columns;
    }

    public String getCommand() {
        return m_command;
    }

    public String[] getExternalValues() {
        return m_externalValues;
    }

    /**
     * @return list of required string properties
     * @see org.opennms.netmgt.model.StringPropertyAttribute
     */
    public String[] getPropertiesValues() {
        return m_propertiesValues;
    }

    /** Can be null. */
    public String[] getTypes() {
        return m_types;
    }
    
    public boolean hasMatchingType(String... matchingTypes) {
        for (String matchingType : matchingTypes) {
            for (String type : m_types) {
                if (type != null && type.equals(matchingType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Can be null. */
    public String getDescription() {
        return m_description;
    }

    /** Can be null. */
    public Integer getGraphWidth() {
        return m_graphWidth;
    }

    /** Can be null. */
    public Integer getGraphHeight() {
        return m_graphHeight;
    }
    
    public String[] getSuppress() {
        return m_suppress;
    }

    public int compareTo(PrefabGraph other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return getOrder() - other.getOrder();
    }
}
