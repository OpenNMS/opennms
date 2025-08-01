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

import java.awt.Color;
import java.awt.Paint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>SeveritySeriesColors class.</p>
 *
 * @author <a href="david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class SeveritySeriesColors implements CustomSeriesColors {
    
    private static final Logger LOG = LoggerFactory.getLogger(SeveritySeriesColors.class);

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
            LOG.warn("Problem converting severity: {} to an int value.", severity);
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

}
