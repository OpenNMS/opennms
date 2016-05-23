/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.newts.converter;

import org.junit.Test;
import org.opennms.netmgt.rrd.model.v3.DS;
import org.opennms.netmgt.rrd.model.v3.DSType;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Timestamp;

import com.google.common.base.Optional;
import com.google.common.primitives.UnsignedLong;

public class NewtsConverterTest {

    @Test(expected=IllegalArgumentException.class)
    public void cantConvertOutOfRangeCounterToSample() {
        Resource resource = new Resource("resource", Optional.absent());
        DS ds = new DS();
        ds.setType(DSType.COUNTER);
        Timestamp timestamp = Timestamp.fromEpochSeconds(0);
        NewtsConverter.toSample(ds, resource, timestamp, Double.MAX_VALUE);
    }

    public void canConvertCounterToSample() {
        Resource resource = new Resource("resource", Optional.absent());
        DS ds = new DS();
        ds.setType(DSType.COUNTER);
        Timestamp timestamp = Timestamp.fromEpochSeconds(0);
        NewtsConverter.toSample(ds, resource, timestamp, UnsignedLong.MAX_VALUE.doubleValue());
    }
}
