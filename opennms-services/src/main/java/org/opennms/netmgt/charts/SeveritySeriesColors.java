/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.charts;

import java.awt.Color;
import java.awt.Paint;

import org.opennms.core.utils.ThreadCategory;

/**
 * <p>SeveritySeriesColors class.</p>
 *
 * @author <a href="david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class SeveritySeriesColors implements CustomSeriesColors {

    /**
     * <p>Constructor for SeveritySeriesColors.</p>
     */
    public SeveritySeriesColors() {
        super();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.charts.CustomSeriesColors#getPaint(java.lang.Comparable)
     */
    /** {@inheritDoc} */
    @Override
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
