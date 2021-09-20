/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.category;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Provides look and feel utilities for the JSPs presenting category (real time
 * console) information.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 */
public class CategoryUtil extends Object {
    /**
     * Specifies how the category values should look.
     * <p>
     * Note this value is currently public, but consider this temporary. To hide
     * the implementation (so we can change it later), please call
     * {@link #formatValue formatValue}instead.
     * </p>
     */
    public static final DecimalFormat valueFormat = new DecimalFormat("0.000");

    /** HTML color code for the color of green we use. */
    // public static final String GREEN = m_green;
    /** HTML color code for the color of yello we use. */
    // public static final String YELLOW = m_yellow;
    /** HTML color code for the color of red we use. */
    // public static final String RED = m_red;

    /** Private, empty constructor so this class will not be instantiated. */
    private CategoryUtil() {
    }

    /**
     * Format an RTC value the way we want it.
     *
     * @param value a double.
     * @return a {@link java.lang.String} object.
     */
    public static String formatValue(double value) {
        return valueFormat.format(value);
    }

    /**
     * Determine the CSS class to use for a given category value and thresholds.
     *
     * @param category a {@link org.opennms.web.category.Category} object.
     * @return a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    public static String getCategoryClass(Category category) throws IOException {
        if (category == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return getCategoryClass(category.getNormalThreshold(), category.getWarningThreshold(), category.getValue());
    }

    /**
     * Determine the CSS color to use for a given value and the given category's
     * thresholds.
     *
     * @param category a {@link org.opennms.web.category.Category} object.
     * @param value a double.
     * @return a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    public static String getCategoryClass(Category category, double value) throws IOException {
        if (category == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return getCategoryClass(category.getNormalThreshold(), category.getWarningThreshold(), value);
    }

    /**
     * Determine the CSS class to use for a given value and thresholds.
     *
     * @param normal a double.
     * @param warning a double.
     * @param value a double.
     * @return a {@link java.lang.String} object.
     */
    public static String getCategoryClass(double normal, double warning,
					  double value) {
        String m_good = "Normal";
        String m_warn = "Warning";
        String m_crit = "Critical";

        String _class = m_crit;

        if (value >= normal) {
            _class = m_good;
        } else if (value >= warning) {
            _class = m_warn;
        }

        return (_class);
    }
}
