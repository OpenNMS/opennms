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
package org.opennms.web.charts;

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
    public void addSubLabel(@SuppressWarnings("rawtypes") Comparable category, String label) {
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
