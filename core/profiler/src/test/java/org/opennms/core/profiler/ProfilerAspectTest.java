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

package org.opennms.core.profiler;

import static org.opennms.core.profiler.ProfilerAspect.humanReadable;

import org.junit.Assert;
import org.junit.Test;

public class ProfilerAspectTest {

    @Test
    public void testHumanReadable() {
        Assert.assertEquals("0ms", humanReadable(0));
        Assert.assertEquals("10ms", humanReadable(10));
        Assert.assertEquals("1s 0ms", humanReadable(1000));
        Assert.assertEquals("1s 50ms", humanReadable(1050));

        Assert.assertEquals("5m 0s 0ms", humanReadable(5 * 1000 * 60));
        Assert.assertEquals("5m 0s 100ms", humanReadable(5* 1000 * 60 + 100));

        Assert.assertEquals("5m 2s 100ms", humanReadable(5* 1000 * 60 + 2000 + 100));

        Assert.assertEquals("2h 0m 0s 0ms", humanReadable(2 * 60 * 1000 * 60 ));
        Assert.assertEquals("2h 7m 3s 1ms", humanReadable(2 * 60 * 1000 * 60 + 7 * 1000 * 60 + 3000 + 1));

        Assert.assertEquals("13s 106ms", humanReadable(13106));
    }
}
