/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.alarm.filter;

import org.opennms.web.filter.SubstringFilter;

public class EventParmLikeFilter extends SubstringFilter {

    /** Constant <code>TYPE="parmmatchany"</code> */
    public static final String TYPE = "parmmatchany";
    
    public EventParmLikeFilter(String parm) {
        super(TYPE, "eventParms", "eventParms", parm + "(string,text)");
    }

    @Override
    public String getTextDescription() {
        String strippedType = getValue().replace("(string,text)", "");
        String[] parms = strippedType.split("=");
        StringBuffer buffer = new StringBuffer(parms[0] + "=\"");
        buffer.append(parms[parms.length - 1]);
        buffer.append("\"");

        return buffer.toString();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return this.toString().equals(obj.toString());
    }
    
    @Override
    public String getDescription() {
        return TYPE + "=" + getValueString().replace("(string,text)", "");
        
    }

}
