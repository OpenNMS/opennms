/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
