//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.charts;

import java.awt.Color;
import java.awt.Paint;

import org.opennms.core.utils.ThreadCategory;

/**
 * @author <a href="david@opennms.org">David Hustace</a>
 */
public class SeveritySeriesColors implements CustomSeriesColors {

    /**
     * 
     */
    public SeveritySeriesColors() {
        super();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.charts.CustomSeriesColors#getPaint(java.lang.Comparable)
     */
    public Paint getPaint(Comparable<?> cat) {
        
        int sev = 0;
        String severity = cat.toString();
        Paint converted = Color.BLACK;
        
        try {
            sev = Integer.parseInt(severity);
        } catch (NumberFormatException e) {
            log().warn("Problem converting severity: "+severity+" to an int value.");
        }

        switch (sev) {
        case 0 :
            break;
        case 1 :
            converted = Color.GRAY;
            break;
        case 2 :
            converted = Color.WHITE;
            break;
        case 3 :
            converted = Color.GREEN;
            break;
        case 4 :
            converted = Color.CYAN;
            break;
        case 5 :
            converted = Color.YELLOW;
            break;
        case 6 :
            converted = Color.ORANGE;
            break;
        case 7 :
            converted = Color.RED;
            break;
        }
        return converted;
    }
    /**
     * Logging helper method.
     * 
     * @return A log4j <code>Category</code>.
     */
    private static ThreadCategory log() {
        return ThreadCategory.getInstance(SeveritySeriesColors.class);
    }

}
