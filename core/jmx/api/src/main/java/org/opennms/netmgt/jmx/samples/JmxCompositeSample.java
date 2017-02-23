/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jmx.samples;

import javax.management.Attribute;
import javax.management.openmbean.CompositeData;

import org.opennms.netmgt.config.collectd.jmx.CompMember;
import org.opennms.netmgt.config.collectd.jmx.Mbean;

/**
 * A {@link JmxCompositeSample} should be created each time the {@link org.opennms.netmgt.jmx.JmxCollector}
 * collects a MBean Composite Member.
 */
public class JmxCompositeSample extends AbstractJmxSample {

    /**
     * The Composite Member the Composite Data belongs to.
     */
    private final CompMember compositeMember;

    /**
     * The collected CompositeData
     */
    private final CompositeData compositeData;

    public JmxCompositeSample(Mbean mbean, Attribute attribute, CompositeData compositeData, CompMember compositeMember) {
        super(mbean, attribute);
        this.compositeData = compositeData;
        this.compositeMember = compositeMember;
    }

    public CompMember getCompositeMember() {
        return compositeMember;
    }

    public String getCompositeKey() {
        return compositeMember.getName();
    }

    @Override
    public String getCollectedValueAsString() {
        Object value = compositeData.get(getCompositeKey());
        if (value != null) {
            return value.toString();
        }
        return null;
    }
}
