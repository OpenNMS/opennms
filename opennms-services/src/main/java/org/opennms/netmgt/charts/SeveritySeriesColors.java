/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2005-2006, 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.charts;

import java.awt.Color;
import java.awt.Paint;

import org.apache.log4j.Category;
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
    private static Category log() {
        return ThreadCategory.getInstance(SeveritySeriesColors.class);
    }

}
