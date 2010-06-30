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

package org.opennms.web.alarm.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Encapsulates filtering on exact unique event identifiers.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class NegativeAcknowledgedByFilter extends Object implements Filter {
    /** Constant <code>TYPE="acknowledgedByNot"</code> */
    public static final String TYPE = "acknowledgedByNot";

    protected String user;

    /**
     * <p>Constructor for NegativeAcknowledgedByFilter.</p>
     *
     * @param user a {@link java.lang.String} object.
     */
    public NegativeAcknowledgedByFilter(String user) {
        if (user == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        this.user = user;
    }

    /**
     * <p>getSql</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSql() {
        return (" (ALARMACKUSER<>'" + this.user + "' OR ALARMACKUSER IS NULL)");
    }
    
    /**
     * <p>getParamSql</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getParamSql() {
        return (" (ALARMACKUSER<>? OR ALARMACKUSER IS NULL)");
    }
    
    /** {@inheritDoc} */
    public int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException {
    	ps.setString(parameterIndex, this.user);
    	return 1;
    }

    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return (TYPE + "=" + this.user);
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTextDescription() {
        return ("not acknowledged by " + this.user);
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return ("<AlarmFactory.NegativeAcknowledgedByFilter: " + this.getDescription() + ">");
    }

    /**
     * <p>getAcknowledgedByFilter</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAcknowledgedByFilter() {
        return (this.user);
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        return (this.toString().equals(obj.toString()));
    }
}
