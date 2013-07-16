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

import org.jfree.chart.axis.ExtendedCategoryAxis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>SeveritySubLabels class.</p>
 *
 * @author <a href="david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class SeveritySubLabels extends ExtendedCategoryAxis {
    
    private static final Logger LOG = LoggerFactory.getLogger(SeveritySubLabels.class);
    
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
    @Override
    public void addSubLabel(@SuppressWarnings("unchecked") Comparable category, String label) {
        super.addSubLabel(category, convertLabel(label));
    }
    
    private static String convertLabel(String severity) {

        int sev = 0;
        String converted = "Unk";
        
        try {
            sev = Integer.parseInt(severity);
        } catch (NumberFormatException e) {
            LOG.warn("Problem converting severity: {} to an int value.", severity);
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
}
