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

import org.jfree.chart.axis.ExtendedCategoryAxis;
import org.opennms.core.utils.ThreadCategory;

/**
 * <p>SeveritySubLabels class.</p>
 *
 * @author <a href="david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class SeveritySubLabels extends ExtendedCategoryAxis {
    private static final long serialVersionUID = 4985544589299368239L;

    /**
     * <p>Constructor for SeveritySubLabels.</p>
     */
    public SeveritySubLabels() {
        super(null);
    }

    /**
     * <p>Constructor for SeveritySubLabels.</p>
     *
     * @param label a {@link java.lang.String} object.
     */
    public SeveritySubLabels(String label) {
        super(label);
    }

    /**
     * {@inheritDoc}
     *
     * Adds a sublabel for a category.
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
    private static ThreadCategory log() {
        return ThreadCategory.getInstance(SeveritySubLabels.class);
    }
}
