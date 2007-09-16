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

package org.opennms.web.outage;

import java.util.Date;

/**
 * A data structure holding information on all outages on a single IP address.
 * 
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS </A>
 */
public class OutageSummary extends Object {
    protected final int nodeId;
    protected final String nodeLabel;
    protected final Date timeDown;
    protected final Date timeUp;

    public OutageSummary(int nodeId, String nodeLabel, Date timeDown, Date timeUp) {
        if (nodeLabel == null || timeDown == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        this.nodeId = nodeId;
        this.nodeLabel = nodeLabel;
        this.timeDown = timeDown;
        this.timeUp = timeUp;
    }
    public OutageSummary(int nodeId, String nodeLabel, Date timeDown) {
        this(nodeId, nodeLabel, timeDown, null);
    }

    public int getNodeId() {
        return (this.nodeId);
    }

    /** @deprecated Please use {@link #getNodeLabel getNodeLabel}instead. */
    public String getHostname() {
        return (this.nodeLabel);
    }

    public String getNodeLabel() {
        return (this.nodeLabel);
    }

    public Date getTimeDown() {
        return (this.timeDown);
    }

    public Date getTimeUp() {
        return (this.timeUp);
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<OutageSummary: ");
        buffer.append(this.nodeId);
        buffer.append(":");
        buffer.append(this.nodeLabel);
        buffer.append(", down at ");
        buffer.append(this.timeDown);
        if (this.timeUp != null) {
            buffer.append(", back up at ");
            buffer.append(this.timeUp);
        }
        return (buffer.toString());
    }

};
