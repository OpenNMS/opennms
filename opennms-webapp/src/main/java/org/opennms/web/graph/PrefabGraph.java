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

package org.opennms.web.graph;

public class PrefabGraph extends Object implements Comparable {
    private String m_name;

    private String m_title;

    private String[] m_columns;

    private String m_command;

    private String[] m_externalValues;
    
    private String[] m_propertiesValues;

    private int m_order;

    private String m_type;

    private String m_description;

    private String m_graphWidth;

    private String m_graphHeight;

    public PrefabGraph(String name, String title, String[] columns,
            String command, String[] externalValues,
            String[] propertiesValues, int order, String type,
            String description, String graphWidth, String graphHeight) {
        if (name == null || title == null || columns == null
                || command == null || externalValues == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        m_name = name;
        m_title = title;
        m_columns = columns;
        m_command = command;
        m_externalValues = externalValues;
	m_propertiesValues = propertiesValues;
        m_order = order;

        // type can be null
        m_type = type;

        // description can be null
        m_description = description;

        // width can be null
        m_graphWidth = graphWidth;

        // height can be null
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

    public String[] getPropertiesValues() {
	return m_propertiesValues;
    }


    /** Can be null. */
    public String getType() {
        return m_type;
    }

    /** Can be null. */
    public String getDescription() {
        return m_description;
    }

    /** Can be null. */
    public String getGraphWidth() {
        return m_graphWidth;
    }

    /** Can be null. */
    public String getGraphHeight() {
        return m_graphHeight;
    }

    public int compareTo(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if (!(obj instanceof PrefabGraph)) {
            throw new IllegalArgumentException("Can only compare to PrefabGraph objects.");
        }

        PrefabGraph otherGraph = (PrefabGraph) obj;

        return getOrder() - otherGraph.getOrder();
    }
}
