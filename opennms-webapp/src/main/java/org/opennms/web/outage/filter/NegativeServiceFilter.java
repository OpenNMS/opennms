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

package org.opennms.web.outage.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.web.element.NetworkElementFactory;

/** Encapsulates all service filtering functionality. */
public class NegativeServiceFilter extends Object implements Filter {
    public static final String TYPE = "servicenot";

    protected int serviceId;

    public NegativeServiceFilter(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getSql() {
        return (" (OUTAGES.SERVICEID<>" + serviceId + " OR OUTAGES.SERVICEID IS NULL)");
    }
    
    public String getParamSql() {
        return (" (OUTAGES.SERVICEID<>? OR OUTAGES.SERVICEID IS NULL)");
    }
    
    public int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException {
    	ps.setInt(parameterIndex, serviceId);
    	return 1;
    }

    public String getDescription() {
        return (TYPE + "=" + serviceId);
    }

    public String getTextDescription() {
        String serviceName = Integer.toString(serviceId);

        try {
            serviceName = NetworkElementFactory.getServiceNameFromId(serviceId);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not get the service name for id " + serviceId);
        }

        return ("service is not " + serviceName);
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("Service ID", getServiceId())
            .toString();
    }

    public int getServiceId() {
        return (serviceId);
    }

    public boolean equals(Object obj) {
        return (this.toString().equals(obj.toString()));
    }
}
