/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.sampler;

import org.opennms.netmgt.api.sample.Sample;
import org.opennms.netmgt.collection.support.AbstractCollectionAttribute;

public class SamplerCollectionAttribute extends AbstractCollectionAttribute {
	private final Sample m_sample;

	public SamplerCollectionAttribute(SamplerCollectionAttributeType attribType, SamplerCollectionResource resource, Sample sample) {
		super(attribType, resource);
		if (sample == null) {
			throw new IllegalArgumentException("Sample cannot be null");
		}
		m_sample = sample;
	}

	@Override
	public String getNumericValue() {
		return String.valueOf(m_sample.getValue().doubleValue());
	}

	@Override
	public String getStringValue() {
		return m_sample.getValue().toString(); //Should this be null instead?
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + getName() + "=" + getStringValue();
	}

	@Override
	public String getMetricIdentifier() {
		// TODO Figure out what to return for this value to eventually support NRTG
		return null;
	}

}
