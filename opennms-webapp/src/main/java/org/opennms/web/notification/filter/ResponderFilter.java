/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.notification.filter;

import org.opennms.web.filter.EqualsFilter;
import org.opennms.web.filter.SQLType;



/**
 * Encapsulates all responder filtering functionality.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ResponderFilter extends EqualsFilter<String> {
    /** Constant <code>TYPE="responder"</code> */
    public static final String TYPE = "responder";

    //protected String responder;

    /**
     * <p>Constructor for ResponderFilter.</p>
     *
     * @param responder a {@link java.lang.String} object.
     */
    public ResponderFilter(String responder) {
        super(TYPE, SQLType.STRING, "ANSWEREDBY", "answeredBy", responder);
        //this.responder = responder;
    }

//    public String getSql() {
//        return (" ANSWEREDBY='" + this.responder + "'");
//    }
//    
//    public String getParamSql() {
//        return (" ANSWEREDBY=?");
//    }
//    
//    public int bindParams(PreparedStatement ps, int parameterIndex) throws SQLException {
//    	ps.setString(parameterIndex, this.responder);
//    	return 1;
//    }
//
//    public String getDescription() {
//        return (TYPE + "=" + this.responder);
//    }
//
//    public String getTextDescription() {
//        return this.getDescription();
//    }
//
//    public String toString() {
//        return ("<NoticeFactory.ResponderFilter: " + this.getDescription() + ">");
//    }
//
//    public String getResponder() {
//        return (this.responder);
//    }
//
//    public boolean equals(Object obj) {
//        return (this.toString().equals(obj.toString()));
//    }
}
