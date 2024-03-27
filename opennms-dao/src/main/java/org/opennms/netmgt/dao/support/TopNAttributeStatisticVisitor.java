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
package org.opennms.netmgt.dao.support;

import java.util.Comparator;

/**
 * <p>TopNAttributeStatisticVisitor class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class TopNAttributeStatisticVisitor extends BottomNAttributeStatisticVisitor {
    /**
     * <p>Constructor for TopNAttributeStatisticVisitor.</p>
     */
    public TopNAttributeStatisticVisitor() {
        super();
        setComparator(new ReverseDoubleComparator());
    }
    
    public static class ReverseDoubleComparator implements Comparator<Double> {
        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(Double o1, Double o2) {
            return o2.compareTo(o1);
        }
    }
}
