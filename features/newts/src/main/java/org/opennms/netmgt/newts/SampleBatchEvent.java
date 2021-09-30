/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.newts;

import java.util.List;

import org.opennms.newts.api.Sample;

/**
 * Wrapper class for a {@list java.util.List} of {@link org.opennms.newts.api.Sample} objects.
 *
 * Instances of this class are preallocated by the {@link com.lmax.disruptor.dsl.Disruptor}.
 *
 * @author jwhite
 */
public class SampleBatchEvent {
    private List<Sample> m_samples;
    private boolean m_indexOnly;

    public void setSamples(List<Sample> samples) {
        m_samples = samples;
    }

    public List<Sample> getSamples() {
        return m_samples;
    }

    public void setIndexOnly(boolean indexOnly) {
        m_indexOnly = indexOnly;
    }

    public boolean isIndexOnly() {
        return m_indexOnly;
    }
}
