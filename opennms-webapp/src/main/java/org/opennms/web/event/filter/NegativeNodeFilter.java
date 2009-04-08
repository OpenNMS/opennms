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

package org.opennms.web.event.filter;

import org.opennms.web.filter.NegativeFilter;
import org.opennms.web.filter.SQLType;

/** Encapsulates all node filtering functionality. */
public class NegativeNodeFilter extends NegativeFilter<Integer> implements Filter {
    public static final String TYPE = "nodenot";

    protected int nodeId;

    public NegativeNodeFilter(int nodeId) {
        super(SQLType.INT, "EVENT.NODEID", "node", new Integer(nodeId), "nodenot");
        this.nodeId = nodeId;
    }

//    public String getSql() {
//        return (" (EVENTS.NODEID<>" + this.nodeId + " OR EVENTS.NODEID IS NULL)");
//    }
//    
//    public String getParamSql() {
//        return (" (EVENTS.NODEID<>? OR EVENTS.NODEID IS NULL)");
//    }
//    
//    public int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException {
//    	ps.setInt(parameterIndex, this.nodeId);
//    	return 1;
//    }
//
//    public String getDescription() {
//        return (TYPE + "=" + this.nodeId);
//    }
//
//    public String getTextDescription() {
//        String nodeName = Integer.toString(this.nodeId);
//        try {
//            nodeName = NetworkElementFactory.getNodeLabel(this.nodeId);
//        } catch (SQLException e) {
//        }
//
//        return ("node is not " + nodeName);
//    }
//
//    public String toString() {
//        return ("<EventFactory.NegativeNodeFilter: " + this.getDescription() + ">");
//    }
//
//    public int getNodeId() {
//        return (this.nodeId);
//    }
//
//    public boolean equals(Object obj) {
//        return (this.toString().equals(obj.toString()));
//    }
}
