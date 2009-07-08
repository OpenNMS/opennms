/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
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

import org.apache.log4j.Category;
import org.jfree.chart.axis.ExtendedCategoryAxis;
import org.opennms.core.utils.ThreadCategory;

/**
 * @author <a href="david@opennms.org">David Hustace</a>
 */
public class SeveritySubLabels extends ExtendedCategoryAxis {
    private static final long serialVersionUID = 4985544589299368239L;

    public SeveritySubLabels() {
        super(null);
    }

    public SeveritySubLabels(String label) {
        super(label);
    }

    /**
     * Adds a sublabel for a category.
     * 
     * @param category  the category.
     * @param label  the label.
     */
    @SuppressWarnings("unchecked")
    public void addSubLabel(Comparable category, String label) {
        super.addSubLabel(category, convertLabel(label));
    }
    
    private static String convertLabel(String severity) {

        int sev = 0;
        String converted = "Unk";
        
        try {
            sev = Integer.parseInt(severity);
        } catch (NumberFormatException e) {
            log().warn("Problem converting severity: "+severity+" to an int value.");
        }

        switch (sev) {
        case 0 :
            converted = "Unk";
            break;
        case 1 :
            converted = "Ind";
            break;
        case 2 :
            converted = "Cleared";
            break;
        case 3 :
            converted = "Normal";
            break;
        case 4 :
            converted = "Warn";
            break;
        case 5 :
            converted = "Minor";
            break;
        case 6 :
            converted = "Major";
            break;
        case 7 :
            converted = "Critical";
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
        return ThreadCategory.getInstance(SeveritySubLabels.class);
    }
}