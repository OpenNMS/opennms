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
package org.opennms.netmgt.model;


/**
 * <p>AttributeStatistic class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class AttributeStatistic implements Comparable<AttributeStatistic> {
    private final OnmsAttribute m_attribute;
    private final Double m_statistic;

    /**
     * <p>Constructor for AttributeStatistic.</p>
     *
     * @param attribute a {@link org.opennms.netmgt.model.OnmsAttribute} object.
     * @param statistic a {@link java.lang.Double} object.
     */
    public AttributeStatistic(final OnmsAttribute attribute, final Double statistic) {
        m_attribute = attribute;
        m_statistic = statistic;
    }

    /**
     * <p>getAttribute</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsAttribute} object.
     */
    public OnmsAttribute getAttribute() {
        return m_attribute;
    }

    /**
     * <p>getStatistic</p>
     *
     * @return a {@link java.lang.Double} object.
     */
    public Double getStatistic() {
        return m_statistic;
    }

    /**
     * <p>compareTo</p>
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     * @param o a {@link org.opennms.netmgt.model.AttributeStatistic} object.
     * @return a int.
     */
    @Override
    public int compareTo(final AttributeStatistic o) {
        int diff;

        diff = getStatistic().compareTo(o.getStatistic()); 
        if (diff != 0) {
            return diff;
        }

        diff = getAttribute().getResource().getId().toString().compareToIgnoreCase(o.getAttribute().getResource().getId().toString());
        if (diff != 0) {
            return diff;
        }

        return Integer.valueOf(getAttribute().hashCode()).compareTo(o.getAttribute().hashCode());
    }
}
