/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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
